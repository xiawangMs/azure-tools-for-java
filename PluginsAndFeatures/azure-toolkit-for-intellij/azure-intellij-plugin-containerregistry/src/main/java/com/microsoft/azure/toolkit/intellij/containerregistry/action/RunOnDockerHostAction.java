/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerregistry.action;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.RunDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.IntelliJAzureIcons;
import com.microsoft.azure.toolkit.intellij.common.action.AzureAnAction;
import com.microsoft.azure.toolkit.intellij.container.model.DockerImage;
import com.microsoft.azure.toolkit.intellij.containerregistry.AzureDockerSupportConfigurationType;
import com.microsoft.azure.toolkit.intellij.containerregistry.dockerhost.DockerHostRunConfiguration;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.Operation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class RunOnDockerHostAction extends AzureAnAction {
    private static final String DIALOG_TITLE = "Run on Docker Host";
    private static final AzureDockerSupportConfigurationType configType = AzureDockerSupportConfigurationType.getInstance();
    private final DockerImage dockerImage;

    public RunOnDockerHostAction() {
        this(null);
    }

    public RunOnDockerHostAction(@Nullable final DockerImage dockerImage) {
        super(DIALOG_TITLE, "Build image and run in local docker host", IntelliJAzureIcons.getIcon("/icons/DockerSupport/Run.svg"));
        this.dockerImage = dockerImage;
    }

    @Override
    public boolean onActionPerformed(@Nonnull AnActionEvent event, @Nullable Operation operation) {
        final Project project = event.getProject();
        if (project == null) {
            return true;
        }
        AzureTaskManager.getInstance().runLater(() -> runConfiguration(project));
        return true;
    }

    @Override
    protected String getServiceName(AnActionEvent event) {
        return TelemetryConstants.WEBAPP;
    }

    @Override
    protected String getOperationName(AnActionEvent event) {
        return TelemetryConstants.DEPLOY_WEBAPP_DOCKERHOST;
    }

    @SuppressWarnings({"Duplicates"})
    private void runConfiguration(Project project) {
        final RunnerAndConfigurationSettings settings = getOrCreateConfigurationSettings(project);
        final RunConfiguration configuration = settings.getConfiguration();
        if (configuration instanceof DockerHostRunConfiguration) {
            Optional.ofNullable(dockerImage).ifPresent(image -> ((DockerHostRunConfiguration) configuration).setDockerImage(image));
        }
        if (RunDialog.editConfiguration(project, settings, DIALOG_TITLE, DefaultRunExecutor.getRunExecutorInstance())) {
            final RunManagerEx manager = RunManagerEx.getInstanceEx(project);
            settings.storeInLocalWorkspace();
            manager.addConfiguration(settings);
            manager.setSelectedConfiguration(settings);
            ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance());
        }
    }

    @Nonnull
    private static RunnerAndConfigurationSettings getOrCreateConfigurationSettings(@Nonnull Project project) {
        final RunManagerEx manager = RunManagerEx.getInstanceEx(project);
        final ConfigurationFactory factory = configType.getDockerHostRunConfigurationFactory();
        final String configurationName = String.format("%s: %s", factory.getName(), project.getName());
        final RunnerAndConfigurationSettings existed = manager.findConfigurationByName(configurationName);
        return Objects.nonNull(existed) ? existed : manager.createConfiguration(configurationName, factory);
    }
}
