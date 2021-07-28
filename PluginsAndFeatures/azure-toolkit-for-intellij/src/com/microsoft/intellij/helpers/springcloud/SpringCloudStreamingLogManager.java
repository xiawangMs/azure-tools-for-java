/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.helpers.springcloud;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.microsoft.azuretools.core.mvp.model.springcloud.AzureSpringCloudMvpModel;
import com.microsoft.intellij.helpers.ConsoleViewStatus;
import com.microsoft.intellij.helpers.StreamingLogsToolWindowManager;
import com.microsoft.intellij.util.PluginUtil;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import org.apache.http.HttpException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.intellij.helpers.ConsoleViewStatus.ACTIVE;
import static com.microsoft.intellij.helpers.ConsoleViewStatus.STOPPED;

public class SpringCloudStreamingLogManager {

    private Map<String, SpringCloudStreamingLogConsoleView> consoleViewMap = new HashMap<>();

    public static SpringCloudStreamingLogManager getInstance() {
        return SpringCloudStreamingLogManager.SingletonHolder.INSTANCE;
    }

    public void showStreamingLog(Project project, String appId, String instanceName) {
        final SpringCloudStreamingLogConsoleView consoleView = consoleViewMap.computeIfAbsent(
                instanceName, name -> new SpringCloudStreamingLogConsoleView(project, name));
        DefaultLoader.getIdeHelper().runInBackground(project, "Starting Streaming Log...", false, true, null, () -> {
            try {
                consoleView.startLog(() -> {
                    try {
                        return AzureSpringCloudMvpModel.getLogStream(appId, instanceName, 0, 10, 0, true);
                    } catch (IOException | HttpException e) {
                        return null;
                    }
                });
                StreamingLogsToolWindowManager
                        .getInstance()
                        .showStreamingLogConsole(project, instanceName, instanceName, consoleView);
            } catch (Throwable e) {
                ApplicationManager.getApplication().invokeLater(() -> PluginUtil.displayErrorDialog(
                        "Failed to start streaming log",
                        e.getMessage()));
                consoleView.shutdown();
            }
        });
    }

    public void closeStreamingLog(String instanceName) {
        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Closing Streaming Log...", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                final SpringCloudStreamingLogConsoleView consoleView = consoleViewMap.get(instanceName);
                if (consoleView != null && consoleView.getStatus() == ACTIVE) {
                    consoleView.shutdown();
                } else {
                    ApplicationManager.getApplication().invokeLater(() -> PluginUtil.displayErrorDialog(
                            "Failed to close streaming log", "Log is not started."));
                }
            }
        });
    }

    public void removeConsoleView(String instanceName) {
        consoleViewMap.remove(instanceName);
    }

    public ConsoleViewStatus getConsoleViewStatus(String instanceName) {
        return consoleViewMap.containsKey(instanceName) ? consoleViewMap.get(instanceName).getStatus() : STOPPED;
    }

    private static final class SingletonHolder {
        private static final SpringCloudStreamingLogManager INSTANCE = new SpringCloudStreamingLogManager();

        private SingletonHolder() {
        }
    }
}
