/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.servicebus;

import com.azure.resourcemanager.servicebus.models.EntityStatus;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.action.IActionGroup;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.view.IView;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;
import com.microsoft.azure.toolkit.lib.servicebus.ServiceBusNamespace;
import com.microsoft.azure.toolkit.lib.servicebus.model.ServiceBusInstance;
import com.microsoft.azure.toolkit.lib.servicebus.queue.ServiceBusQueue;

import java.util.ArrayList;

public class ServiceBusActionsContributor implements IActionsContributor {
    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;
    public static final String SERVICE_ACTIONS = "actions.servicebus.service";
    public static final String NAMESPACE_ACTIONS = "actions.servicebus.namaspace";
    public static final String QUEUE_ACTIONS = "actions.servicebus.queue";
    public static final String TOPIC_ACTIONS = "actions.servicebus.topic";
    public static final String MODULE_ACTIONS = "actions.servicebus.module";
    public static final String SET_STATUS_ACTIONS = "actions.servicebus.set_status.group";
    public static final Action.Id<ServiceBusNamespace> COPY_NAMESPACE_CONNECTION_STRING = Action.Id.of("user/servicebus.copy_connection_string.namespace");
    public static final Action.Id<ServiceBusInstance<?, ?, ?>> ACTIVE_INSTANCE = Action.Id.of("user/servicebus.active_instance.instance");
    public static final Action.Id<ServiceBusInstance<?, ?, ?>> DISABLE_INSTANCE = Action.Id.of("user/servicebus.disable_instance.instance");
    public static final Action.Id<ServiceBusInstance<?, ?, ?>> SEND_DISABLE_INSTANCE = Action.Id.of("user/servicebus.send_disable_instance.instance");
    public static final Action.Id<ServiceBusInstance<?, ?, ?>> RECEIVE_DISABLE_INSTANCE = Action.Id.of("user/servicebus.receive_disable_instance.instance");
    public static final Action.Id<ServiceBusInstance<?, ?, ?>> COPY_CONNECTION_STRING = Action.Id.of("user/servicebus.copy_connection_string.instance");
    public static final Action.Id<ServiceBusInstance<?, ?, ?>> SEND_MESSAGE_INSTANCE = Action.Id.of("user/servicebus.open_send_message_panel.instance");
    public static final Action.Id<ServiceBusInstance<?, ?, ?>> START_RECEIVING_INSTANCE = Action.Id.of("user/servicebus.start_receiving.instance");
    public static final Action.Id<ServiceBusInstance<?, ?, ?>> STOP_RECEIVING_INSTANCE = Action.Id.of("user/servicebus.stop_receiving.instance");
    public static final Action.Id<ResourceGroup> GROUP_CREATE_SERVICE_BUS = Action.Id.of("user/servicebus.create_servicebus.group");

    @Override
    public void registerActions(AzureActionManager am) {
        new Action<>(COPY_NAMESPACE_CONNECTION_STRING)
                .visibleWhen(s -> s instanceof ServiceBusNamespace)
                .withLabel("Copy Connection String")
                .withIdParam(AzResource::getName)
                .register(am);
        new Action<>(ACTIVE_INSTANCE)
                .visibleWhen(s -> s instanceof ServiceBusInstance)
                .enableWhen(s -> EntityStatus.ACTIVE != s.getEntityStatus())
                .withLabel("Active")
                .withIdParam(AzResource::getName)
                .register(am);
        new Action<>(DISABLE_INSTANCE)
                .visibleWhen(s -> s instanceof ServiceBusInstance)
                .enableWhen(s -> EntityStatus.DISABLED != s.getEntityStatus())
                .withLabel("Disabled")
                .withIdParam(AzResource::getName)
                .register(am);
        new Action<>(SEND_DISABLE_INSTANCE)
                .visibleWhen(s -> s instanceof ServiceBusInstance)
                .enableWhen(s -> EntityStatus.SEND_DISABLED != s.getEntityStatus())
                .withLabel("SendDisabled")
                .withIdParam(AzResource::getName)
                .register(am);
        new Action<>(RECEIVE_DISABLE_INSTANCE)
                .visibleWhen(s -> s instanceof ServiceBusQueue)
                .enableWhen(s -> EntityStatus.RECEIVE_DISABLED != s.getEntityStatus())
                .withLabel("ReceivedDisabled")
                .withIdParam(AzResource::getName)
                .register(am);
        new Action<>(COPY_CONNECTION_STRING)
                .visibleWhen(s -> s instanceof ServiceBusInstance)
                .withLabel("Copy Connection String")
                .withIdParam(AzResource::getName)
                .register(am);
        new Action<>(SEND_MESSAGE_INSTANCE)
                .visibleWhen(s -> s instanceof ServiceBusInstance)
                .enableWhen(ServiceBusInstance::isSendEnabled)
                .withLabel("Send Message")
                .withIdParam(AbstractAzResource::getName)
                .register(am);
        new Action<>(START_RECEIVING_INSTANCE)
                .visibleWhen(s -> s instanceof ServiceBusInstance && !((ServiceBusInstance<?, ?, ?>) s).isListening())
                .enableWhen(s -> s.getEntityStatus() != EntityStatus.DISABLED && s.getEntityStatus() != EntityStatus.RECEIVE_DISABLED)
                .withLabel("Start Listening")
                .withIdParam(AbstractAzResource::getName)
                .register(am);
        new Action<>(STOP_RECEIVING_INSTANCE)
                .visibleWhen(s -> s instanceof ServiceBusInstance && ((ServiceBusInstance<?, ?, ?>) s).isListening())
                .enableWhen(s -> s.getEntityStatus() != EntityStatus.DISABLED && s.getEntityStatus() != EntityStatus.RECEIVE_DISABLED)
                .withLabel("Stop Listening")
                .withIdParam(AbstractAzResource::getName)
                .register(am);
        new Action<>(GROUP_CREATE_SERVICE_BUS)
                .withLabel("Service Bus")
                .withIdParam(AzResource::getName)
                .visibleWhen(s -> s instanceof ResourceGroup)
                .enableWhen(s -> s.getFormalStatus().isConnected())
                .register(am);
    }

