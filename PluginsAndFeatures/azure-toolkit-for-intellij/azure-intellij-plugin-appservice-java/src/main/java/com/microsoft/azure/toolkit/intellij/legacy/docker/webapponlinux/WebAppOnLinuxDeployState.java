/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.docker.webapponlinux;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.RunProcessHandler;
import com.microsoft.azure.toolkit.intellij.container.Constant;
import com.microsoft.azure.toolkit.intellij.container.DockerUtil;
import com.microsoft.azure.toolkit.intellij.legacy.common.AzureRunProfileState;
import com.microsoft.azure.toolkit.lib.appservice.AppServiceAppBase;
import com.microsoft.azure.toolkit.lib.appservice.webapp.WebApp;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azuretools.core.mvp.model.webapp.AzureWebAppMvpModel;
import com.microsoft.azuretools.core.mvp.model.webapp.PrivateRegistryImageSetting;
import com.microsoft.azuretools.core.mvp.model.webapp.WebAppOnLinuxDeployModel;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.Operation;
import com.microsoft.azuretools.telemetrywrapper.TelemetryManager;
import com.microsoft.intellij.util.MavenRunTaskUtil;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class WebAppOnLinuxDeployState extends AzureRunProfileState<AppServiceAppBase<?, ?, ?>> {
    private final WebAppOnLinuxDeployModel deployModel;

    public WebAppOnLinuxDeployState(Project project, WebAppOnLinuxDeployModel webAppOnLinuxDeployModel) {
        super(project);
        this.deployModel = webAppOnLinuxDeployModel;
    }

    @Override
    @AzureOperation(name = "platform/docker.deploy_image")
    public AppServiceAppBase<?, ?, ?> executeSteps(@Nonnull RunProcessHandler processHandler, @Nonnull Operation operation) throws Exception {
        processHandler.setText("Starting job ...  ");
        final String basePath = project.getBasePath();
        if (basePath == null) {
            processHandler.println("Project base path is null.", ProcessOutputTypes.STDERR);
            throw new FileNotFoundException("Project base path is null.");
        }
        // locate artifact to specified location
        final String targetFilePath = deployModel.getTargetPath();
        processHandler.setText(String.format("Locating artifact ... [%s]", targetFilePath));

        // validate dockerfile
        final Path targetDockerfile = Paths.get(deployModel.getDockerFilePath());
        processHandler.setText(String.format("Validating dockerfile ... [%s]", targetDockerfile));
        if (!targetDockerfile.toFile().exists()) {
            throw new FileNotFoundException("Dockerfile not found.");
        }
        processHandler.setText(String.format("dockerfile [%s] is located and validated.", targetDockerfile));
        // replace placeholder if exists
        String content = new String(Files.readAllBytes(targetDockerfile));
        content = content.replaceAll(Constant.DOCKERFILE_ARTIFACT_PLACEHOLDER,
            Paths.get(basePath).toUri().relativize(Paths.get(targetFilePath).toUri()).getPath()
        );
        Files.write(targetDockerfile, content.getBytes());

        // build image
        final PrivateRegistryImageSetting acrInfo = deployModel.getPrivateRegistryImageSetting();
        processHandler.setText(String.format("Building image ...  [%s]", acrInfo.getImageTagWithServerUrl()));
        DockerUtil.ping();
        DockerUtil.buildImage(
            acrInfo.getImageTagWithServerUrl(),
            targetDockerfile.toFile(),
            targetDockerfile.getParent().toFile());
        processHandler.setText(String.format("Image [%s] is built successfully.", acrInfo.getImageTagWithServerUrl()));

        // push to ACR
        processHandler.setText(String.format("Pushing to ACR ... [%s] ", acrInfo.getServerUrl()));
        DockerUtil.pushImage(acrInfo.getServerUrl(), acrInfo.getUsername(), acrInfo.getPassword(),
            acrInfo.getImageTagWithServerUrl());
        processHandler.setText(String.format("[%s] is pushed to ACR successfully.", acrInfo.getServerUrl()));

        // deploy
        if (deployModel.isCreatingNewWebAppOnLinux()) {
            // create new WebApp
            processHandler.setText(String.format("Creating new WebApp ... [%s]", deployModel.getWebAppName()));
            final WebApp app = AzureWebAppMvpModel.getInstance().createAzureWebAppWithPrivateRegistryImage(deployModel);
            if (app != null) {
                processHandler.setText(String.format("WebApp [%s] is created successfully.", app.getName()));
                processHandler.setText(String.format("URL:  https://%s.azurewebsites.net/", app.getName()));
                updateConfigurationDataModel(app);
            }
            return app;
        } else {
            // update WebApp
            processHandler.setText(String.format("Updating WebApp ... [%s]",
                deployModel.getWebAppName()));
            final WebApp app = AzureWebAppMvpModel.getInstance().updateWebAppOnDocker(deployModel.getWebAppId(), acrInfo);
            if (app != null) {
                processHandler.setText(String.format("WebApp [%s] is updated successfully.", app.getName()));
                processHandler.setText(String.format("URL:  https://%s.azurewebsites.net/", app.getName()));
            }
            return app;
        }
    }

    @Override
    protected Operation createOperation() {
        return TelemetryManager.createOperation(TelemetryConstants.WEBAPP, TelemetryConstants.DEPLOY_WEBAPP_CONTAINER);
    }

    @Override
    @AzureOperation(name = "boundary/webapp.complete_deployment.app", params = {"this.deployModel.getWebAppName()"})
    protected void onSuccess(AppServiceAppBase<?, ?, ?> result, @Nonnull RunProcessHandler processHandler) {
        processHandler.notifyComplete();
    }

    protected Map<String, String> getTelemetryMap() {
        final Map<String, String> telemetryMap = new HashMap<>();
        telemetryMap.put("SubscriptionId", deployModel.getSubscriptionId());
        telemetryMap.put("CreateNewApp", String.valueOf(deployModel.isCreatingNewWebAppOnLinux()));
        telemetryMap.put("CreateNewSP", String.valueOf(deployModel.isCreatingNewAppServicePlan()));
        telemetryMap.put("CreateNewRGP", String.valueOf(deployModel.isCreatingNewResourceGroup()));
        String fileType = "";
        if (null != deployModel.getTargetName()) {
            fileType = MavenRunTaskUtil.getFileType(deployModel.getTargetName());
        }
        telemetryMap.put("FileType", fileType);
        return telemetryMap;
    }

    private void updateConfigurationDataModel(WebApp app) {
        deployModel.setCreatingNewWebAppOnLinux(false);
        deployModel.setWebAppId(app.getId());
        deployModel.setResourceGroupName(app.getResourceGroupName());
    }
}
