package com.microsoft.azure.toolkit.intellij.connector.dotazure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.intellij.common.runconfig.IWebAppRunConfiguration;
import com.microsoft.azure.toolkit.intellij.connector.Connection;
import com.microsoft.azure.toolkit.intellij.connector.IConnectionAware;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import io.github.cdimascio.dotenv.internal.DotenvParser;
import io.github.cdimascio.dotenv.internal.DotenvReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class AzureModule {
    @Nonnull
    private final Project project;
    @Nullable
    private final Module module;

    public AzureModule(@Nonnull Module module) {
        this.project = module.getProject();
        this.module = module;
    }

    public void initialize() throws IOException {
        this.createConfigJsonIfNot();
    }

    public void initializeAndCreateDefaultEnv(String envName) throws IOException {
        this.createConfigJsonIfNot();
        if (StringUtils.isBlank(this.getDefaultEnvironmentName())) {
            this.createEnvironmentIfNot(envName);
            this.setDefaultEnvironment(envName);
        }
    }

    private void createConfigJsonIfNot() throws IOException {
        final VirtualFile moduleDir = this.getModuleDir();
        if (Objects.nonNull(moduleDir) && Objects.isNull(this.getConfigJsonFile())) {
            final VirtualFile dotAzure = VfsUtil.createDirectoryIfMissing(moduleDir, ".azure");
            final VirtualFile configJson = dotAzure.findOrCreateChildData(this, "config.json");
        }
    }

    public void createEnvironmentIfNot(String envName) throws IOException {
        final VirtualFile moduleDir = this.getModuleDir();
        if (Objects.nonNull(moduleDir) && Objects.isNull(this.getDotEnvFile(envName))) {
            final VirtualFile dotAzure = VfsUtil.createDirectoryIfMissing(moduleDir, ".azure");
            final VirtualFile envDir = VfsUtil.createDirectoryIfMissing(dotAzure, envName);
            final VirtualFile dotEnv = envDir.findOrCreateChildData(this, ".env");
        }
    }

    public void createDefaultEnvironmentIfNot(String envName) throws IOException {
        if (StringUtils.isBlank(this.getDefaultEnvironmentName())) {
            this.createEnvironmentIfNot(envName);
            this.setDefaultEnvironment(envName);
        }
    }

    @Nullable
    public VirtualFile getDotAzureDir() {
        return Optional.ofNullable(module).map(ProjectUtil::guessModuleDir).map(p -> p.findChild(".azure"))
            .or(() -> Optional.of(project).map(ProjectUtil::guessProjectDir).map(p -> p.findChild(".azure")))
            .orElse(null);
    }

    @Nullable
    public VirtualFile getModuleDir() {
        return Optional.ofNullable(module).map(ProjectUtil::guessModuleDir)
            .or(() -> Optional.of(project).map(ProjectUtil::guessProjectDir))
            .orElse(null);
    }

    @Nullable
    public VirtualFile getConfigJsonFile() {
        return Optional.ofNullable(getDotAzureDir())
            .map(f -> f.findChild("config.json"))
            .orElse(null);
    }

    @Nullable
    public VirtualFile getDefaultDotEnvFile() {
        return Optional.ofNullable(this.getDefaultEnvironmentName())
            .map(this::getDotEnvFile).orElse(null);
    }

    @Nullable
    public VirtualFile getDotEnvFile(@Nonnull final String envName) {
        final VirtualFile dotAzure = this.getDotAzureDir();
        return Optional.ofNullable(dotAzure)
            .map(f -> f.findChild(envName))
            .map(f -> f.findChild(".env"))
            .orElse(null);
    }

    public List<Pair<String, String>> loadDefaultEnv() {
        return loadEnv(this.getDefaultDotEnvFile());
    }

    public List<Pair<String, String>> loadEnv(final String envName) {
        return loadEnv(this.getDotEnvFile(envName));
    }

    @Nonnull
    public static List<Pair<String, String>> loadEnv(VirtualFile dotEnvFile) {
        if (Objects.isNull(dotEnvFile)) {
            return Collections.emptyList();
        }
        final DotenvReader reader = new DotenvReader(dotEnvFile.getParent().getPath(), dotEnvFile.getName());
        final DotenvParser parser = new DotenvParser(reader, false, false);
        return parser.parse().stream().map(e -> Pair.of(e.getKey(), e.getValue())).toList();
    }

    @Nullable
    public String getDefaultEnvironmentName() {
        final VirtualFile configFile = getConfigJsonFile();
        if (Objects.nonNull(configFile)) {
            try {
                final String content = VfsUtil.loadText(configFile);
                if (StringUtils.isBlank(content)) {
                    return null;
                }
                final HashMap<String, String> map = new ObjectMapper().readValue(content, new TypeReference<>() {
                });
                return map.get("defaultEnvironment");
            } catch (final IOException e) {
                log.error("failed to load content of `.azure/config.json`", e);
            }
        }
        return null;
    }

    public void setDefaultEnvironment(String envName) {
        final VirtualFile configFile = getConfigJsonFile();
        if (Objects.isNull(configFile)) {
            throw new AzureToolkitRuntimeException("`.azure/config.json` is not found.");
        }
        try {
            final String content = VfsUtil.loadText(configFile);
            final HashMap<String, Object> map = new HashMap<>();
            final ObjectMapper mapper = new ObjectMapper();
            if (StringUtils.isBlank(content)) {
                map.put("version", 1);
                map.put("defaultEnvironment", envName);
            } else {
                final TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
                };
                map.putAll(mapper.readValue(content, typeRef));
                map.put("defaultEnvironment", envName);
            }
            mapper.writeValue(configFile.getOutputStream(this), map);
        } catch (final IOException e) {
            log.error("failed to set default environment.", e);
        }
    }

    public void appendDotEnv(@Nonnull String envName, @Nonnull String content) {
        final VirtualFile dotEnvFile = this.getDotEnvFile(envName);
        if (Objects.nonNull(dotEnvFile)) {
            AzureTaskManager.getInstance().write(() -> {
                try {
                    Files.writeString(dotEnvFile.toNioPath(), content, StandardOpenOption.APPEND);
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public boolean isValid() {
        return Objects.nonNull(this.getDefaultDotEnvFile());
    }

    public static AzureModule from(RunConfiguration configuration) {
        final Project project = configuration.getProject();
        final Module module = AzureModule.getTargetModule(configuration);
        return new AzureModule(project, module);
    }

    @Nullable
    public static Module getTargetModule(@Nonnull RunConfiguration configuration) {
        if (configuration instanceof ModuleBasedConfiguration) {
            return ((ModuleBasedConfiguration<?, ?>) configuration).getConfigurationModule().getModule();
        } else if (configuration instanceof IWebAppRunConfiguration) {
            return ((IWebAppRunConfiguration) configuration).getModule();
        } else if (configuration instanceof IConnectionAware) {
            return ((IConnectionAware) configuration).getModule();
        }
        return null;
    }

    public void addConnection(@Nonnull Connection<?, ?> connection) {
        final String name = this.getDefaultEnvironmentName();
        if (StringUtils.isNotBlank(name)) {
            final String envVariables = generateEnvironmentString(project, connection);
            this.appendDotEnv(name, envVariables + System.lineSeparator());
        }
    }

    public static String generateEnvironmentString(@Nonnull final Project project, @Nonnull final Connection<?, ?> connection) {
        final StringBuilder string = new StringBuilder();
        final String dataId = connection.getResource().getDataId();
        string.append("# resource: ").append(dataId).append(System.lineSeparator());
        connection.getEnvironmentVariables(project)
            .forEach((k, v) -> string.append(k).append("=").append("\"").append(v).append("\"").append(System.lineSeparator()));
        return string.toString();
    }
}
