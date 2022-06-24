package com.microsoft.azure.toolkit.ide.guidance;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.microsoft.azure.toolkit.ide.common.store.AzureStoreManager;
import com.microsoft.azure.toolkit.ide.common.store.IIdeStore;
import com.microsoft.azure.toolkit.ide.guidance.config.SequenceConfig;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GuidanceStartupListener implements StartupActivity.DumbAware {
    private static final String GUIDANCE = "guidance";
    private static final String GUIDANCE_SHOWN = "guidance_shown";

    @Override
    public void runActivity(@NotNull Project project) {
        final SequenceConfig sequenceConfigFromWorkspace = GuidanceConfigManager.getInstance().getProcessConfigFromWorkspace(project);
        Optional.ofNullable(sequenceConfigFromWorkspace)
                .ifPresent(config -> GuidanceViewManager.getInstance().showGuidance(project, sequenceConfigFromWorkspace));
        // show guidance tool window after install
        final IIdeStore ideStore = AzureStoreManager.getInstance().getIdeStore();
        if (StringUtils.isEmpty(ideStore.getProperty(GUIDANCE, GUIDANCE_SHOWN))) {
            ideStore.setProperty(GUIDANCE, GUIDANCE_SHOWN, String.valueOf(true));
            GuidanceViewManager.getInstance().showGuidanceWelcome(project);
        }
    }
}
