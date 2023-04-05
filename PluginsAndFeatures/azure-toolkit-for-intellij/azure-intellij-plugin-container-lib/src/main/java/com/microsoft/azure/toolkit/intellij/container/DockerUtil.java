/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PushImageCmd;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.intellij.openapi.util.io.FileUtil;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;


public class DockerUtil {
    public static void createDockerFile(String baseDir, String filename, String content) throws IOException {
        File dockerFile = Paths.get(baseDir, filename).toFile();
        if (!dockerFile.exists()) {
            FileUtil.writeToFile(dockerFile, content);
        }
    }

    public static String createContainer(@Nonnull DockerClient docker, @Nonnull String imageNameWithTag, String port) throws DockerException {
        final CreateContainerCmd cmd = docker.createContainerCmd(imageNameWithTag)
            .withExposedPorts(ExposedPort.parse(port));
        final CreateContainerResponse container = cmd.exec();
        return container.getId();
    }

    public static Container runContainer(@Nonnull DockerClient docker, @Nonnull String containerId) throws DockerException {
        docker.startContainerCmd(containerId).exec();
        final List<Container> containers = docker.listContainersCmd().exec();
        return containers.stream().filter(item -> item.getId().equals(containerId)).findFirst()
            .orElseThrow(() -> new DockerException("Error in starting container.", 404));
    }

    @AzureOperation(name = "boundary/docker.build_image.image|dir", params = {"imageNameWithTag", "baseDir"})
    public static String buildImage(@Nonnull DockerClient docker, String imageNameWithTag, @Nonnull File dockerFile, File baseDir)
        throws DockerException {
        BuildImageResultCallback callback = new BuildImageResultCallback() {
        };
        final String imageId = docker.buildImageCmd()
            .withDockerfile(dockerFile)
            .withBaseDirectory(baseDir)
            .withTags(Set.of(imageNameWithTag))
            .exec(callback).awaitImageId();
        return imageId == null ? null : imageNameWithTag;
    }

    @AzureOperation(name = "boundary/docker.push_image.image|registry", params = {"targetImageName", "registryUrl"})
    public static void pushImage(@Nonnull DockerClient dockerClient, @Nonnull String registryUrl, String username, String password, @Nonnull String targetImageName)
        throws DockerException, InterruptedException {
        if (targetImageName.startsWith(registryUrl)) {
            final AuthConfig authConfig = new AuthConfig().withUsername(username).withPassword(password).withRegistryAddress(registryUrl);
            final PushImageCmd cmd = dockerClient.pushImageCmd(targetImageName).withAuthConfig(authConfig);
            cmd.exec(new ResultCallback.Adapter<>()).awaitCompletion();
        } else {
            throw new DockerException("serverUrl and imageName mismatch.", 400);
        }
    }

    @AzureOperation(name = "boundary/docker.pull_image.image|registry", params = {"targetImageName", "registryUrl"})
    public static void pullImage(@Nonnull DockerClient dockerClient, @Nonnull String registryUrl, String username, String password, @Nonnull String targetImageName)
        throws DockerException, InterruptedException {
        if (targetImageName.startsWith(registryUrl)) {
            final AuthConfig authConfig = new AuthConfig().withUsername(username).withPassword(password).withRegistryAddress(registryUrl);
            final PullImageCmd cmd = dockerClient.pullImageCmd(registryUrl).withTag(targetImageName).withAuthConfig(authConfig);
            cmd.exec(new ResultCallback.Adapter<>()).awaitCompletion();
        } else {
            throw new DockerException("serverUrl and imageName mismatch.", 400);
        }
    }

    public static void stopContainer(@Nonnull DockerClient dockerClient, @Nonnull String containerId) throws DockerException {
        dockerClient.stopContainerCmd(containerId).exec();
        dockerClient.removeContainerCmd(containerId).exec();
    }

    public static DockerClient getDockerClient(String dockerHost, boolean tlsEnabled, String certPath) {
        final DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerHost(dockerHost);
        if (tlsEnabled) {
            builder.withDockerCertPath(certPath).build();
        }
        return DockerClientBuilder.getInstance(builder.build()).build();

    }

    /**
     * check if the default docker file exists.
     * If yes, return the path as a String.
     * Else return an empty String.
     */
    public static String getDefaultDockerFilePathIfExist(String basePath) {
        try {
            if (!StringUtils.isEmpty(basePath)) {
                Path targetDockerfile = Paths.get(basePath, Constant.DOCKERFILE_NAME);
                if (targetDockerfile.toFile().exists()) {
                    return targetDockerfile.toString();
                }
            }
        } catch (RuntimeException ignored) {
        }
        return "";
    }

    @AzureOperation(name = "boundary/docker.ping_host")
    public static void ping(DockerClient docker) {
        try {
            docker.pingCmd().exec();
        } catch (DockerException e) {
            throw new AzureToolkitRuntimeException("Failed to connect docker server, \nIs Docker installed and running?");
        }
    }
}
