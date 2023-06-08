package com.microsoft.azure.toolkit.intellij.facet.projectexplorer;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ui.SimpleTextAttributes;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcons;
import com.microsoft.azure.toolkit.intellij.common.IntelliJAzureIcons;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.AzureModule;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

public class AzureRootNode extends AbstractTreeNode<AzureModule> {
    private final AbstractTreeNode<?> dotAzureDirNode;

    public AzureRootNode(final AzureModule module, final AbstractTreeNode<?> dotAzureDirNode) {
        super(module.getProject(), module);
        this.dotAzureDirNode = dotAzureDirNode;
    }

    @Override
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        final ArrayList<AbstractTreeNode<?>> result = new ArrayList<>();
        final AzureModule module = this.getValue();
        result.add(this.dotAzureDirNode);
        result.add(new LocalConnectionsNode(module));
        return result;
    }

    @Override
    protected void update(@Nonnull final PresentationData presentation) {
        presentation.addText("Azure", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
        presentation.setTooltip("Manage connected Azure resources here.");
        presentation.setIcon(IntelliJAzureIcons.getIcon(AzureIcons.Common.AZURE));
    }
}
