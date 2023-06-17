/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.facet.projectexplorer;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import com.microsoft.azure.toolkit.ide.common.IExplorerNodeProvider;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceIconProvider;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.intellij.common.IntelliJAzureIcons;
import com.microsoft.azure.toolkit.intellij.connector.Connection;
import com.microsoft.azure.toolkit.intellij.connector.Resource;
import com.microsoft.azure.toolkit.intellij.connector.ResourceConnectionActionsContributor;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.AzureModule;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.Profile;
import com.microsoft.azure.toolkit.intellij.explorer.AzureExplorer;
import com.microsoft.azure.toolkit.lib.auth.AzureToolkitAuthenticationException;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.action.IActionGroup;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class ConnectionNode extends AbstractTreeNode<Connection<?, ?>> implements IAzureFacetNode {
    private final AzureModule module;

    public ConnectionNode(@Nonnull Project project, @Nonnull AzureModule module, @Nonnull final Connection<?, ?> connection) {
        super(project, connection);
        this.module = module;
    }

    @Override
    @Nonnull
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        final Connection<?, ?> connection = this.getValue();
        if (!connection.validate(getProject())) {
            return Collections.singletonList(new ActionNode<>(this.myProject, ResourceConnectionActionsContributor.FIX_CONNECTION, connection));
        }
        final ArrayList<AbstractTreeNode<?>> children = new ArrayList<>();
        final AbstractTreeNode<?> resourceNode = getResourceNode(connection);
        final Profile profile = Objects.requireNonNull(module.getDefaultProfile());
        final EnvironmentVariablesNode environmentVariablesNode = new EnvironmentVariablesNode(this.getProject(), profile, connection);
        children.add(resourceNode);
        children.add(environmentVariablesNode);
        return children;
    }

    private AbstractTreeNode<?> getResourceNode(Connection<?, ?> connection) {
        try {
            final Object resource = connection.getResource().getData();
            final Node<?> node = AzureExplorer.manager.createNode(resource, null, IExplorerNodeProvider.ViewType.APP_CENTRIC);
            return new ResourceNode(this.getProject(), node);
        } catch (Throwable e) {
            e.printStackTrace();
            e = ExceptionUtils.getRootCause(e);
            if (e instanceof AzureToolkitAuthenticationException) {
                final Action<Object> signin = AzureActionManager.getInstance().getAction(Action.AUTHENTICATE).bind(connection).withLabel("Sign in to manage connected resource");
                return new ActionNode<>(this.myProject, signin);
            } else {
                return new ExceptionNode(this.myProject, e);
            }
        }
    }

    @Override
    protected void update(@Nonnull final PresentationData presentation) {
        final Connection<?, ?> connection = this.getValue();
        final Resource<?> resource = connection.getResource();
        final ResourceId resourceId = ResourceId.fromString(resource.getDataId());
        final boolean isValid = connection.validate(getProject());
        presentation.setIcon(isValid ? IntelliJAzureIcons.getIcon(AzureResourceIconProvider.getResourceIconPath(resourceId)) : AllIcons.General.Warning);
        presentation.addText(resource.getDefinition().getTitle(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        if (isValid) {
            presentation.addText(" :" + resource.getName(), SimpleTextAttributes.GRAYED_ATTRIBUTES);
            presentation.addText(" (" + connection.getEnvPrefix() + "_*)", SimpleTextAttributes.GRAYED_ATTRIBUTES);
        } else {
            presentation.addText(" (" + connection.getEnvPrefix() + "_*)", SimpleTextAttributes.GRAYED_ATTRIBUTES);
            final String message = connection.getResource().isValidResource() ? "Invalid Consumer" : "Invalid Resource";
            presentation.addText(String.format(" (%s)", message), SimpleTextAttributes.ERROR_ATTRIBUTES);
        }
        // presentation.setIcon(AllIcons.CodeWithMe.CwmInvite);
        // presentation.setIcon(AllIcons.Debugger.ThreadStates.Socket);
    }

    @Override
    @Nullable
    public Object getData(@Nonnull String dataId) {
        return StringUtils.equalsIgnoreCase(dataId, "ACTION_SOURCE") ? this.getValue() : null;
    }

    @Nullable
    @Override
    public IActionGroup getActionGroup() {
        return AzureActionManager.getInstance().getGroup(ResourceConnectionActionsContributor.CONNECTION_ACTIONS);
    }

    @Override
    public boolean isAlwaysExpand() {
        return true;
    }

    @Override
    public String toString() {
        return "->" + this.getValue().getResource().getName();
    }
}