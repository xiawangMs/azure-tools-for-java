/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.container;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PushImageCmd;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import com.intellij.openapi.util.io.FileUtil;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.containerregistry.Tag;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class DockerUtil {
    public static DockerClient getDefaultDockerClient() {
        final DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        final DockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .build();
        return DockerClientBuilder.getInstance(config).withDockerHttpClient(httpClient).build();
    }

    public static ObjectMapper getDefaultObjectMapper() {
        return DockerClientConfig.getDefaultObjectMapper();
    }

    public static DockerClient getDockerClient(String dockerHost, boolean tlsEnabled, String certPath) {
        final DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerHost(dockerHost);
        if (tlsEnabled) {
            builder.withDockerCertPath(certPath).build();
        }
        final DefaultDockerClientConfig config = builder.build();
        final ZerodepDockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .build();
        return DockerClientBuilder.getInstance(config).withDockerHttpClient(httpClient).build();
    }

    public static void createDockerFile(String baseDir, String filename, String content) throws IOException {
        final File dockerFile = Paths.get(baseDir, filename).toFile();
        if (!dockerFile.exists()) {
            FileUtil.writeToFile(dockerFile, content);
        }
    }

    public static String createContainer(@Nonnull String imageNameWithTag, String port) throws DockerException {
        return createContainer(getDefaultDockerClient(), imageNameWithTag, port);
    }

    public static String createContainer(@Nonnull DockerClient docker, @Nonnull String imageNameWithTag, String port) throws DockerException {
        final CreateContainerCmd cmd = docker.createContainerCmd(imageNameWithTag)
            .withExposedPorts(ExposedPort.parse(port));
        final CreateContainerResponse container = cmd.exec();
        return container.getId();
    }

    public static Container runContainer(@Nonnull String containerId) throws DockerException {
        return runContainer(getDefaultDockerClient(), containerId);
    }

    public static Container runContainer(@Nonnull DockerClient docker, @Nonnull String containerId) throws DockerException {
        docker.startContainerCmd(containerId).exec();
        final List<Container> containers = docker.listContainersCmd().exec();
        return containers.stream().filter(item -> item.getId().equals(containerId)).findFirst()
            .orElseThrow(() -> new DockerException("Error in starting container.", 404));
    }

    public static String buildImage(String imageNameWithTag, @Nonnull File dockerFile, File baseDir) throws DockerException {
        return buildImage(getDefaultDockerClient(), imageNameWithTag, dockerFile, baseDir);
    }

    @Nullable
    @AzureOperation(name = "boundary/docker.build_image.image|dir", params = {"imageNameWithTag", "baseDir"})
    public static String buildImage(@Nonnull DockerClient docker, String imageNameWithTag, @Nonnull File dockerFile, File baseDir)
        throws DockerException {
        final BuildImageResultCallback callback = new BuildImageResultCallback() {
        };
        final String imageId = docker.buildImageCmd()
            .withDockerfile(dockerFile)
            .withBaseDirectory(baseDir)
            .withTags(Set.of(imageNameWithTag))
            .exec(callback).awaitImageId();
        return imageId == null ? null : imageNameWithTag;
    }

    public static void pushImage(@Nonnull String registryUrl, String username, String password, @Nonnull String targetImageName)
        throws DockerException, InterruptedException {
        pushImage(getDefaultDockerClient(), registryUrl, username, password, targetImageName);
    }

    @AzureOperation(name = "boundary/docker.push_image.image|registry", params = {"targetImageName", "registryUrl"})
    public static void pushImage(@Nonnull DockerClient dockerClient, @Nonnull String registryUrl, String username, String password,
                                 @Nonnull String targetImageName)
        throws DockerException, InterruptedException {
        final AuthConfig authConfig = new AuthConfig().withUsername(username).withPassword(password).withRegistryAddress(registryUrl);
        final PushImageCmd cmd = dockerClient.pushImageCmd(targetImageName).withAuthConfig(authConfig);
        cmd.exec(new ResultCallback.Adapter<>()).awaitCompletion();
    }

    public static void pullImage(@Nonnull String registryUrl, String username, String password,
                                 @Nonnull String repository, @Nonnull String tag)
        throws DockerException, InterruptedException {
        pullImage(getDefaultDockerClient(), registryUrl, username, password, repository, tag);
    }

    @AzureOperation(name = "boundary/docker.pull_image.image|registry", params = {"repository", "registryUrl"})
    public static String pullImage(@Nonnull DockerClient dockerClient, @Nonnull String registryUrl, String username, String password,
                                   @Nonnull String repository, @Nonnull String tag)
        throws DockerException, InterruptedException {
        final AuthConfig authConfig = new AuthConfig().withUsername(username).withPassword(password).withRegistryAddress(registryUrl);
        final String fullRepositoryName = String.format("%s/%s", registryUrl, repository);
        final PullImageCmd cmd = dockerClient.pullImageCmd(fullRepositoryName).withRegistry(registryUrl).withTag(tag).withAuthConfig(authConfig);
        final String[] imageId = new String[1];
        final ResultCallback.Adapter<PullResponseItem> adapter = cmd.exec(new ResultCallback.Adapter<>() {
            @Override
            public void onNext(PullResponseItem object) {
                imageId[0] = object.getId();
            }
        }).awaitCompletion();
        return imageId[0];
    }

    @AzureOperation(name = "boundary/docker.inspect_image.image", params = {"imageId"})
    public static InspectImageResponse inspectImage(String imageId)
        throws DockerException {
        final DockerClient dockerClient = getDefaultDockerClient();
        return dockerClient.inspectImageCmd(imageId).exec();
    }

    public static Image getImage(Tag tag)
        throws DockerException {
        final DockerClient dockerClient = getDefaultDockerClient();
        final List<Image> images = dockerClient.listImagesCmd().exec();
        return images.stream()
            .filter(i -> Arrays.stream(i.getRepoTags()).anyMatch(t -> t.equalsIgnoreCase(tag.getFullName())))
            .findAny().orElse(null);
    }

    public static void stopContainer(@Nonnull String containerId) throws DockerException {
        stopContainer(getDefaultDockerClient(), containerId);
    }

    public static void stopContainer(@Nonnull DockerClient dockerClient, @Nonnull String containerId) throws DockerException {
        dockerClient.stopContainerCmd(containerId).exec();
        dockerClient.removeContainerCmd(containerId).exec();
    }

    /**
     * check if the default docker file exists.
     * If yes, return the path as a String.
     * Else return an empty String.
     */
    public static String getDefaultDockerFilePathIfExist(String basePath) {
        try {
            if (!StringUtils.isEmpty(basePath)) {
                final Path targetDockerfile = Paths.get(basePath, Constant.DOCKERFILE_NAME);
                if (targetDockerfile.toFile().exists()) {
                    return targetDockerfile.toString();
                }
            }
        } catch (final RuntimeException ignored) {
        }
        return "";
    }

    public static void ping() {
        ping(getDefaultDockerClient());
    }

    @AzureOperation(name = "boundary/docker.ping_host")
    public static void ping(DockerClient docker) {
        try {
            docker.pingCmd().exec();
        } catch (final DockerException e) {
            throw new AzureToolkitRuntimeException("Failed to connect docker server, \nIs Docker installed and running?");
        }
    }
}
