package com.microsoft.azure.toolkit.intellij.connector.dotazure;

import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.microsoft.azure.toolkit.lib.common.messager.ExceptionNotification;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;

import javax.annotation.Nonnull;

public class DotEnvRunConfigurationExtension extends RunConfigurationExtension {

    @Override
    @ExceptionNotification
    @AzureOperation(name = "user/dotazure.load_env.config", params = {"config.getName()"})
    public <T extends RunConfigurationBase<?>> void updateJavaParameters(@Nonnull T config, @Nonnull JavaParameters params, RunnerSettings s) {
        final AzureModule module = AzureModule.from(config);
        if (module.isValid()) {
            module.loadDefaultEnv().forEach(p -> params.addEnv(p.getKey(), p.getValue()));
        }
        config.getBeforeRunTasks().stream()
            .filter(t -> t instanceof DotEnvBeforeRunTaskProvider.LoadDotEnvBeforeRunTask)
            .map(t -> (DotEnvBeforeRunTaskProvider.LoadDotEnvBeforeRunTask) t)
            .flatMap(t -> t.loadEnv().stream())
            .forEach(p -> params.addEnv(p.getKey(), p.getValue()));
    }

    @Override
    public boolean isApplicableFor(@Nonnull RunConfigurationBase<?> configuration) {
        return AzureModule.from(configuration).isValid() || configuration.getBeforeRunTasks().stream().anyMatch(t -> t instanceof DotEnvBeforeRunTaskProvider.LoadDotEnvBeforeRunTask);
    }
}
