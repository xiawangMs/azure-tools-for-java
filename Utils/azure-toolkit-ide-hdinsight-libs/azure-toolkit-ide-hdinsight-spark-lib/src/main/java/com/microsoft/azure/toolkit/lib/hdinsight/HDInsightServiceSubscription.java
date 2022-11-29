package com.microsoft.azure.toolkit.lib.hdinsight;

import com.azure.resourcemanager.hdinsight.HDInsightManager;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResourceModule;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzServiceSubscription;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public class HDInsightServiceSubscription extends AbstractAzServiceSubscription<HDInsightServiceSubscription, HDInsightManager> {

    @Nonnull
    private final String subscriptionId;

    protected HDInsightServiceSubscription(@NotNull String subscriptionId, @NotNull AzureHDInsightService service) {
        super(subscriptionId, service);
        this.subscriptionId = subscriptionId;
    }

    protected HDInsightServiceSubscription(@Nonnull HDInsightManager manager, @Nonnull AzureHDInsightService service) {
        this(manager.serviceClient().getSubscriptionId(), service);
    }

    @NotNull
    @Override
    public List<AbstractAzResourceModule<?, ?, ?>> getSubModules() {
        return null;
    }
}
