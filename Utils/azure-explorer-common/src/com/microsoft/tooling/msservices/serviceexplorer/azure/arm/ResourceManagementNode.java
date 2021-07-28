/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.arm;

import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azuretools.azurecommons.helpers.AzureCmdException;
import com.microsoft.azuretools.core.mvp.model.ResourceEx;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import com.microsoft.azuretools.utils.AzureUIRefreshCore;
import com.microsoft.azuretools.utils.AzureUIRefreshEvent;
import com.microsoft.azuretools.utils.AzureUIRefreshListener;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.RefreshableNode;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureNodeActionPromptListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.arm.deployments.DeploymentNode;
import java.io.IOException;
import java.util.List;

public class ResourceManagementNode extends RefreshableNode implements ResourceManagementNodeView {

    private static final String ICON_RESOURCE_MANAGEMENT = "arm_resourcegroup.png";
    private static final String ACTION_DELETE = "Delete";
    private static final String DELETE_RESOURCE_GROUP_PROMPT_MESSAGE = "This operation will delete the Resource "
        + "Group: %s. Are you sure you want to continue?";
    private static final String DELETE_RESOURCE_GROUP_PROGRESS_MESSAGE = "Deleting Resource Group";
    private final ResourceManagementNodePresenter rmNodePresenter;
    private final String sid;
    private final String rgName;
    private final Object listenerObj = new Object();

    public ResourceManagementNode(ResourceManagementModule parent, String subscriptionId, ResourceGroup resourceGroup) {
        super(resourceGroup.id(), resourceGroup.name(), parent, ICON_RESOURCE_MANAGEMENT, true);
        rmNodePresenter = new ResourceManagementNodePresenter();
        rmNodePresenter.onAttachView(this);
        sid = subscriptionId;
        rgName = resourceGroup.name();
        loadActions();
    }

    @Override
    protected void refreshItems() throws AzureCmdException {
        try {
            rmNodePresenter.onModuleRefresh(sid, rgName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void loadActions() {
        addAction(ACTION_DELETE, new DeleteResourceGroupAction());
        super.loadActions();
    }

    @Override
    public void renderChildren(List<ResourceEx<Deployment>> resourceExes) {
        for (final ResourceEx<Deployment> resourceEx : resourceExes) {
            final Deployment deployment = resourceEx.getResource();
            final DeploymentNode node = new DeploymentNode(this, resourceEx.getSubscriptionId(), deployment);
            addChildNode(node);
        }
    }

    @Override
    public void removeNode(String sid, String id, Node node) {
        EventUtil.executeWithLog(TelemetryConstants.ARM, TelemetryConstants.DELETE_DEPLOYMENT, (operation -> {
            rmNodePresenter.onDeleteDeployment(sid, id);
            removeDirectChildNode(node);
        }), (e) -> {
            DefaultLoader.getUIHelper()
                .showException("An error occurred while attempting to delete the resource group ",
                    e, "Azure Services Explorer - Error Deleting Resource Group", false, true);
        });
    }

    public String getSid() {
        return sid;
    }

    public String getRgName() {
        return rgName;
    }

    private class DeleteResourceGroupAction extends AzureNodeActionPromptListener {
        DeleteResourceGroupAction() {
            super(ResourceManagementNode.this, String.format(DELETE_RESOURCE_GROUP_PROMPT_MESSAGE, rgName),
                DELETE_RESOURCE_GROUP_PROGRESS_MESSAGE);
        }

        @Override
        protected void azureNodeAction(NodeActionEvent e) {
            getParent().removeNode(sid, rgName, ResourceManagementNode.this);
        }

        @Override
        protected void onSubscriptionsChanged(NodeActionEvent e) {
        }
    }

}
