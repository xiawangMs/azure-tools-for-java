package com.microsoft.azure.toolkit.ide.hdinsight.spark;

import com.azure.resourcemanager.hdinsight.models.StorageAccount;
import com.microsoft.azure.hdinsight.common.ClusterManagerEx;
import com.microsoft.azure.hdinsight.common.OpenHDIAzureStorageExplorerAction;
import com.microsoft.azure.hdinsight.sdk.cluster.EmulatorClusterDetail;
import com.microsoft.azure.hdinsight.sdk.cluster.IClusterDetail;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.Account;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.action.*;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.operation.OperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.hdinsight.AzureHDInsightService;
import com.microsoft.azure.toolkit.lib.hdinsight.SparkClusterNode;
import com.microsoft.azure.toolkit.lib.hdinsight.StorageAccountNode;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;
import com.microsoft.azuretools.azurecommons.helpers.StringHelper;
import com.microsoft.tooling.msservices.components.DefaultLoader;

import java.util.Optional;
import java.util.function.Consumer;

import static com.microsoft.azure.toolkit.lib.Azure.az;
import static com.microsoft.azure.toolkit.lib.common.operation.OperationBundle.description;

public class HDInsightActionsContributor implements IActionsContributor {

    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;

    public static final String SERVICE_ACTIONS = "actions.hdinsight.service";
    public static final String SPARK_CLUSTER_ACTIONS = "actions.hdinsight.spark";
    public static final String SPARK_ADDITIONAL_CLUSTER_ACTIONS = "actions.hdinsight_additional.spark";
    public static final String HDINSIGHT_STORAGE_ACTIONS = "actions.hdinsight.storage";

    public static final Action.Id<ResourceGroup> GROUP_CREATE_HDInsight_SERVICE = Action.Id.of("hdinsight.create_hdinsight.group");
    public static final Action.Id<Object> LINK_A_CLUSTER = Action.Id.of("hdinsight.link_a_cluster.spark");
    public static final Action.Id<Object> UNLINK_A_CLUSTER = Action.Id.of("hdinsight.unlink_a_cluster.spark");
    public static final Action.Id<AzResource> OPEN_HDINSIGHT_JOB_VIEW = Action.Id.of("hdinsight.open_hdinsight_job_view.spark");
    public static final Action.Id<AzResource> OPEN_AZURE_STORAGE_EXPLORER = Action.Id.of("hdinsight.open_azure_storage_explorer.cluster");
    public static final Action.Id<AzResource> OPEN_AZURE_EXPLORER_AMBARI = Action.Id.of("hdinsight.open_azure_management_explorer.ambari");
    public static final Action.Id<AzResource> OPEN_AZURE_EXPLORER_JUPYTER = Action.Id.of("hdinsight.open_jupyter_explorer.jupyter");
    public static final Action.Id<AzResource> OPEN_SPARK_HISTORY_UI = Action.Id.of("hdinsight.open_history_ui.spark");
    public static final Action.Id<AzResource> OPEN_AZURE_STORAGE_MANAGEMENT_EXPLORER = Action.Id.of("hdinsight.open_azure_storage_explorer.storage");


