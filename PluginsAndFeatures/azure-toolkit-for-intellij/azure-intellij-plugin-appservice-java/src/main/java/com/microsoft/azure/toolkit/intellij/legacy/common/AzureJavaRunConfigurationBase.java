package com.microsoft.azure.toolkit.intellij.legacy.common;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class AzureJavaRunConfigurationBase<T> extends AzureRunConfigurationBase<T> {
    protected JavaRunConfigurationModule myModule;

    protected AzureJavaRunConfigurationBase(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    public JavaRunConfigurationModule getConfigurationModule() {
        return myModule;
    }
}
