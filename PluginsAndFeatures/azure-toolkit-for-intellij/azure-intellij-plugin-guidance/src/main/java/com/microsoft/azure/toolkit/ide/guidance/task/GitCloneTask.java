package com.microsoft.azure.toolkit.ide.guidance.task;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.ide.impl.ProjectUtil;
import com.microsoft.azure.toolkit.ide.guidance.ComponentContext;
import com.microsoft.azure.toolkit.ide.guidance.Guidance;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceConfigManager;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceTask;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceViewManager;
import com.microsoft.azure.toolkit.ide.guidance.config.SequenceConfig;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Optional;

public class GitCloneTask implements GuidanceTask {
    public static final String DIRECTORY = "directory";
    public static final String DEFAULT_GIT_DIRECTORY = "defaultGitDirectory";
    public static final String BRANCH = "branch";
    public static final String REPOSITORY = "repository";
    public static final String REPOSITORY_PATH = "repository_path";
    private final Guidance guidance;
    private final ComponentContext context;

    public GitCloneTask(@Nonnull ComponentContext context) {
        this.context = context;
        this.guidance = context.getGuidance();
        init();
    }


    @Override
    public boolean isDone() {
        // Check whether project was clone to local
        final SequenceConfig workspaceConfig = GuidanceConfigManager.getInstance().getProcessConfigFromWorkspace(context.getProject());
        if (workspaceConfig != null && StringUtils.equals(workspaceConfig.getName(), guidance.getName())) {
            Optional.ofNullable(context.getProject().getBasePath()).ifPresent(path -> this.context.applyResult(DEFAULT_GIT_DIRECTORY, path));
            return true;
        } else {
            return false;
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return "task.common.clone";
    }

    @Override
    public void execute() throws Exception {
        final String repository = (String) context.getParameter(REPOSITORY);
        final String branch = (String) context.getParameter(BRANCH);
        final String repositoryPath = (String) context.getParameter(REPOSITORY_PATH);
        final String directory = (String) context.getParameter(DIRECTORY);
        try {
            final CloneCommand cloneCommand = Git.cloneRepository().setURI(repository).setDirectory(Paths.get(directory).toFile());
            if (StringUtils.isNotBlank(branch)) {
                cloneCommand.setBranch(branch);
            }
            cloneCommand.call();
            // Copy get start file to path
            final File target = StringUtils.isEmpty(repositoryPath) ? new File(directory) : new File(directory, repositoryPath);
            AzureMessager.getMessager().info(AzureString.format("Clone project to %s successfully.", directory));
            copyConfigurationToWorkspace(target);
            ProjectUtil.openOrImport(target.toPath(), OpenProjectTask.build().asNewProject());
            GuidanceViewManager.getInstance().closeGuidance(context.getProject());
        } catch (final Exception ex) {
            AzureMessager.getMessager().error(ex);
            throw new AzureToolkitRuntimeException(ex);
        }
    }

    private void init() {
        final String directoryName = String.format("%s-%s", context.getGuidance().getName(), Utils.getTimestamp());
        final String defaultPath = new File(System.getProperty("user.home"), directoryName).getAbsolutePath();
        this.context.applyResult(DEFAULT_GIT_DIRECTORY, defaultPath);
    }

    private void copyConfigurationToWorkspace(final File target) throws IOException {
        if (StringUtils.isEmpty(guidance.getUri())) {
            return;
        }
        try (final InputStream inputStream = GuidanceConfigManager.class.getResourceAsStream(guidance.getUri())) {
            if (inputStream == null) {
                return;
            }
            FileUtils.copyInputStreamToFile(inputStream, new File(target, GuidanceConfigManager.GETTING_START_CONFIGURATION_NAME));
        }
    }
}
