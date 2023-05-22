/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.dotazure;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.microsoft.azure.toolkit.intellij.connector.Connection;
import com.microsoft.azure.toolkit.intellij.connector.ConnectionManager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MigrateStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@Nonnull Project project) {
        final ModuleManager moduleManager = ModuleManager.getInstance(project);
        final Map<String, List<Connection<?, ?>>> moduleConnections = project.getService(ConnectionManager.class)
            .getConnections().stream()
            .collect(Collectors.groupingBy(c -> c.getConsumer().getName()));
        moduleConnections.forEach((moduleName, connections) -> {
            Optional.ofNullable(moduleManager.findModuleByName(moduleName))
                .map(AzureModule::new).filter(m -> !m.isInitialized())
                .ifPresent(module -> AzureTaskManager.getInstance().write(() -> {
                    final Environment env = module.initialize().getEnvironment();
                    connections.forEach(env::addConnection);
                }));
        });
    }
}
