/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.facet.projectexplorer;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.microsoft.azure.toolkit.intellij.common.action.IntellijAzureActionManager;
import com.microsoft.azure.toolkit.intellij.connector.ResourceConnectionActionsContributor;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.AzureModule;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Collection;
import java.util.Collections;

public class AddResourceConnectorNode extends AbstractTreeNode<AzureModule>
    implements IAzureFacetNode {

    protected AddResourceConnectorNode(AzureModule value) {
        super(value.getProject(), value);
    }

    @Override
    public @Nonnull Collection<? extends AbstractTreeNode<?>> getChildren() {
        return Collections.emptyList();
    }

    @Override
    protected void update(@Nonnull PresentationData presentation) {
        presentation.setPresentableText("Click to Connect to Azure Resources");
        presentation.setTooltip("Connect your project to Azure");
        presentation.setForcedTextForeground(UIManager.getColor("Hyperlink.linkColor"));
    }

    @Override
    public @Nullable Object getData(@Nonnull String dataId) {
        return StringUtils.equalsIgnoreCase(dataId, "ACTION_SOURCE") ? this.getValue() : null;
    }

    @Override
    public void onClicked(Object event) {
        final Action<AzureModule> action = IntellijAzureActionManager.getInstance().getAction(ResourceConnectionActionsContributor.CONNECT_TO_MODULE);
        action.handle(getValue(), event);
    }
}
