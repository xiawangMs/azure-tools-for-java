package com.microsoft.azure.toolkit.ide.hdinsight.spark;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.*;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;

import java.util.Optional;

import static com.microsoft.azure.toolkit.lib.common.operation.OperationBundle.description;

public class HDInsightActionsContributor implements IActionsContributor {

    public static final String SERVICE_ACTIONS = "actions.hdinsight.service";

    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;

    public static final Action.Id<ResourceGroup> GROUP_CREATE_HDInsight_SERVICE = Action.Id.of("hdinsight.create_hdinsight.group");

    //public static final Action.Id<String> LINK_A_CLUSTER = Action.Id.of("hdinsight.create_hdinsight.group");;
    @Override
    public void registerActions(AzureActionManager am) {
//        final ActionView.Builder createClusterView = new ActionView.Builder("HDInsight")
//                .title(s -> Optional.ofNullable(s).map(r ->
//                        description("hdinsight.create_hdinsight.group", ((ResourceGroup) r).getName())).orElse(null))
//                .enabled(s -> s instanceof ResourceGroup && ((ResourceGroup) s).getFormalStatus().isConnected());
//        am.registerAction(GROUP_CREATE_HDInsight_SERVICE, new Action<>(GROUP_CREATE_HDInsight_SERVICE, createClusterView));
    }

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup serviceActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.REFRESH,
                ResourceCommonActionsContributor.OPEN_AZURE_REFERENCE_BOOK,
                "---",
                ResourceCommonActionsContributor.CREATE
        );
        am.registerGroup(SERVICE_ACTIONS, serviceActionGroup);

        final IActionGroup group = am.getGroup(ResourceCommonActionsContributor.RESOURCE_GROUP_CREATE_ACTIONS);
        group.addAction(GROUP_CREATE_HDInsight_SERVICE);
    }

    @Override
    public int getOrder() {
        return INITIALIZE_ORDER;
    }

}
