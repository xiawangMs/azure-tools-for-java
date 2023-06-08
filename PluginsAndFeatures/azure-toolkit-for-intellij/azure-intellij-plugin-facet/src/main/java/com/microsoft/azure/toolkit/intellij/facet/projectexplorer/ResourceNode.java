package com.microsoft.azure.toolkit.intellij.facet.projectexplorer;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.ide.common.component.NodeView;
import com.microsoft.azure.toolkit.intellij.common.IntelliJAzureIcons;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;

public class ResourceNode extends AbstractTreeNode<Node<?>> {
    public ResourceNode(@Nonnull Project project, final Node<?> node) {
        super(project, node);
        final NodeView view = node.view();
        view.setRefresher(new NodeView.Refresher() {
            @Override
            public void refreshView() {
                ResourceNode.this.update();
            }
        });
    }

    @Override
    @Nonnull
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        final Node<?> node = this.getValue();
        return node.getChildren().stream().map(n -> new ResourceNode(this.getProject(), n)).toList();
    }

    @Override
    protected void update(@Nonnull final PresentationData presentation) {
        final Node<?> node = this.getValue();
        final NodeView view = node.view();
        presentation.addText(view.getLabel(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        presentation.setIcon(IntelliJAzureIcons.getIcon(view.getIcon()));
        Optional.ofNullable(view.getDescription()).ifPresent(d -> presentation.addText(" " + d, SimpleTextAttributes.GRAYED_ATTRIBUTES));
    }
}