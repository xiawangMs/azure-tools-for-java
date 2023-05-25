package com.microsoft.azure.toolkit.intellij.connector.dotazure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AzureModule {
    private static final Map<Module, AzureModule> modules = new ConcurrentHashMap<>();
    private final Map<String, Profile> environments = new ConcurrentHashMap<>();
    @Nonnull
    private final Module module;
    @Getter
    @Nullable
    private VirtualFile dotAzure;
    @Getter
    @Nullable
    private VirtualFile configJsonFile;
    @Nullable
    private Profile defaultProfile;

    public AzureModule(@Nonnull final Module module) {
        this.module = module;
        this.getModuleDir().map(d -> d.findChild(".azure")).ifPresent(dotAzure -> {
            this.dotAzure = dotAzure;
            this.configJsonFile = this.dotAzure.findChild("config.json");
        });
    }

    @Nonnull
    public AzureModule initializeIfNot() {
        if (Objects.nonNull(this.configJsonFile)) {
            return this;
        }
        return this.getModuleDir().map(moduleDir -> {
            try {
                this.dotAzure = VfsUtil.createDirectoryIfMissing(moduleDir, ".azure");
                final VirtualFile dotGitIgnore = dotAzure.findOrCreateChildData(this, ".gitignore");
                dotGitIgnore.setBinaryContent(".env\nconnections.resources.xml".getBytes());
                this.configJsonFile = dotAzure.findOrCreateChildData(this, "config.json");
                this.dotAzure.refresh(true, false);
            } catch (final IOException e) {
                throw new AzureToolkitRuntimeException(e);
            }
            return this;
        }).orElse(this);
    }

    @Nonnull
    public Profile initializeWithDefaultProfileIfNot() {
        final AzureModule module = this.initializeIfNot();
        Profile profile = module.getDefaultProfile();
        if (Objects.isNull(profile)) {
            profile = module.getOrCreateProfile("default");
            module.setDefaultProfile(profile);
        }
        return profile;
    }

    @Nullable
    public Profile getDefaultProfile() {
        if (Objects.isNull(this.defaultProfile)) {
            if (!this.isInitialized()) {
                return null;
            }
            final String envName = this.getDefaultProfileName();
            if (StringUtils.isBlank(envName)) {
                return null;
            }
            this.defaultProfile = this.getProfile(envName);
        }
        return this.defaultProfile;
    }

    @Nullable
    public Profile getProfile(String profileName) {
        if (!this.isInitialized()) {
            return null;
        }
        return this.environments.computeIfAbsent(profileName, name -> Optional
            .ofNullable(this.dotAzure)
            .map(dotAzure -> dotAzure.findChild(profileName))
            .map(envDir -> envDir.findChild(".env"))
            .map(dotEnv -> new Profile(name, dotEnv, this)).orElse(null));
    }

    @Nonnull
    public Profile getOrCreateProfile(String profileName) {
        this.validate();
        return this.environments.computeIfAbsent(profileName, name -> {
            try {
                final VirtualFile envDir = VfsUtil.createDirectoryIfMissing(this.dotAzure, profileName);
                final VirtualFile dotEnv = envDir.findOrCreateChildData(this, ".env");
                return new Profile(name, dotEnv, this);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Nullable
    @SneakyThrows(IOException.class)
    private String getDefaultProfileName() {
        if (!this.isInitialized()) {
            return null;
        }
        final VirtualFile configFile = Objects.requireNonNull(getConfigJsonFile());
        final String content = VfsUtil.loadText(configFile);
        if (StringUtils.isBlank(content)) {
            return null;
        }
        final HashMap<String, String> map = new ObjectMapper().readValue(content, new TypeReference<>() {
        });
        return map.get("defaultEnvironment");
    }

    public void setDefaultProfile(@Nonnull Profile profile) {
        this.validate();
        final VirtualFile configFile = Objects.requireNonNull(getConfigJsonFile());
        try {
            final String content = VfsUtil.loadText(configFile);
            final HashMap<String, Object> map = new HashMap<>();
            final ObjectMapper mapper = new ObjectMapper();
            if (StringUtils.isBlank(content)) {
                map.put("version", 1);
                map.put("defaultEnvironment", profile.getName());
            } else {
                final TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
                };
                map.putAll(mapper.readValue(content, typeRef));
                map.put("defaultEnvironment", profile.getName());
            }
            mapper.writeValue(configFile.getOutputStream(this), map);
            this.defaultProfile = profile;
        } catch (final IOException e) {
            log.error("failed to set default environment.", e);
        }
    }

    private void validate() {
        final VirtualFile configFile = getConfigJsonFile();
        if (Objects.isNull(configFile)) {
            throw new AzureToolkitRuntimeException("Azure module is not initialized.");
        }
    }

    public String getName() {
        return module.getName();
    }

    public Optional<VirtualFile> getModuleDir() {
        return Optional.of(module).map(ProjectUtil::guessModuleDir);
    }

    public Optional<VirtualFile> getDotAzureDir() {
        return Optional.ofNullable(this.dotAzure);
    }

    public boolean isInitialized() {
        return Objects.nonNull(this.configJsonFile);
    }

    public static AzureModule from(@Nonnull Module module) {
        return modules.computeIfAbsent(module, t -> new AzureModule(module));
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

    public Project getProject() {
        return this.module.getProject();
    }
}
