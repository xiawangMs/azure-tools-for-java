/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.dotazure;

import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.intellij.connector.Connection;
import com.microsoft.azure.toolkit.intellij.connector.ConnectionDefinition;
import com.microsoft.azure.toolkit.intellij.connector.ResourceDefinition;
import com.microsoft.azure.toolkit.intellij.connector.ResourceManager;
import com.microsoft.azure.toolkit.lib.common.messager.ExceptionNotification;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;
import org.jdom.JDOMException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ConnectionManager {
    private static final ExtensionPointName<ConnectionDefinition<?, ?>> exPoints =
        ExtensionPointName.create("com.microsoft.tooling.msservices.intellij.azure.connectorConnectionType");
    private static final String ELEMENT_NAME_CONNECTIONS = "connections";
    private static final String ELEMENT_NAME_CONNECTION = "connection";
    private static final String FIELD_TYPE = "type";
    private static Map<String, ConnectionDefinition<?, ?>> definitions = null;
    private final Set<Connection<?, ?>> connections = new LinkedHashSet<>();
    private final VirtualFile connectionsFile;

    public synchronized static Map<String, ConnectionDefinition<?, ?>> getDefinitionsMap() {
        if (MapUtils.isEmpty(definitions)) {
            definitions = exPoints.getExtensionList().stream().collect(Collectors.toMap(ConnectionDefinition::getName, r -> r));
        }
        return definitions;
    }

    @Nonnull
    public static ArrayList<ConnectionDefinition<?, ?>> getDefinitions() {
        return new ArrayList<>(getDefinitionsMap().values());
    }

    @Nullable
    public static ConnectionDefinition<?, ?> getDefinitionOrDefault(@Nonnull String name) {
        return getDefinitionsMap().computeIfAbsent(name, def -> {
            final String[] split = def.split(":");
            final ResourceDefinition<?> rd = com.microsoft.azure.toolkit.intellij.connector.ResourceManager.getDefinition(split[0]);
            final ResourceDefinition<?> cd = ResourceManager.getDefinition(split[1]);
            return new ConnectionDefinition<>(rd, cd);
        });
    }

    @Nonnull
    public static List<Connection<?, ?>> getConnectionForRunConfiguration(final RunConfiguration config) {
        final List<Connection<?, ?>> connections = config.getProject().getService(ConnectionManager.class).getConnections();
        return connections.stream().filter(c -> c.isApplicableFor(config)).collect(Collectors.toList());
    }

    @Nonnull
    public static ConnectionDefinition<?, ?> getDefinitionOrDefault(@Nonnull ResourceDefinition<?> rd, @Nonnull ResourceDefinition<?> cd) {
        final String name = getName(rd, cd);
        return getDefinitionsMap().computeIfAbsent(name, def -> new ConnectionDefinition<>(rd, cd));
    }

    @EqualsAndHashCode.Include
    public static String getName(ConnectionDefinition<?, ?> definition) {
        return getName(definition.getResourceDefinition(), definition.getConsumerDefinition());
    }

    public static String getName(@Nonnull ResourceDefinition<?> rd, @Nonnull ResourceDefinition<?> cd) {
        return String.format("%s:%s", rd.getName(), cd.getName());
    }

    @AzureOperation(name = "user/connector.add_connection")
    public synchronized void addConnection(Connection<?, ?> connection) {
        connections.removeIf(c -> Objects.equals(c, connection)); // always replace the old with the new one.
        connections.add(connection);
    }

    @AzureOperation(name = "user/connector.remove_connection")
    public synchronized void removeConnection(String resourceId, String consumerId) {
        connections.removeIf(c -> StringUtils.equals(resourceId, c.getResource().getId()) && StringUtils.equals(consumerId, c.getConsumer().getId()));
    }

    public List<Connection<?, ?>> getConnections() {
        return new ArrayList<>(connections);
    }

    public List<Connection<?, ?>> getConnectionsByResourceId(String id) {
        return connections.stream().filter(e -> StringUtils.equals(id, e.getResource().getId())).collect(Collectors.toList());
    }

    public List<Connection<?, ?>> getConnectionsByConsumerId(String id) {
        return connections.stream().filter(e -> StringUtils.equals(id, e.getConsumer().getId())).collect(Collectors.toList());
    }

    @ExceptionNotification
    void save() throws IOException {
        final Element connectionsEle = new Element(ELEMENT_NAME_CONNECTIONS);
        for (final Connection<?, ?> connection : this.connections) {
            final Element connectionEle = new Element(ELEMENT_NAME_CONNECTION);
            connectionEle.setAttribute(FIELD_TYPE, ConnectionManager.getName(connection.getDefinition()));
            connection.write(connectionEle);
            connectionsEle.addContent(connectionEle);
        }
        JDOMUtil.write(connectionsEle, this.connectionsFile.toNioPath());
    }

    @ExceptionNotification
    @AzureOperation(name = "boundary/connector.load_resource_connections")
    void load() throws IOException, JDOMException {
        this.connections.clear();
        final Element connectionsEle = JDOMUtil.load(this.connectionsFile.toNioPath());
        for (final Element connectionEle : connectionsEle.getChildren()) {
            final String name = connectionEle.getAttributeValue(FIELD_TYPE);
            final ConnectionDefinition<?, ?> definition = ConnectionManager.getDefinitionOrDefault(name);
            try {
                Optional.ofNullable(definition).map(d -> d.read(connectionEle)).ifPresent(this::addConnection);
            } catch (final Exception e) {
                log.warn(String.format("error occurs when load a resource connection of type '%s'", name), e);
            }
        }
    }
}
