package com.microsoft.azure.toolkit.intellij.eventhubs;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.eventhubs.EventHubsActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.eventhubs.EventHubsInstance;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;


public class IntellijEventHubsActionsContributor implements IActionsContributor {
    @Override
    public void registerHandlers(AzureActionManager am) {
        registerActiveActionHandler(am);
        registerDisabledActionHandler(am);
        registerSendDisabledActionHandler(am);
    }

    private void registerActiveActionHandler(AzureActionManager am) {
        final BiPredicate<EventHubsInstance, AnActionEvent> condition = (r, e) -> true;
        final BiConsumer<EventHubsInstance, AnActionEvent> handler = (c, e) -> c.active();
        am.registerHandler(EventHubsActionsContributor.ACTIVE_INSTANCE, condition, handler);
    }

    private void registerDisabledActionHandler(AzureActionManager am) {
        final BiPredicate<EventHubsInstance, AnActionEvent> condition = (r, e) -> true;
        final BiConsumer<EventHubsInstance, AnActionEvent> handler = (c, e) -> c.disable();
        am.registerHandler(EventHubsActionsContributor.DISABLE_INSTANCE, condition, handler);
    }

    private void registerSendDisabledActionHandler(AzureActionManager am) {
        final BiPredicate<EventHubsInstance, AnActionEvent> condition = (r, e) -> true;
        final BiConsumer<EventHubsInstance, AnActionEvent> handler = (c, e) -> c.disableSending();
        am.registerHandler(EventHubsActionsContributor.SEND_DISABLE_INSTANCE, condition, handler);
    }

    @Override
    public int getOrder() {
        return EventHubsActionsContributor.INITIALIZE_ORDER + 1;
    }
}
