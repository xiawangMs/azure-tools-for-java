/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.dotazure;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.microsoft.azure.toolkit.intellij.connector.Connection;
import com.microsoft.azure.toolkit.intellij.connector.ConnectionManager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MigrateStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@Nonnull Project project) {
        final Map<String, List<Connection<?, ?>>> moduleConnections = project.getService(ConnectionManager.class)
            .getConnections().stream()
            .collect(Collectors.groupingBy(c -> c.getConsumer().getName()));
        final ModuleManager moduleManager = ModuleManager.getInstance(project);
        moduleConnections.forEach((moduleName, connections) -> {
            final Module m = moduleManager.findModuleByName(moduleName);
            if (Objects.nonNull(m)) {
                final AzureModule module = new AzureModule(project, m);
                if (Objects.nonNull(module.getConfigJsonFile())) {
                    return;
                }
                AzureTaskManager.getInstance().write(() -> {
                    try {
                        module.initializeIfNot("default");
                        final String envVariables = connections.stream()
                            .map(c -> generateEnvironmentString(project, c))
                            .collect(Collectors.joining(System.lineSeparator()));
                        module.appendDotEnv("default", envVariables);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
    }

    public static String generateEnvironmentString(@Nonnull final Project project, @Nonnull final Connection<?, ?> connection) {
        final StringBuilder string = new StringBuilder();
        final String dataId = connection.getResource().getDataId();
        string.append("# resource: ").append(dataId).append(System.lineSeparator());
        connection.getEnvironmentVariables(project)
            .forEach((k, v) -> string.append(k).append("=").append(v).append(System.lineSeparator()));
        return string.toString();
    }
}
