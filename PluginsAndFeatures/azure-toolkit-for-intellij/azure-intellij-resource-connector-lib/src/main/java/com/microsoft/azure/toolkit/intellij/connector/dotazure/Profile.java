/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.dotazure;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.intellij.connector.Connection;
import com.microsoft.azure.toolkit.intellij.connector.ConnectionTopics;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.operation.OperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import io.github.cdimascio.dotenv.internal.DotenvParser;
import io.github.cdimascio.dotenv.internal.DotenvReader;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

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

import static com.microsoft.azure.toolkit.intellij.connector.ConnectionTopics.CONNECTION_CHANGED;
import static com.microsoft.azure.toolkit.intellij.connector.dotazure.AzureModule.*;

public class Profile {
    @Getter
    @Nonnull
    private final String name;
    @Getter
    @Nonnull
    private final VirtualFile profileDir;
    @Nonnull
    private final AzureModule module;
    @Getter
    @Nullable
    private VirtualFile dotEnvFile;
    @Nullable
    private ConnectionManager connectionManager;
    @Nullable
    private ResourceManager resourceManager;

    public Profile(@Nonnull String name, @Nonnull VirtualFile profileDir, @Nonnull AzureModule module) {
        this.name = name;
        this.module = module;
        this.profileDir = profileDir;
        this.dotEnvFile = this.profileDir.findChild(DOT_ENV);
        final VirtualFile connectionsFile = this.profileDir.findChild(CONNECTIONS_FILE);
        final VirtualFile resourcesFile = this.profileDir.findChild(RESOURCES_FILE);
        if (Objects.nonNull(resourcesFile)) {
            this.resourceManager = new ResourceManager(this);
        }
        if (Objects.nonNull(connectionsFile)) {
            this.connectionManager = new ConnectionManager(this);
        }
    }

    public List<Pair<String, String>> load() {
        return Optional.ofNullable(this.dotEnvFile).map(Profile::load).orElseGet(Collections::emptyList);
    }

    public static List<Pair<String, String>> load(@Nonnull VirtualFile dotEnv) {
        final DotenvReader reader = new DotenvReader(dotEnv.getParent().getPath(), dotEnv.getName());
        final DotenvParser parser = new DotenvParser(reader, false, false);
        return parser.parse().stream().map(e -> Pair.of(e.getKey(), e.getValue())).toList();
    }

    public synchronized Profile addConnection(@Nonnull Connection<?, ?> connection) {
        this.addConnectionToDotEnv(connection);
        this.getResourceManager().addResource(connection.getResource());
        this.getConnectionManager().addConnection(connection);
        final Project project = this.module.getProject();
        project.getMessageBus().syncPublisher(CONNECTION_CHANGED).connectionChanged(project, connection, ConnectionTopics.Action.ADD);
        return this;
    }

    public synchronized Profile removeConnection(@Nonnull Connection<?, ?> connection) {
        this.removeConnectionFromDotEnv(connection);
        this.getConnectionManager().removeConnection(connection);
        final Project project = this.module.getProject();
        project.getMessageBus().syncPublisher(CONNECTION_CHANGED).connectionChanged(project, connection, ConnectionTopics.Action.REMOVE);
        return this;
    }

    public synchronized Profile createOrUpdateConnection(@Nonnull Connection<?, ?> connection) {
        // Remove old connection
        this.getConnections().stream().filter(c -> c.getId().equals(connection.getId())).findFirst().ifPresent(this::removeConnection);
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
            this.profileDir.refresh(true, true);
        } catch (final IOException e) {
            throw new AzureToolkitRuntimeException(e);
        }
    }

    @AzureOperation(value = "internal/connector.generate_env_variables.resource", params = "connection.getResource().getName()")
    private static List<String> generateEnvLines(@Nonnull final Project project, @Nonnull final Connection<?, ?> connection) {
        final ArrayList<String> lines = new ArrayList<>();
        lines.add("# connection.id=" + connection.getId());
        lines.addAll(connection.getEnvironmentVariables(project).entrySet().stream()
            .map((e) -> String.format("%s=\"%s\"", e.getKey(), e.getValue()))
            .toList());
        return lines;
    }

    @AzureOperation(value = "boundary/remove_connection_from_dotenv.resource", params = "connection.getResource().getName()")
    private void removeConnectionFromDotEnv(@Nonnull Connection<?, ?> connection) {
        if (Objects.isNull(this.dotEnvFile)) {
            return;
        }
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

    @SneakyThrows(IOException.class)
    @AzureOperation(value = "boundary/add_connection_to_dotenv.resource", params = "connection.getResource().getName()")
    private void addConnectionToDotEnv(@Nonnull Connection<?, ?> connection) {
        WriteAction.run(()-> this.dotEnvFile = this.profileDir.findOrCreateChildData(this, DOT_ENV));
        Objects.requireNonNull(this.dotEnvFile, ".env file can not be created.");
        final AzureString description = OperationBundle.description("internal/dotazure.load_env.resource", connection.getResource().getDataId());
        AzureTaskManager.getInstance().runInBackground(description, () -> {
            final String envVariables = generateEnvLines(module.getProject(), connection).stream().collect(Collectors.joining(System.lineSeparator()));
            try {
                Files.writeString(this.dotEnvFile.toNioPath(), envVariables + System.lineSeparator() + System.lineSeparator(), StandardOpenOption.APPEND);
                this.profileDir.refresh(true, true);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public List<Connection<?, ?>> getConnections() {
        return this.getConnectionManager().getConnections();
    }

    @Nonnull
    public synchronized ConnectionManager getConnectionManager() {
        if (Objects.isNull(this.connectionManager)) {
            this.connectionManager = new ConnectionManager(this);
        }
        return this.connectionManager;
    }

    @Nonnull
    public synchronized ResourceManager getResourceManager() {
        if (Objects.isNull(this.resourceManager)) {
            this.resourceManager = new ResourceManager(this);
        }
        return this.resourceManager;
    }
}