    @Override
    public void registerGroups(AzureActionManager am) {
        final IView.Label.Static view = new IView.Label.Static("Set Status");
        final ActionGroup setStatusActionGroup = new ActionGroup(new ArrayList<>(), view);
        setStatusActionGroup.addAction(ACTIVE_INSTANCE);
        setStatusActionGroup.addAction(DISABLE_INSTANCE);
        setStatusActionGroup.addAction(SEND_DISABLE_INSTANCE);
        setStatusActionGroup.addAction(RECEIVE_DISABLE_INSTANCE);
        am.registerGroup(SET_STATUS_ACTIONS, setStatusActionGroup);

        final ActionGroup serviceGroup = new ActionGroup(
                ResourceCommonActionsContributor.REFRESH,
                ResourceCommonActionsContributor.OPEN_AZURE_REFERENCE_BOOK,
                "---",
                ResourceCommonActionsContributor.CREATE_IN_PORTAL
        );
        am.registerGroup(SERVICE_ACTIONS, serviceGroup);

        final ActionGroup namespaceGroup = new ActionGroup(
                ResourceCommonActionsContributor.PIN,
                "---",
                ResourceCommonActionsContributor.REFRESH,
                ResourceCommonActionsContributor.OPEN_AZURE_REFERENCE_BOOK,
                COPY_NAMESPACE_CONNECTION_STRING,
                ResourceCommonActionsContributor.OPEN_PORTAL_URL,
                "---",
                ResourceCommonActionsContributor.DELETE);
        am.registerGroup(NAMESPACE_ACTIONS, namespaceGroup);

        final ActionGroup moduleGroup = new ActionGroup(
                ResourceCommonActionsContributor.REFRESH
        );
        am.registerGroup(MODULE_ACTIONS, moduleGroup);

        final ActionGroup queueGroup = new ActionGroup(
                ResourceCommonActionsContributor.REFRESH,
                ResourceCommonActionsContributor.OPEN_PORTAL_URL,
                "---",
                SET_STATUS_ACTIONS,
                COPY_CONNECTION_STRING,
                SEND_MESSAGE_INSTANCE,
                START_RECEIVING_INSTANCE,
                STOP_RECEIVING_INSTANCE,
                "---",
                ResourceCommonActionsContributor.DELETE);
        am.registerGroup(QUEUE_ACTIONS, queueGroup);

        final ActionGroup topicGroup = new ActionGroup(
                ResourceCommonActionsContributor.REFRESH,
                ResourceCommonActionsContributor.OPEN_PORTAL_URL,
                "---",
                SET_STATUS_ACTIONS,
                COPY_CONNECTION_STRING,
                SEND_MESSAGE_INSTANCE,
                START_RECEIVING_INSTANCE,
                STOP_RECEIVING_INSTANCE,
                "---",
                ResourceCommonActionsContributor.DELETE);
        am.registerGroup(TOPIC_ACTIONS, topicGroup);

        final IActionGroup group = am.getGroup(ResourceCommonActionsContributor.RESOURCE_GROUP_CREATE_ACTIONS);
        group.addAction(GROUP_CREATE_SERVICE_BUS);
    }

    @Override
    public int getOrder() {
        return INITIALIZE_ORDER;
    }
}
