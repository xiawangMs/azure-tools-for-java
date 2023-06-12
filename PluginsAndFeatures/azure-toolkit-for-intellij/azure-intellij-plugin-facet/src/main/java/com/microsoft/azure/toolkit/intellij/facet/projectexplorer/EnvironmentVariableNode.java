/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.facet.projectexplorer;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import com.microsoft.azure.toolkit.intellij.connector.ResourceConnectionActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.IActionGroup;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

public class EnvironmentVariableNode extends AbstractTreeNode<Pair<String, String>> implements IAzureFacetNode {
    private boolean visible;

    public EnvironmentVariableNode(Project project, Pair<String, String> generated) {
        super(project, generated);
        this.visible = false;
    }

    @Override
    @Nonnull
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        return Collections.emptyList();
    }

    @Override
    protected void update(@Nonnull final PresentationData presentation) {
        final Pair<String, String> pair = this.getValue();
        presentation.setIcon(AllIcons.Nodes.Variable);
        presentation.addText(pair.getKey(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        presentation.setTooltip("Click to toggle visibility");
        if (visible) {
            presentation.addText(" = " + pair.getValue(), SimpleTextAttributes.GRAYED_ATTRIBUTES);
        } else {
            presentation.addText(" = ***", SimpleTextAttributes.GRAYED_ATTRIBUTES);
        }
    }

    @Override
    @Nullable
    public Object getData(@Nonnull String dataId) {
        return StringUtils.equalsIgnoreCase(dataId, "ACTION_SOURCE") ? this.getValue() : null;
    }

    @Nullable
    @Override
    public IActionGroup getActionGroup() {
        return new ActionGroup(
            ResourceConnectionActionsContributor.COPY_ENV_KEY,
            ResourceConnectionActionsContributor.COPY_ENV_PAIR
        );
    }

    @Override
    public void onClicked(Object event) {
        this.visible = !this.visible;
        this.rerender(false);
    }

    @Override
    public String toString() {
        return this.getValue().getKey();
    }
}