    @Override
    public void registerActions(AzureActionManager am) {
        final ActionView.Builder openLinkAClusterView = new ActionView.Builder("Link A Cluster")
                .title(s -> Optional.ofNullable(s).map(r -> description("hdinsight.link_a_cluster.spark", "")).orElse(null))
                .enabled(s -> true);
        am.registerAction(new Action<>(LINK_A_CLUSTER,(resource)->{},openLinkAClusterView)
                .setShortcuts(am.getIDEDefaultShortcuts().edit()));

        final ActionView.Builder openUnLinkAClusterView = new ActionView.Builder("Unlink")
                .title(s -> Optional.ofNullable(s).map(r -> description("hdinsight.unlink_a_cluster.spark", "")).orElse(null))
                .enabled(s -> true);
        am.registerAction(new Action<>(UNLINK_A_CLUSTER,(resource)->{
            if (resource instanceof SparkClusterNode) {
                SparkClusterNode sparkClusterNode = (SparkClusterNode) resource;
                boolean choice = DefaultLoader.getUIHelper().showConfirmation("Do you really want to unlink the HDInsight cluster?",
                        "Unlink HDInsight Cluster", new String[]{"Yes", "No"}, null);
                if (choice) {
                    ClusterManagerEx.getInstance().removeAdditionalCluster(sparkClusterNode.getClusterDetail());
                    AzureHDInsightService service = az(AzureHDInsightService.class);
                    service.refresh();
                }
            }
        },openUnLinkAClusterView)
                .setShortcuts(am.getIDEDefaultShortcuts().edit()));

        final ActionView.Builder openHDInsightSparkJobsView = new ActionView.Builder("Open HDInsight Spark JobView")
                .title(s -> Optional.ofNullable(s).map(r -> description("hdinsight.open_hdinsight_job_view.spark", ((AzResource) r).getName())).orElse(null))
                .enabled(s -> s instanceof AzResource);
        am.registerAction(new Action<>(OPEN_HDINSIGHT_JOB_VIEW,(resource)->{},openHDInsightSparkJobsView));


        final Consumer<AzResource> openAzureStorageExplorer = resource -> {
            if (resource instanceof SparkClusterNode) {
                IClusterDetail clusterDetail = ((SparkClusterNode) resource).getClusterDetail();
                final AzureString title = OperationBundle.description("hdinsight.open_azure_storage_explorer.cluster", clusterDetail.getName());
                AzureTaskManager.getInstance().runInBackground(new AzureTask<>(title, () -> {
                    OpenHDIAzureStorageExplorerAction openHDIAzureStorageExplorerAction = new OpenHDIAzureStorageExplorerAction();
                    openHDIAzureStorageExplorerAction.openResource(clusterDetail);
                }));
            }
        };
        final ActionView.Builder openAzureStorageExplorerView = new ActionView.Builder("Open Azure Storage Explorer")
                .title(s -> Optional.ofNullable(s).map(r -> description("hdinsight.open_azure_storage_explorer.cluster", ((AzResource) r).getName())).orElse(null))
                .enabled(s -> true);
        final Action<AzResource> openAzureStorageExplorerAction = new Action<>(OPEN_AZURE_STORAGE_EXPLORER, openAzureStorageExplorer,openAzureStorageExplorerView);
        openAzureStorageExplorerAction.setShortcuts(am.getIDEDefaultShortcuts().edit());
        am.registerAction(openAzureStorageExplorerAction);


        final Consumer<AzResource> openHDInsightExplorerAmbariPotal = resource -> {
            if (resource instanceof SparkClusterNode) {
                IClusterDetail clusterDetail = ((SparkClusterNode) resource).getClusterDetail();
                String ambariUrl = clusterDetail.isEmulator() ?
                ((EmulatorClusterDetail) clusterDetail).getAmbariEndpoint() :
                ClusterManagerEx.getInstance().getClusterConnectionString(clusterDetail.getName());
                openUrlLink(clusterDetail,ambariUrl);
            }
        };
        final ActionView.Builder openHDInsightAmbariPotalView = new ActionView.Builder("Open Cluster Management Portal(Ambari)")
                .title(s -> Optional.ofNullable(s).map(r -> description("hdinsight.open_azure_management_explorer.ambari", ((AzResource) r).getName())).orElse(null))
                .enabled(s -> s instanceof AzResource);
        final Action<AzResource> openHDInsightExplorerAmbariPotalAction = new Action<>(OPEN_AZURE_EXPLORER_AMBARI, openHDInsightExplorerAmbariPotal,openHDInsightAmbariPotalView);
        am.registerAction(openHDInsightExplorerAmbariPotalAction);


        final Consumer<AzResource> openHDInsightJupyter = resource -> {
            if (resource instanceof SparkClusterNode) {
                IClusterDetail clusterDetail = ((SparkClusterNode) resource).getClusterDetail();
                final String jupyterUrl = ClusterManagerEx.getInstance().getClusterConnectionString(clusterDetail.getName()) + "/jupyter/tree";
                openUrlLink(clusterDetail,jupyterUrl);
            }
        };
        final ActionView.Builder openHDInsightJupyterView = new ActionView.Builder("Open Jupyter Notebook")
                .title(s -> Optional.ofNullable(s).map(r -> description("hdinsight.open_jupyter_explorer.jupyter", ((AzResource) r).getName())).orElse(null))
                .enabled(s -> s instanceof AzResource);
        final Action<AzResource> openHDInsightJupyterAction = new Action<>(OPEN_AZURE_EXPLORER_JUPYTER, openHDInsightJupyter,openHDInsightJupyterView);
        am.registerAction(openHDInsightJupyterAction);


        final Consumer<AzResource> openHDInsightSparkHistory = resource -> {
            if (resource instanceof SparkClusterNode) {
                IClusterDetail clusterDetail = ((SparkClusterNode) resource).getClusterDetail();
                String sparkHistoryUrl = clusterDetail.isEmulator() ?
                ((EmulatorClusterDetail)clusterDetail).getSparkHistoryEndpoint() :
                ClusterManagerEx.getInstance().getClusterConnectionString(clusterDetail.getName()) + "/sparkhistory";
                openUrlLink(clusterDetail,sparkHistoryUrl);
            }
        };
        final ActionView.Builder openHDInsightSparkHistoryView = new ActionView.Builder("Open Spark History UI")
                .title(s -> Optional.ofNullable(s).map(r -> description("hdinsight.open_history_ui.spark", ((AzResource) r).getName())).orElse(null))
                .enabled(s -> s instanceof AzResource);
        final Action<AzResource> openHDInsightSparkHistoryAction = new Action<>(OPEN_SPARK_HISTORY_UI, openHDInsightSparkHistory,openHDInsightSparkHistoryView);
        am.registerAction(openHDInsightSparkHistoryAction);


        final Consumer<AzResource> openStoragePortal = resource -> {
            if (resource instanceof StorageAccountNode) {
                final Account account = Azure.az(AzureAccount.class).account();
                final String portalUrl = account.getPortalUrl();
                StorageAccountNode storageAccountNode = (StorageAccountNode)resource;
                StorageAccount remote = storageAccountNode.getRemote(true);
                String resourceId = remote.resourceId();
                String tenantId = storageAccountNode.getSubscription().getTenantId();
                String url = portalUrl + "/#@" + tenantId + "/resource" + resourceId;
                DefaultLoader.getIdeHelper().openLinkInBrowser(url);
            }
        };
        final ActionView.Builder openStoragePortalView = new ActionView.Builder("Open Storage in Azure management Portal")
                .title(s -> Optional.ofNullable(s).map(r -> description("hdinsight.open_azure_storage_explorer.storage", ((AzResource) r).getName())).orElse(null))
                .enabled(s -> s instanceof AzResource);
        final Action<AzResource> openStoragePortalAction = new Action<>(OPEN_AZURE_STORAGE_MANAGEMENT_EXPLORER, openStoragePortal,openStoragePortalView);
        am.registerAction(openStoragePortalAction);
    }

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup serviceActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.REFRESH,
                ResourceCommonActionsContributor.OPEN_AZURE_REFERENCE_BOOK,
                "---",
                this.LINK_A_CLUSTER
        );
        am.registerGroup(SERVICE_ACTIONS, serviceActionGroup);

        final ActionGroup sparkActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.PIN,
                "---",
                ResourceCommonActionsContributor.REFRESH,
                ResourceCommonActionsContributor.OPEN_AZURE_REFERENCE_BOOK,
                ResourceCommonActionsContributor.OPEN_PORTAL_URL,
                "---",
                this.OPEN_AZURE_STORAGE_EXPLORER,
                this.OPEN_AZURE_EXPLORER_AMBARI,
                this.OPEN_AZURE_EXPLORER_JUPYTER,
                this.OPEN_SPARK_HISTORY_UI
        );
        am.registerGroup(SPARK_CLUSTER_ACTIONS, sparkActionGroup);//

        final ActionGroup sparkAdditionalActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.PIN,
                "---",
                ResourceCommonActionsContributor.REFRESH,
                ResourceCommonActionsContributor.OPEN_AZURE_REFERENCE_BOOK,
                ResourceCommonActionsContributor.OPEN_PORTAL_URL,
                "---",
                this.OPEN_AZURE_STORAGE_EXPLORER,
                this.OPEN_AZURE_EXPLORER_AMBARI,
                this.OPEN_AZURE_EXPLORER_JUPYTER,
                this.OPEN_SPARK_HISTORY_UI,
                "---",
                this.UNLINK_A_CLUSTER
        );
        am.registerGroup(SPARK_ADDITIONAL_CLUSTER_ACTIONS, sparkAdditionalActionGroup);//

        final ActionGroup storageActionGroup = new ActionGroup(
                this.OPEN_AZURE_STORAGE_MANAGEMENT_EXPLORER
        );
        am.registerGroup(HDINSIGHT_STORAGE_ACTIONS, storageActionGroup);

        final IActionGroup group = am.getGroup(ResourceCommonActionsContributor.RESOURCE_GROUP_CREATE_ACTIONS);
        group.addAction(GROUP_CREATE_HDInsight_SERVICE);
    }

    public void openUrlLink(IClusterDetail clusterDetail,String linkUrl){
        if (!StringHelper.isNullOrWhiteSpace(clusterDetail.getName())) {
            try {
                DefaultLoader.getIdeHelper().openLinkInBrowser(linkUrl);
            } catch (Exception exception) {
                DefaultLoader.getUIHelper().showError(exception.getMessage(), "HDInsight Explorer");
            }
        }
    }

    @Override
    public int getOrder() {
        return INITIALIZE_ORDER;
    }

}
