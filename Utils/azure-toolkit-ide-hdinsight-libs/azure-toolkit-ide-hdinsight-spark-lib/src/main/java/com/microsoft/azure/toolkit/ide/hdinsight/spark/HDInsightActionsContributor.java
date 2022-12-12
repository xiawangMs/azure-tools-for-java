package com.microsoft.azure.toolkit.ide.hdinsight.spark;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.storage.StorageActionsContributor;
import com.microsoft.azure.toolkit.ide.storage.action.OpenAzureStorageExplorerAction;
import com.microsoft.azure.toolkit.lib.common.action.*;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.hdinsight.SparkCluster;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;
import com.microsoft.azure.toolkit.lib.storage.StorageAccount;

import java.util.Optional;
import java.util.function.Consumer;

import static com.microsoft.azure.toolkit.lib.common.operation.OperationBundle.description;

public class HDInsightActionsContributor implements IActionsContributor {

    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;
    public static final String SERVICE_ACTIONS = "actions.hdinsight.service";

    public static final String SPARK_CLUSTER_ACTIONS = "actions.hdinsight.spark";

    public static final Action.Id<ResourceGroup> GROUP_CREATE_HDInsight_SERVICE = Action.Id.of("hdinsight.create_hdinsight.group");
    public static final Action.Id<AzResource> OPEN_HDINSIGHT_JOB_VIEW = Action.Id.of("hdinsight.open_hdinsight_job_view.spark");

    @Override
    public void registerActions(AzureActionManager am) {
        final ActionView.Builder openHDInsightSparkJobsView = new ActionView.Builder("Open HDInsight Spark JobView")
                .title(s -> Optional.ofNullable(s).map(r -> description("hdinsight.open_hdinsight_job_view.spark", ((AzResource) r).getName())).orElse(null))
                .enabled(s -> s instanceof AzResource);
        am.registerAction(new Action<>(OPEN_HDINSIGHT_JOB_VIEW,(resource)->{},openHDInsightSparkJobsView));

        final Consumer<AzResource> openAzureStorageExplorer = resource -> {
            if (resource instanceof StorageAccount) {
                new OpenAzureStorageExplorerAction().openResource((StorageAccount) resource);
            } else if (resource instanceof AbstractAzResource && ((AbstractAzResource<?, ?, ?>) resource).getParent() instanceof StorageAccount) {
                //noinspection unchecked
                new OpenAzureStorageExplorerAction().openResource((AbstractAzResource<?, StorageAccount, ?>) resource);
            } else {
                AzureMessager.getMessager().warning("Only Azure Storages can be opened with Azure Storage Explorer.");
            }
        };
        final ActionView.Builder openAzureStorageExplorerView = new ActionView.Builder("Open Azure Storage Explorer")
                .title(s -> Optional.ofNullable(s).map(r -> description("storage.open_azure_storage_explorer.account", ((AzResource) r).getName())).orElse(null))
                .enabled(s -> (s instanceof StorageAccount && ((AzResource) s).getFormalStatus().isConnected()) || s instanceof AzResource || s instanceof SparkCluster);
        final Action<AzResource> openAzureStorageExplorerAction = new Action<>(StorageActionsContributor.OPEN_AZURE_STORAGE_EXPLORER, openAzureStorageExplorer, openAzureStorageExplorerView);
        openAzureStorageExplorerAction.setShortcuts(am.getIDEDefaultShortcuts().edit());
        am.registerAction(openAzureStorageExplorerAction);
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

        final ActionGroup sparkActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.PIN,
                "---",
                ResourceCommonActionsContributor.REFRESH,
                ResourceCommonActionsContributor.OPEN_AZURE_REFERENCE_BOOK,
                ResourceCommonActionsContributor.OPEN_PORTAL_URL,
                "---",
                StorageActionsContributor.OPEN_AZURE_STORAGE_EXPLORER
        );
        am.registerGroup(SPARK_CLUSTER_ACTIONS, sparkActionGroup);

        final IActionGroup group = am.getGroup(ResourceCommonActionsContributor.RESOURCE_GROUP_CREATE_ACTIONS);
        group.addAction(GROUP_CREATE_HDInsight_SERVICE);
    }

    @Override
    public int getOrder() {
        return INITIALIZE_ORDER;
    }

}
