/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.serviceexplorer.azure.appservice;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.microsoft.azuretools.azurecommons.helpers.AzureCmdException;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import com.microsoft.intellij.helpers.AppServiceStreamingLogManager;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.Name;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.function.FunctionNode;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.WebAppNode;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.deploymentslot.DeploymentSlotNode;
import org.jetbrains.annotations.NotNull;

import static com.microsoft.azuretools.telemetry.TelemetryConstants.*;

@Name("Start Streaming Logs")
public class StartStreamingLogsAction extends NodeActionListener {

    private Project project;
    private String resourceId;
    private String service;
    private String operation;

    public StartStreamingLogsAction(WebAppNode webAppNode) {
        super();
        this.project = (Project) webAppNode.getProject();
        this.resourceId = webAppNode.getId();
        this.service = WEBAPP;
        this.operation = START_STREAMING_LOG_WEBAPP;
    }

    public StartStreamingLogsAction(DeploymentSlotNode deploymentSlotNode) {
        super();
        this.project = (Project) deploymentSlotNode.getProject();
        this.resourceId = deploymentSlotNode.getId();
        this.service = WEBAPP;
        this.operation = START_STREAMING_LOG_WEBAPP_SLOT;
    }

    public StartStreamingLogsAction(FunctionNode functionNode) {
        super();
        this.project = (Project) functionNode.getProject();
        this.resourceId = functionNode.getId();
        this.service = FUNCTION;
        this.operation = START_STREAMING_LOG_FUNCTION_APP;
    }

    @Override
    protected void actionPerformed(NodeActionEvent nodeActionEvent) throws AzureCmdException {
        EventUtil.executeWithLog(service, operation, op -> {
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Start Streaming Logs", true) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    switch (operation) {
                        case START_STREAMING_LOG_FUNCTION_APP:
                            AppServiceStreamingLogManager.INSTANCE.showFunctionStreamingLog(project, resourceId);
                            break;
                        case START_STREAMING_LOG_WEBAPP:
                            AppServiceStreamingLogManager.INSTANCE.showWebAppStreamingLog(project, resourceId);
                            break;
                        case START_STREAMING_LOG_WEBAPP_SLOT:
                            AppServiceStreamingLogManager.INSTANCE.showWebAppDeploymentSlotStreamingLog(project,
                                                                                                        resourceId);
                            break;
                        default:
                            DefaultLoader.getUIHelper().showError("Unsupported operation", "Unsupported operation");
                            break;
                    }
                }
            });
        });
    }
}
