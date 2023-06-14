/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.facet.projectexplorer;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.microsoft.azure.toolkit.intellij.connector.ResourceConnectionActionsContributor;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.AzureModule;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.action.IActionGroup;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ConnectionsNode extends AbstractTreeNode<AzureModule> implements IAzureFacetNode {

    public ConnectionsNode(@Nonnull final AzureModule module) {
        super(module.getProject(), module);
    }

    @Override
    @Nonnull
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        final AzureModule module = Objects.requireNonNull(this.getValue());
        final List<ConnectionNode> children = Optional.of(module).stream()
            .map(AzureModule::getDefaultProfile).filter(Objects::nonNull)
            .flatMap(p -> p.getConnections().stream())
            .map(r -> new ConnectionNode(module.getProject(), module, r))
            .toList();
        if (CollectionUtils.isNotEmpty(children)) {
            return children;
        }
        final ArrayList<AbstractTreeNode<?>> nodes = new ArrayList<>();
        nodes.add(new ActionNode<>(module.getProject(), ResourceConnectionActionsContributor.CONNECT_TO_MODULE, module));
        return nodes;
    }

    @Override
    protected void update(@Nonnull final PresentationData presentation) {
        presentation.setIcon(AllIcons.Nodes.HomeFolder);
        presentation.setPresentableText("Resource Connections");
        presentation.setTooltip("The dependent/connected resources.");
    }

    @Override
    @Nullable
    public Object getData(@Nonnull String dataId) {
        return StringUtils.equalsIgnoreCase(dataId, "ACTION_SOURCE") ? this.getValue() : null;
    }

    @Nullable
    @Override
    public IActionGroup getActionGroup() {
        return AzureActionManager.getInstance().getGroup(ResourceConnectionActionsContributor.EXPLORER_MODULE_LOCAL_CONNECTIONS_ACTIONS);
    }

    @Override
    public String toString() {
        return "Resource Connections";
    }
}