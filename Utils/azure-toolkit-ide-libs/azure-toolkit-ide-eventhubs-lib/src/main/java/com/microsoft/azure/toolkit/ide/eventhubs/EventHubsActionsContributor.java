package com.microsoft.azure.toolkit.ide.eventhubs;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.view.IView;
import com.microsoft.azure.toolkit.lib.eventhubs.EventHubsInstance;
import com.microsoft.azure.toolkit.lib.eventhubs.EventHubsNamespace;

import java.util.ArrayList;

public class EventHubsActionsContributor implements IActionsContributor {
    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;
    public static final String SERVICE_ACTIONS = "actions.eventhubs.service";
    public static final String NAMESPACE_ACTIONS = "actions.eventhubs.namaspace";
    public static final String INSTANCE_ACTIONS = "actions.eventhubs.instance";
    public static final String SET_STATUS_ACTIONS = "actions.eventhubs.set_status.group";
    public static final Action.Id<EventHubsInstance> ACTIVE_INSTANCE = Action.Id.of("user/eventhubs.active_instance.instance");
    public static final Action.Id<EventHubsInstance> DISABLE_INSTANCE = Action.Id.of("user/eventhubs.disable_instance.instance");
    public static final Action.Id<EventHubsInstance> SEND_DISABLE_INSTANCE = Action.Id.of("user/eventhubs.send_disable_instance.instance");
    public static final Action.Id<EventHubsNamespace> COPY_CONNECTION_STRING = Action.Id.of("user/eventhubs.copy_connection_string.namespace");
    public static final Action.Id<EventHubsInstance> SEND_MESSAGE_INSTANCE = Action.Id.of("user/eventhubs.send_message.instance");
    public static final Action.Id<EventHubsInstance> START_LISTENING_INSTANCE = Action.Id.of("user/eventhubs.start_listening.instance");
    public static final Action.Id<EventHubsInstance> STOP_LISTENING_INSTANCE = Action.Id.of("user/eventhubs.stop_listening.instance");

    @Override
    public void registerActions(AzureActionManager am) {
        new Action<>(ACTIVE_INSTANCE)
                .visibleWhen(s -> s instanceof EventHubsInstance)
                .withLabel("Active")
                .withIdParam(AbstractAzResource::getName)
                .register(am);
        new Action<>(DISABLE_INSTANCE)
                .visibleWhen(s -> s instanceof EventHubsInstance)
                .withLabel("Disabled")
                .withIdParam(AbstractAzResource::getName)
                .register(am);
        new Action<>(SEND_DISABLE_INSTANCE)
                .visibleWhen(s -> s instanceof EventHubsInstance)
                .withLabel("SendDisabled")
                .withIdParam(AbstractAzResource::getName)
                .register(am);
        new Action<>(SEND_MESSAGE_INSTANCE)
                .visibleWhen(s -> s instanceof EventHubsInstance && ((EventHubsInstance) s).isActive())
                .withLabel("Send message")
                .withIdParam(AbstractAzResource::getName)
                .register(am);
        new Action<>(START_LISTENING_INSTANCE)
                .visibleWhen(s -> s instanceof EventHubsInstance && !((EventHubsInstance) s).isDisabled()
                        && !((EventHubsInstance) s).isListening())
                .withLabel("Start listening")
                .withIdParam(AbstractAzResource::getName)
                .register(am);
        new Action<>(STOP_LISTENING_INSTANCE)
                .visibleWhen(s -> s instanceof EventHubsInstance && !((EventHubsInstance) s).isDisabled()
                        && ((EventHubsInstance) s).isListening())
                .withLabel("Stop listening")
                .withIdParam(AbstractAzResource::getName)
                .register(am);
    }

    @Override
    public void registerGroups(AzureActionManager am) {
        final IView.Label.Static view = new IView.Label.Static("Set status");
        final ActionGroup setStatusActionGroup = new ActionGroup(new ArrayList<>(), view);
        setStatusActionGroup.addAction(ACTIVE_INSTANCE);
        setStatusActionGroup.addAction(DISABLE_INSTANCE);
        setStatusActionGroup.addAction(SEND_DISABLE_INSTANCE);
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
                ResourceCommonActionsContributor.OPEN_PORTAL_URL,
                "---",
                ResourceCommonActionsContributor.CREATE_IN_PORTAL,
                "---",
                ResourceCommonActionsContributor.DELETE);
        am.registerGroup(NAMESPACE_ACTIONS, namespaceGroup);

        final ActionGroup instanceGroup = new ActionGroup(
                ResourceCommonActionsContributor.REFRESH,
                ResourceCommonActionsContributor.OPEN_PORTAL_URL,
                "---",
                SET_STATUS_ACTIONS,
                SEND_MESSAGE_INSTANCE,
                START_LISTENING_INSTANCE,
                STOP_LISTENING_INSTANCE,
                "---",
                ResourceCommonActionsContributor.DELETE);
        am.registerGroup(INSTANCE_ACTIONS, instanceGroup);
    }

    @Override
    public int getOrder() {
        return INITIALIZE_ORDER;
    }
}
