package com.microsoft.azure.toolkit.lib.hdinsight;

import com.azure.resourcemanager.hdinsight.models.Cluster;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResourceModule;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class SparkCluster extends AbstractAzResource<SparkCluster, HDInsightServiceSubscription, Cluster> {

    /**
     * copy constructor
     */
    protected SparkCluster(@Nonnull SparkCluster origin) {
        super(origin);
    }

    protected SparkCluster(@Nonnull String name, @Nonnull String resourceGroup, @Nonnull SparkClusterModule module) {
        super(name, resourceGroup, module);
    }

    protected SparkCluster(@Nonnull Cluster remote, @Nonnull SparkClusterModule module) {
        super(remote.name(), ResourceId.fromString(remote.id()).resourceGroupName(), module);
    }


    @NotNull
    @Override
    public List<AbstractAzResourceModule<?, ?, ?>> getSubModules() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public String loadStatus(@NotNull Cluster remote) {
        return null;
    }

}
