/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.facet.projectexplorer;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.action.IntellijAzureActionManager;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.view.IView;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Collection;
import java.util.Collections;

public class ActionNode<T> extends AbstractTreeNode<T> implements IAzureFacetNode {

    private final Action<T> action;

    protected ActionNode(@Nonnull Project project, T value, Action<T> action) {
        super(project, value);
        this.action = action;
    }

    protected ActionNode(@Nonnull Project project, T value, Action.Id<T> actionId) {
        super(project, value);
        this.action = IntellijAzureActionManager.getInstance().getAction(actionId);
    }

    @Override
    public @Nonnull Collection<? extends AbstractTreeNode<?>> getChildren() {
        return Collections.emptyList();
    }

    @Override
    protected void update(@Nonnull PresentationData presentation) {
        final IView.Label view = action.getView(this.getValue());
        presentation.setPresentableText("Click to " + StringUtils.uncapitalize(view.getLabel()));
        presentation.setTooltip(view.getDescription());
        presentation.setForcedTextForeground(UIManager.getColor("Hyperlink.linkColor"));
    }

    @Override
    public void onClicked(Object event) {
        this.action.handle(getValue(), event);
    }

    @Override
    public @Nullable Object getData(@Nonnull String dataId) {
        return StringUtils.equalsIgnoreCase(dataId, "ACTION_SOURCE") ? this.getValue() : null;
    }
}
