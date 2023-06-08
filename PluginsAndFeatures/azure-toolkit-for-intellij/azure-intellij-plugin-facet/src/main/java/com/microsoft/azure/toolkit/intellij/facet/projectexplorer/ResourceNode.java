/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.facet.projectexplorer;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.util.ui.tree.TreeUtil;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.ide.common.component.NodeView;
import com.microsoft.azure.toolkit.intellij.common.IntelliJAzureIcons;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.IActionGroup;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class ResourceNode extends AbstractTreeNode<Node<?>> implements IAzureFacetNode, NodeView.Refresher {
    public ResourceNode(@Nonnull Project project, final Node<?> node) {
        super(project, node);
        final NodeView view = node.view();
        view.setRefresher(this);
    }

    @Override
    @Nonnull
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        final Node<?> node = this.getValue();
        final ArrayList<AbstractTreeNode<?>> children = new ArrayList<>(node.getChildren().stream().map(n -> new ResourceNode(this.getProject(), n)).toList());
        if (node.hasMoreChildren()) {
            final Action<Object> loadMoreAction = new Action<>(Action.Id.of("user/common.load_more"))
                .withHandler(i -> node.loadMoreChildren())
                .withLabel("load more")
                .withAuthRequired(true);
            children.add(new ActionNode<>(this.myProject, loadMoreAction));
        }
        return children;
    }

    @Override
    protected void update(@Nonnull final PresentationData presentation) {
        final Node<?> node = this.getValue();
        final NodeView view = node.view();
        presentation.addText(view.getLabel(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        presentation.setIcon(IntelliJAzureIcons.getIcon(view.getIcon()));
        Optional.ofNullable(view.getDescription()).ifPresent(d -> presentation.addText(" " + d, SimpleTextAttributes.GRAYED_ATTRIBUTES));
    }

    @Override
    @Nullable
    public Object getData(@Nonnull String dataId) {
        if (StringUtils.equalsIgnoreCase(dataId, Action.SOURCE)) {
            return Optional.ofNullable(getValue()).map(Node::data).orElse(null);
        }
        return null;
    }

    @Override
    public void onDoubleClicked(Object event) {
        Optional.ofNullable(this.getValue()).ifPresent(n -> n.triggerDoubleClickAction(event));
    }

    @Override
    public void onClicked(Object event) {
        Optional.ofNullable(this.getValue()).ifPresent(n -> n.triggerClickAction(event));
    }

    @Override
    @Nullable
    public IActionGroup getActionGroup() {
        return Optional.ofNullable(getValue()).map(Node::actions).orElse(null);
    }

    @Override
    public void refreshView() {
        refresh(false);
    }

    @Override
    public void refreshChildren(boolean... incremental) {
        refresh(true);
    }

    private void refresh(boolean updateStructure) {
        final AbstractProjectViewPane pane = ProjectView.getInstance(getProject()).getCurrentProjectViewPane();
        final AsyncTreeModel model = (AsyncTreeModel) pane.getTree().getModel();
        final DefaultMutableTreeNode node = TreeUtil.findNodeWithObject((DefaultMutableTreeNode) model.getRoot(), ResourceNode.this);
        if (Objects.nonNull(node)) {
            final TreePath path = TreeUtil.getPath((TreeNode) model.getRoot(), node);
            pane.updateFrom(path, false, updateStructure);
        }
    }
}