/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.storage.azurite;

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunManagerListener;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.connector.Connection;
import com.microsoft.azure.toolkit.intellij.connector.ConnectionManager;
import com.microsoft.azure.toolkit.intellij.connector.ConnectionTopics;
import com.microsoft.azure.toolkit.intellij.storage.connection.StorageAccountResourceDefinition;
import com.microsoft.azure.toolkit.lib.common.messager.ExceptionNotification;
import com.microsoft.azure.toolkit.lib.storage.AzuriteStorageAccount;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

// todo: Remove duplicates with com.microsoft.azure.toolkit.intellij.connector.BeforeRunTaskAdder
public class AzuriteTaskAdder implements RunManagerListener, ConnectionTopics.ConnectionChanged {
    @Override
    @ExceptionNotification
    public void runConfigurationAdded(@Nonnull RunnerAndConfigurationSettings settings) {
        final RunConfiguration config = settings.getConfiguration();
        final List<BeforeRunTask<?>> tasks = config.getBeforeRunTasks();
        final List<Connection<?, ?>> azuriteConnections = config.getProject().getService(ConnectionManager.class).getConnections().stream()
                .filter(c -> c.isApplicableFor(config) && isAzuriteResourceConnection(c)).toList();
        final List<BeforeRunTask<?>> azuriteTasks = tasks.stream().filter(t -> t instanceof AzuriteTaskProvider.AzuriteBeforeRunTask).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(azuriteConnections) && CollectionUtils.isEmpty(azuriteTasks)) {
            config.getBeforeRunTasks().add(new AzuriteTaskProvider.AzuriteBeforeRunTask());
        }
    }

    @Override
    public void connectionChanged(Project project, Connection<?, ?> connection, ConnectionTopics.Action action) {
        if (!isAzuriteResourceConnection(connection)) {
            return;
        }
        final RunManagerEx rm = RunManagerEx.getInstanceEx(project);
        final List<RunConfiguration> configurations = rm.getAllConfigurationsList();
        for (final RunConfiguration config : configurations) {
            final List<BeforeRunTask<?>> tasks = config.getBeforeRunTasks();
            if (action == ConnectionTopics.Action.ADD) {
                if (connection.isApplicableFor(config) && tasks.stream().noneMatch(t -> t instanceof AzuriteTaskProvider.AzuriteBeforeRunTask)) {
                    tasks.add(new AzuriteTaskProvider.AzuriteBeforeRunTask());
                }
            } else if (action == ConnectionTopics.Action.REMOVE) {
                final boolean isAzuriteConnectionExists = config.getProject().getService(ConnectionManager.class).getConnections()
                        .stream().anyMatch(c -> c.isApplicableFor(config) && isAzuriteResourceConnection(c));
                if (!isAzuriteConnectionExists) {
                    tasks.removeIf(t -> t instanceof AzuriteTaskProvider.AzuriteBeforeRunTask);
                }
            }
        }
    }

    public static boolean isAzuriteResourceConnection(@Nonnull final Connection<?, ?> connection) {
        return connection.getDefinition().getResourceDefinition() instanceof StorageAccountResourceDefinition && connection.getResource().getData() instanceof AzuriteStorageAccount;
    }
}
