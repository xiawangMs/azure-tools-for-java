/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.docker.pushimage;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.RunProcessHandler;
import com.microsoft.azure.toolkit.intellij.legacy.common.AzureRunProfileState;
import com.microsoft.azure.toolkit.intellij.legacy.docker.utils.Constant;
import com.microsoft.azure.toolkit.intellij.legacy.docker.utils.DockerUtil;
import com.microsoft.azuretools.core.mvp.model.container.pojo.PushImageRunModel;
import com.microsoft.azuretools.core.mvp.model.webapp.PrivateRegistryImageSetting;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.Operation;
import com.microsoft.azuretools.telemetrywrapper.TelemetryManager;
import com.microsoft.intellij.util.MavenRunTaskUtil;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

public class PushImageRunState extends AzureRunProfileState<String> {
    private final PushImageRunModel dataModel;

    public PushImageRunState(Project project, PushImageRunModel pushImageRunModel) {
        super(project);
        this.dataModel = pushImageRunModel;
    }

    @Override
    public String executeSteps(@Nonnull RunProcessHandler processHandler, @Nonnull Operation operation) throws Exception {
        processHandler.setText("Starting job ...  ");
        String basePath = project.getBasePath();
        if (basePath == null) {
            processHandler.println("Project base path is null.", ProcessOutputTypes.STDERR);
            throw new FileNotFoundException("Project base path is null.");
        }
        // locate artifact to specified location
        String targetFilePath = dataModel.getTargetPath();
        processHandler.setText(String.format("Locating artifact ... [%s]", targetFilePath));

        // validate dockerfile
        Path targetDockerfile = Paths.get(dataModel.getDockerFilePath());
        processHandler.setText(String.format("Validating dockerfile ... [%s]", targetDockerfile));
        if (!targetDockerfile.toFile().exists()) {
            throw new FileNotFoundException("Dockerfile not found.");
        }
        // replace placeholder if exists
        String content = new String(Files.readAllBytes(targetDockerfile));
        content = content.replaceAll(Constant.DOCKERFILE_ARTIFACT_PLACEHOLDER,
            Paths.get(basePath).toUri().relativize(Paths.get(targetFilePath).toUri()).getPath()
        );
        Files.write(targetDockerfile, content.getBytes());

        // build image
        PrivateRegistryImageSetting acrInfo = dataModel.getPrivateRegistryImageSetting();
        processHandler.setText(String.format("Building image ...  [%s]", acrInfo.getImageTagWithServerUrl()));
        DockerClient docker = DockerClientBuilder.getInstance().build();
        DockerUtil.ping(docker);
        String image = DockerUtil.buildImage(docker,
            acrInfo.getImageTagWithServerUrl(),
            targetDockerfile.toFile(),
            targetDockerfile.getParent().toFile()
        );

        // push to ACR
        processHandler.setText(String.format("Pushing to ACR ... [%s] ", acrInfo.getServerUrl()));
        DockerUtil.pushImage(docker, acrInfo.getServerUrl(), acrInfo.getUsername(), acrInfo.getPassword(),
            acrInfo.getImageTagWithServerUrl());

        return image;
    }

    @Nonnull
    @Override
    protected Operation createOperation() {
        return TelemetryManager.createOperation(TelemetryConstants.ACR, TelemetryConstants.ACR_PUSHIMAGE);
    }

    @Override
    protected void onSuccess(String image, @Nonnull RunProcessHandler processHandler) {
        processHandler.setText("pushed.");
        processHandler.notifyComplete();
    }

    @Override
    protected Map<String, String> getTelemetryMap() {
        final String fileType = dataModel.getTargetName() == null ? StringUtils.EMPTY : MavenRunTaskUtil.getFileType(dataModel.getTargetName());
        return Collections.singletonMap(TelemetryConstants.FILETYPE, fileType);
    }
}
