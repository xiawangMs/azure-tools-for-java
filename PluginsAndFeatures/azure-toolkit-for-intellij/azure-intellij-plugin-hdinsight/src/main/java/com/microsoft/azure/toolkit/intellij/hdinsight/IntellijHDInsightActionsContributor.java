package com.microsoft.azure.toolkit.intellij.hdinsight;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.hdinsight.spark.HDInsightActionsContributor;
import com.microsoft.azure.toolkit.intellij.hdinsight.actions.OpenSparkJobViewAction;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.hdinsight.SparkCluster;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class IntellijHDInsightActionsContributor implements IActionsContributor {

    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiPredicate<AzResource, AnActionEvent> condition = (r, e) -> r instanceof SparkCluster;
        final BiConsumer<AzResource, AnActionEvent> handler = (c, e) -> OpenSparkJobViewAction.open((SparkCluster) c, e);
        am.registerHandler(HDInsightActionsContributor.OPEN_HDINSIGHT_JOB_VIEW, condition, handler);
    }

}