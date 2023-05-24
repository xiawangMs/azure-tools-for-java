package com.microsoft.azure.toolkit.intellij.connector;

import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.module.Module;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.AzureModule;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.DotEnvBeforeRunTaskProvider;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.Environment;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public interface IConnectionAware extends RunConfiguration {
    @Deprecated
    default void addConnection(@Nonnull final Connection<?, ?> connection) {}

    default Module getModule() {
        return null;
    }

    @Nonnull
    default List<Connection<?, ?>> getConnections() {
        return AzureModule.createIfSupport(this)
                .map(AzureModule::getEnvironment)
                .map(Environment::getConnections)
                .orElse(Collections.emptyList());
    }

    default DotEnvBeforeRunTaskProvider.LoadDotEnvBeforeRunTask getLoadDotEnvBeforeRunTask() {
        return (DotEnvBeforeRunTaskProvider.LoadDotEnvBeforeRunTask) this.getBeforeRunTasks().stream()
                .filter(task -> task instanceof DotEnvBeforeRunTaskProvider.LoadDotEnvBeforeRunTask).findAny().orElse(null);
    }

    default boolean isConnectionEnabled() {
        return Objects.nonNull(getLoadDotEnvBeforeRunTask());
    }
}
