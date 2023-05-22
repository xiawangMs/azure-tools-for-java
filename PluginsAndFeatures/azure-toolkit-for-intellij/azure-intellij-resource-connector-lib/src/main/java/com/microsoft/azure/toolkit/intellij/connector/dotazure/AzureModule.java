package com.microsoft.azure.toolkit.intellij.connector.dotazure;

import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.intellij.common.runconfig.IWebAppRunConfiguration;
import com.microsoft.azure.toolkit.intellij.connector.IConnectionAware;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AzureModule {
    private static Map<Module, AzureModule> modules = new HashMap<>();
    @Nonnull
    private final Module module;
    @Nullable
    private Environment environment;

    public String getName() {
        return module.getName();
    }

    public AzureModule initialize() {
        return this.getModuleDir().map(moduleDir -> {
            try {
                final VirtualFile dotAzure = VfsUtil.createDirectoryIfMissing(moduleDir, ".azure");
                final VirtualFile dotEnv = dotAzure.findOrCreateChildData(this, ".env");
                if (Objects.isNull(environment)) {
                    this.environment = new Environment(dotEnv, this.module);
                }
            } catch (final IOException e) {
                throw new AzureToolkitRuntimeException(e);
            }
            return this;
        }).orElse(this);
    }

    @Nullable
    public synchronized Environment getEnvironment() {
        if (Objects.isNull(this.environment)) {
            this.environment = this.getDotEnvFile().map(f -> new Environment(f, this.module)).orElse(null);
        }
        return this.environment;
    }

    public Optional<VirtualFile> getModuleDir() {
        return Optional.of(module)
            .map(ProjectUtil::guessModuleDir);
    }

    public Optional<VirtualFile> getDotAzureDir() {
        return Optional.of(module)
            .map(ProjectUtil::guessModuleDir)
            .map(p -> p.findChild(".azure"));
    }

    public Optional<VirtualFile> getDotEnvFile() {
        return this.getDotAzureDir()
            .map(f -> f.findChild(".env"));
    }

    public boolean isInitialized() {
        return this.getDotEnvFile().isPresent();
    }

    public static AzureModule from(@Nonnull Module module) {
        return modules.computeIfAbsent(module, AzureModule::new);
    }

    public static List<AzureModule> list(@Nonnull Project project) {
        return Arrays.stream(ModuleManager.getInstance(project).getModules()).map(AzureModule::from).toList();
    }

    /**
     * check if the given {@param configuration} meet the requirements to convert into an {@link AzureModule}
     *
     * @return true if {@param configuration} is a
     * {@link ModuleBasedConfiguration}, {@link IWebAppRunConfiguration} or {@link IConnectionAware}.
     */
    public static boolean isSupported(RunConfiguration configuration) {
        return Optional.ofNullable(configuration)
            .map(AzureModule::getTargetModule)
            .isPresent();
    }

    /**
     * create {@code AzureModule} if the given {@param configuration} meet the requirements.
     * see {@link AzureModule#isSupported(RunConfiguration)}
     */
    public static Optional<AzureModule> createIfSupport(RunConfiguration configuration) {
        return Optional.ofNullable(configuration)
            .map(AzureModule::getTargetModule)
            .map(AzureModule::from);
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
}
