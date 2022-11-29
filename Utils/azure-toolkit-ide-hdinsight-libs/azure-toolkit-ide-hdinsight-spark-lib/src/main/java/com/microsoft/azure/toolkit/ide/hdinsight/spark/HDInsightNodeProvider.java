package com.microsoft.azure.toolkit.ide.hdinsight.spark;

import com.microsoft.azure.toolkit.ide.common.IExplorerNodeProvider;
import com.microsoft.azure.toolkit.ide.common.component.AzureServiceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcons;
import com.microsoft.azure.toolkit.lib.hdinsight.AzureHDInsightService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.microsoft.azure.toolkit.lib.Azure.az;

public class HDInsightNodeProvider implements IExplorerNodeProvider {

    private static final String NAME = "HDInsight";
    private static final String ICON = AzureIcons.HDInsight.MODULE.getIconPath();

    @Nullable
    @Override
    public Object getRoot() {
        return az(AzureHDInsightService.class);
    }

    @Override
    public boolean accept(@Nonnull Object data, @Nullable Node<?> parent, ViewType type) {
        return data instanceof AzureHDInsightService;
    }

    @Nullable
    @Override
    public Node<?> createNode(@Nonnull Object data,@Nullable Node<?> parent,@Nonnull Manager manager) {
        if (data instanceof AzureHDInsightService) {
            final AzureHDInsightService service = ((AzureHDInsightService) data);
            return new Node<>(service).view(new AzureServiceLabelView<>(service, NAME, ICON))
                    .actions(HDInsightActionsContributor.SERVICE_ACTIONS);
        }
        return null;
    }

}