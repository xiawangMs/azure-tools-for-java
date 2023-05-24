/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.dotazure;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.intellij.connector.Connection;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import io.github.cdimascio.dotenv.internal.DotenvParser;
import io.github.cdimascio.dotenv.internal.DotenvReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Environment {
    @Nonnull
    private final VirtualFile dotEnvFile;
    @Nonnull
    private final Module module;
    @Nullable
    private ConnectionManager connectionManager;
    @Nullable
    private ResourceManager resourceManager;

    public Environment(@Nonnull VirtualFile dotEnvFile, @Nonnull Module module) {
        this.module = module;
        this.dotEnvFile = dotEnvFile;
        final VirtualFile dotAzureDir = this.dotEnvFile.getParent();
        final VirtualFile connectionsFile = dotAzureDir.findChild("connections.xml");
        final VirtualFile resourcesFile = dotAzureDir.findChild("resources.xml");
        if (Objects.nonNull(resourcesFile)) {
            this.resourceManager = new ResourceManager(resourcesFile, this);
        }
        if (Objects.nonNull(connectionsFile)) {
            this.connectionManager = new ConnectionManager(connectionsFile, this);
        }
    }

    public List<Pair<String, String>> load() {
        return load(this.dotEnvFile);
    }

    public static List<Pair<String, String>> load(@Nonnull VirtualFile dotEnv) {
        final DotenvReader reader = new DotenvReader(dotEnv.getParent().getPath(), dotEnv.getName());
        final DotenvParser parser = new DotenvParser(reader, false, false);
        return parser.parse().stream().map(e -> Pair.of(e.getKey(), e.getValue())).toList();
    }

    public synchronized Environment addConnection(@Nonnull Connection<?, ?> connection) {
        this.addConnectionToDotEnv(connection);
        Optional.of(this.getResourceManager(true)).ifPresent(m -> m.addResource(connection.getResource()));
        Optional.of(this.getConnectionManager(true)).ifPresent(m -> m.addConnection(connection));
        this.dotEnvFile.getParent().refresh(true, true);
        return this;
    }

    public synchronized Environment removeConnection(@Nonnull Connection<?, ?> connection) {
        this.removeConnectionFromDotEnv(connection);
        Optional.of(this.getConnectionManager(true)).ifPresent(m -> m.removeConnection(connection));
        this.dotEnvFile.getParent().refresh(true, true);
        return this;
    }

    public synchronized Environment updateConnection(@Nonnull Connection<?, ?> connection) {
        this.removeConnectionFromDotEnv(connection);
        this.addConnection(connection);
        return this;
    }

    public void save() {
        if (Objects.isNull(this.connectionManager) || Objects.isNull(this.resourceManager)) {
            return;
        }
        try {
            this.connectionManager.save();
            this.resourceManager.save();
        } catch (final IOException e) {
            throw new AzureToolkitRuntimeException(e);
        }
    }

    private void createResourceConnectionFilesIfNot() {
        try {
            final VirtualFile dotAzureDir = this.dotEnvFile.getParent();
            final VirtualFile connectionsFile = dotAzureDir.findOrCreateChildData(this, "connections.xml");
            final VirtualFile resourcesFile = dotAzureDir.findOrCreateChildData(this, "resources.xml");
        } catch (final IOException e) {
            throw new AzureToolkitRuntimeException(e);
        }
    }

    @AzureOperation("internal/connector.generate_env_variables")
    private static List<String> generateEnvLines(@Nonnull final Project project, @Nonnull final Connection<?, ?> connection) {
        final ArrayList<String> lines = new ArrayList<>();
        lines.add("# connection.id=" + connection.getId());
        lines.addAll(connection.getEnvironmentVariables(project).entrySet().stream()
            .map((e) -> String.format("%s=\"%s\"", e.getKey(), e.getValue()))
            .toList());
        return lines;
    }

    @AzureOperation("boundary/remove_connection_from_dotenv")
    private void removeConnectionFromDotEnv(@Nonnull Connection<?, ?> connection) {
        try {
            final List<String> lines = Files.readAllLines(this.dotEnvFile.toNioPath());
            final String startMark = "# connection.id=" + connection.getId();
            boolean started = false;
            final Iterator<String> each = lines.iterator();
            while (each.hasNext()) {
                final String line = each.next();
                started = started || line.equalsIgnoreCase(startMark);
                final boolean ended = started && !line.equalsIgnoreCase(startMark) && (StringUtils.isBlank(line.trim()) || line.trim().startsWith("# connection.id="));
                if (started && (!ended || StringUtils.isBlank(line.trim()))) {
                    each.remove();
                }
                if (ended) {
                    break;
                }
            }
            FileUtils.write(this.dotEnvFile.toNioPath().toFile(), lines.stream().collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new AzureToolkitRuntimeException(e);
        }
    }

    @AzureOperation("boundary/add_connection_to_dotenv")
    private void addConnectionToDotEnv(@Nonnull Connection<?, ?> connection) {
        final String envVariables = generateEnvLines(module.getProject(), connection).stream().collect(Collectors.joining(System.lineSeparator()));
        AzureTaskManager.getInstance().write(() -> {
            try {
                Files.writeString(this.dotEnvFile.toNioPath(), envVariables + System.lineSeparator() + System.lineSeparator(), StandardOpenOption.APPEND);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public List<Connection<?, ?>> getConnections() {
        return Optional.ofNullable(this.getConnectionManager(true)).map(ConnectionManager::getConnections)
            .orElse(Collections.emptyList());
    }

    @Nullable
    @Contract("true->!null")
    public synchronized ConnectionManager getConnectionManager(boolean createIfNotExist) {
        if (Objects.isNull(this.connectionManager)) {
            final VirtualFile dotAzureDir = this.dotEnvFile.getParent();
            if (createIfNotExist) {
                try {
                    final VirtualFile connectionsFile = dotAzureDir.findOrCreateChildData(this, "connections.xml");
                    this.connectionManager = new ConnectionManager(connectionsFile, this);
                } catch (final IOException e) {
                    throw new AzureToolkitRuntimeException(e);
                }
            } else {
                this.connectionManager = Optional.ofNullable(dotAzureDir.findChild("connections.xml"))
                    .map(file -> new ConnectionManager(file, this))
                    .orElse(null);
            }
        }
        return this.connectionManager;
    }

    @Nullable
    @Contract("true->!null")
    public synchronized ResourceManager getResourceManager(boolean createIfNotExist) {
        if (Objects.isNull(this.resourceManager)) {
            final VirtualFile dotAzureDir = this.dotEnvFile.getParent();
            if (createIfNotExist) {
                try {
                    final VirtualFile resourcesFile = dotAzureDir.findOrCreateChildData(this, "resources.xml");
                    this.resourceManager = new ResourceManager(resourcesFile, this);
                } catch (final IOException e) {
                    throw new AzureToolkitRuntimeException(e);
                }
            } else {
                this.resourceManager = Optional.ofNullable(dotAzureDir.findChild("resources.xml"))
                    .map(file -> new ResourceManager(file, this))
                    .orElse(null);
            }
        }
        return this.resourceManager;
    }
}
