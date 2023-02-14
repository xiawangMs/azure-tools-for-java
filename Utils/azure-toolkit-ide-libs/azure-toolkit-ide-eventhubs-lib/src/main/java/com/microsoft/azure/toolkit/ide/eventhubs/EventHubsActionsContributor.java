package com.microsoft.azure.toolkit.ide.eventhubs;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;

public class EventHubsActionsContributor implements IActionsContributor {
    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;
    public static final String SERVICE_ACTIONS = "actions.eventhubs.service";
    public static final String NAMESPACE_ACTIONS = "actions.eventhubs.namaspace";
    public static final String INSTANCE_ACTIONS = "actions.eventhubs.instance";

    @Override
    public void registerActions(AzureActionManager am) {

    }

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup serviceGroup = new ActionGroup(ResourceCommonActionsContributor.REFRESH);
        am.registerGroup(SERVICE_ACTIONS, serviceGroup);

        final ActionGroup namespaceGroup = new ActionGroup(
                ResourceCommonActionsContributor.PIN,
                "---",
                ResourceCommonActionsContributor.REFRESH,
                ResourceCommonActionsContributor.OPEN_AZURE_REFERENCE_BOOK,
                ResourceCommonActionsContributor.OPEN_PORTAL_URL);
        am.registerGroup(NAMESPACE_ACTIONS, namespaceGroup);

        final ActionGroup instanceGroup = new ActionGroup(
                ResourceCommonActionsContributor.REFRESH);
        am.registerGroup(INSTANCE_ACTIONS, instanceGroup);
    }

    @Override
    public int getOrder() {
        return INITIALIZE_ORDER;
    }
}
