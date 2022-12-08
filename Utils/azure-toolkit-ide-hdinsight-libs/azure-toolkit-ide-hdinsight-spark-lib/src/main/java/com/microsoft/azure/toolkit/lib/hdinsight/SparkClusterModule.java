package com.microsoft.azure.toolkit.lib.hdinsight;

import com.azure.resourcemanager.hdinsight.HDInsightManager;
import com.azure.resourcemanager.hdinsight.models.Cluster;
import com.azure.resourcemanager.hdinsight.models.Clusters;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResourceModule;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SparkClusterModule extends AbstractAzResourceModule<SparkCluster, HDInsightServiceSubscription, Cluster> {

    public static final String NAME = "clusters";

    public SparkClusterModule(@Nonnull HDInsightServiceSubscription parent) {
        super(NAME, parent);
    }

    @Nonnull
    @Override
    @AzureOperation(name = "resource.load_resources_in_azure.type", params = {"this.getResourceTypeName()"}, type = AzureOperation.Type.REQUEST)
    protected Stream<Cluster> loadResourcesFromAzure() {
        //log.debug("[{}]:loadResourcesFromAzure()", this.getName());
        return Optional.ofNullable( this.getClient()).map((c) -> {
            List<Cluster> sourceList = c.list().stream().collect(Collectors.toList());
            List<Cluster> resultList = new ArrayList<Cluster>();

            // Remove duplicate clusters that share the same cluster name
            HashSet<String> clusterIdSet = new HashSet<>();
            for (Cluster cluster : sourceList)
                if (clusterIdSet.add(cluster.id()))
                    resultList.add(cluster);

            return resultList.stream();
        }).orElse(Stream.empty());
    }

    @Nullable
    @Override
    public Clusters getClient() {
        return Optional.ofNullable(this.parent.getRemote()).map(HDInsightManager::clusters).orElse(null);
    }

    public SparkClusterModule(@NotNull String name, @NotNull HDInsightServiceSubscription parent) {
        super(name, parent);
    }

//    @Nullable
//    @Override
//    public SparkCluster get(@Nonnull String name, @Nullable String resourceGroup) {
//        resourceGroup = StringUtils.firstNonBlank(resourceGroup, this.getParent().getResourceGroupName());
//        if (StringUtils.isBlank(resourceGroup) || StringUtils.equalsIgnoreCase(resourceGroup, RESOURCE_GROUP_PLACEHOLDER)) {
//            return this.list().stream().filter(c -> StringUtils.equalsIgnoreCase(name, c.getName())).findAny().orElse(null);
//        }
//        return super.get(name, resourceGroup);
//    }

    @NotNull
    @Override
    protected SparkCluster newResource(@NotNull Cluster cluster) {
        return new SparkCluster(cluster,this);
    }

    @NotNull
    @Override
    protected SparkCluster newResource(@NotNull String name, @Nullable String resourceGroupName) {
        return new SparkCluster(name, Objects.requireNonNull(resourceGroupName),this);
    }

    @Nonnull
    @Override
    public String getResourceTypeName() {
        return "HDInsight Clusters";
    }
}
