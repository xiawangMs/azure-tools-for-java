/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.springcloud.streaminglog;

import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.AppStreamingLogConsoleView;
import com.microsoft.azure.toolkit.intellij.common.StreamingLogsToolWindowManager;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.operation.OperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudDeployment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SpringCloudStreamingLogManager {

    private final Map<String, AppStreamingLogConsoleView> consoleViewMap = new HashMap<>();

    public static SpringCloudStreamingLogManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void showStreamingLog(Project project, SpringCloudApp app, String instanceName) {
        final AppStreamingLogConsoleView consoleView = consoleViewMap.computeIfAbsent(
                instanceName, name -> new AppStreamingLogConsoleView(project));
        final AzureString title = OperationBundle.description("internal/springcloud.start_log_stream.instance", instanceName);
        AzureTaskManager.getInstance().runInBackground(new AzureTask<>(project, title, false, () -> {
            try {
                final SpringCloudDeployment deployment = app.getActiveDeployment();
                if (Objects.isNull(deployment)) {
                    throw new AzureToolkitRuntimeException(String.format("No active deployment in current app %s", app.getName()));
                }
                consoleView.startStreamingLog(deployment.streamingLogs(deployment.getParent().getLogStreamingEndpoint(instanceName),
                        ImmutableMap.of("follow", String.valueOf(true),
                                "sinceSeconds", String.valueOf(300),
                                "tailLines", String.valueOf(500),
                                "limitBytes", String.valueOf(1024 * 1024))));
                AzureTaskManager.getInstance().runLater(() ->
                        StreamingLogsToolWindowManager.getInstance().showStreamingLogConsole(project, instanceName, instanceName, consoleView));
            } catch (final Throwable e) {
                AzureMessager.getMessager().error(e.getMessage(), "Failed to start streaming log");
                consoleView.closeStreamingLog();
            }
        }));
    }

    public void closeStreamingLog(String instanceName) {
        final AzureString title = OperationBundle.description("boundary/springcloud.close_log_stream.instance", instanceName);
        AzureTaskManager.getInstance().runInBackground(new AzureTask<>(null, title, false, () -> {
            final AppStreamingLogConsoleView consoleView = consoleViewMap.get(instanceName);
            if (consoleView != null && consoleView.isActive()) {
                consoleView.closeStreamingLog();
            } else {
                AzureMessager.getMessager().error("Log is not started.", "Failed to close streaming log");
            }
        }));
    }

    public void removeConsoleView(String instanceName) {
        consoleViewMap.remove(instanceName);
    }

    private static final class SingletonHolder {
        private static final SpringCloudStreamingLogManager INSTANCE = new SpringCloudStreamingLogManager();

        private SingletonHolder() {
        }
    }
}
