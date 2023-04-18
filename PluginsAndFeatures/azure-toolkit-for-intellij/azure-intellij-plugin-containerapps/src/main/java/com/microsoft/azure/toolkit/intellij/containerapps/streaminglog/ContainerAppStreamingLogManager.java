package com.microsoft.azure.toolkit.intellij.containerapps.streaminglog;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.AppStreamingLogConsoleView;
import com.microsoft.azure.toolkit.intellij.common.StreamingLogsToolWindowManager;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerApp;
import com.microsoft.azure.toolkit.lib.containerapps.environment.ContainerAppsEnvironment;
import reactor.core.publisher.Flux;

import java.util.Objects;
import java.util.Optional;

public class ContainerAppStreamingLogManager {
    private static final ContainerAppStreamingLogManager instance = new ContainerAppStreamingLogManager();
    public static ContainerAppStreamingLogManager getInstance() {
        return instance;
    }

    public void showConsoleStreamingLog(Project project, ContainerApp app, String revisionName, String replicaName, String containerName) {
        AzureTaskManager.getInstance().runLater(() ->
                showStreamingLog(project, app, ContainerApp.LOG_TYPE_CONSOLE, revisionName, replicaName, containerName));
    }

    public void showSystemStreamingLog(Project project, ContainerApp app) {
        AzureTaskManager.getInstance().runLater(() ->
                showStreamingLog(project, app, ContainerApp.LOG_TYPE_SYSTEM, null, null, null));
    }

    public void showEnvStreamingLog(Project project, ContainerAppsEnvironment appsEnvironment) {
        AzureTaskManager.getInstance().runLater(() ->
                showStreamingLog(project, appsEnvironment, null, null, null, null));
    }

    public void closeStreamingLog(Project project, String resourceId) {
        final AppStreamingLogConsoleView consoleView = StreamingLogsToolWindowManager.getInstance().getToolWindowContent(project, resourceId);
        if (Objects.isNull(consoleView) || !consoleView.isActive()) {
            AzureTaskManager.getInstance().runLater(() -> AzureMessager.getMessager().warning("Streaming log is not started."));
            return;
        }
        consoleView.closeStreamingLog();
    }

    public boolean isStreamingLogStarted(Project project, String resourceId) {
        final AppStreamingLogConsoleView consoleView = StreamingLogsToolWindowManager.getInstance().getToolWindowContent(project, resourceId);
        return Optional.ofNullable(consoleView).map(AppStreamingLogConsoleView::isActive).orElse(false);
    }

    private void showStreamingLog(Project project, AzResource app, String logType, String revisionName,
                                  String replicaName, String containerName) {
        final String resourceId;
        final String resourceName;
        if (Objects.equals(logType, ContainerApp.LOG_TYPE_CONSOLE)) {
            resourceName = String.format("%s-%s", replicaName, containerName);
            resourceId = String.format("%s/revisionManagement/%s", app.getId(), resourceName);
        } else {
            resourceId = app.getId();
            resourceName = app.getName();
        }
        final AppStreamingLogConsoleView consoleView = Optional.ofNullable(StreamingLogsToolWindowManager.getInstance()
                .getToolWindowContent(project, resourceId)).orElse(new AppStreamingLogConsoleView(project));
        AzureTaskManager.getInstance().runInBackground(new AzureTask<>(project, AzureString.fromString("open streaming logs"), false, () -> {
            try {
                final Flux<String> log;
                // refer to https://learn.microsoft.com/en-us/azure/container-apps/log-streaming?tabs=bash#view-log-streams-via-the-azure-cli
                // tail lines must be between 0 and 300, default is 20
                if (app instanceof ContainerApp) {
                    log = ((ContainerApp) app).streamingLogs(logType, revisionName, replicaName, containerName, true, 20);
                } else if (app instanceof ContainerAppsEnvironment) {
                    log = ((ContainerAppsEnvironment) app).streamingLogs(true, 20);
                } else {
                    return;
                }
                consoleView.startStreamingLog(log);
                AzureTaskManager.getInstance().runLater(() ->
                        StreamingLogsToolWindowManager.getInstance().showStreamingLogConsole(project, resourceId, resourceName, consoleView)
                );
            } catch (final AzureToolkitRuntimeException e) {
                throw e;
            } catch (final Throwable e) {
                throw new AzureToolkitRuntimeException("failed to open streaming log", e);
            }
        }));
    }

}
