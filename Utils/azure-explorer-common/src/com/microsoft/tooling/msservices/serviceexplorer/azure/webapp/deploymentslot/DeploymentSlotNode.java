/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.deploymentslot;

import static com.microsoft.azuretools.telemetry.TelemetryConstants.DELETE_WEBAPP_SLOT;
import static com.microsoft.azuretools.telemetry.TelemetryConstants.OPERN_WEBAPP_SLOT_BROWSER;
import static com.microsoft.azuretools.telemetry.TelemetryConstants.RESTART_WEBAPP_SLOT;
import static com.microsoft.azuretools.telemetry.TelemetryConstants.SHOW_WEBAPP_SLOT_PROP;
import static com.microsoft.azuretools.telemetry.TelemetryConstants.START_WEBAPP_SLOT;
import static com.microsoft.azuretools.telemetry.TelemetryConstants.STOP_WEBAPP_SLOT;
import static com.microsoft.azuretools.telemetry.TelemetryConstants.SWAP_WEBAPP_SLOT;
import static com.microsoft.azuretools.telemetry.TelemetryConstants.WEBAPP;

import com.microsoft.tooling.msservices.serviceexplorer.WrappedTelemetryNodeActionListener;
import java.io.IOException;
import java.util.List;

import com.microsoft.azuretools.azurecommons.helpers.AzureCmdException;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.serviceexplorer.NodeAction;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureNodeActionPromptListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.base.WebAppBaseNode;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.base.WebAppBaseState;

public class DeploymentSlotNode extends WebAppBaseNode implements DeploymentSlotNodeView {
    private static final String ACTION_SWAP_WITH_PRODUCTION = "Swap with production";
    private static final String LABEL = "Slot";
    private static final String DELETE_SLOT_PROMPT_MESSAGE = "This operation will delete the Deployment Slot: %s.\n"
        + "Are you sure you want to continue?";
    private static final String DELETE_SLOT_PROGRESS_MESSAGE = "Deleting Deployment Slot";
    private final DeploymentSlotNodePresenter presenter;
    protected final String webAppId;
    protected final String webAppName;
    protected final String slotName;

    public DeploymentSlotNode(final String slotId, final String webAppId, final String webAppName,
                              final DeploymentSlotModule parent, final String name, final String state, final String os,
                              final String subscriptionId, final String hostName) {
        super(slotId, name, LABEL, parent, subscriptionId, hostName, os, state);
        this.webAppId = webAppId;
        this.webAppName = webAppName;
        this.slotName = name;
        this.presenter = new DeploymentSlotNodePresenter();
        this.presenter.onAttachView(this);
        loadActions();
    }

    public String getWebAppId() {
        return this.webAppId;
    }

    public String getWebAppName() {
        return this.webAppName;
    }

    @Override
    public List<NodeAction> getNodeActions() {
        getNodeActionByName(ACTION_SWAP_WITH_PRODUCTION).setEnabled(this.state == WebAppBaseState.RUNNING);
        return super.getNodeActions();
    }

    @Override
    protected void loadActions() {
        // todo: why only the stop action has icon?
        addAction(ACTION_STOP, getIcon(this.os, this.label, WebAppBaseState.STOPPED),
            new WrappedTelemetryNodeActionListener(WEBAPP, STOP_WEBAPP_SLOT,
                createBackgroundActionListener("Stopping Deployment Slot", () -> stop())));
        addAction(ACTION_START, new WrappedTelemetryNodeActionListener(WEBAPP, START_WEBAPP_SLOT,
            createBackgroundActionListener("Starting Deployment Slot", () -> start())));
        addAction(ACTION_RESTART, new WrappedTelemetryNodeActionListener(WEBAPP, RESTART_WEBAPP_SLOT,
            createBackgroundActionListener("Restarting Deployment Slot", () -> restart())));
        addAction(ACTION_SWAP_WITH_PRODUCTION, new WrappedTelemetryNodeActionListener(WEBAPP, SWAP_WEBAPP_SLOT,
            createBackgroundActionListener("Swapping with Production", () -> swapWithProduction())));
        addAction(ACTION_OPEN_IN_BROWSER, new WrappedTelemetryNodeActionListener(WEBAPP, OPERN_WEBAPP_SLOT_BROWSER,
            new NodeActionListener() {
            @Override
            protected void actionPerformed(NodeActionEvent e) {
                DefaultLoader.getUIHelper().openInBrowser("http://" + hostName);
            }
        }));
        addAction(ACTION_DELETE, new DeleteDeploymentSlotAction());
        addAction(ACTION_SHOW_PROPERTY, new WrappedTelemetryNodeActionListener(WEBAPP, SHOW_WEBAPP_SLOT_PROP,
            new NodeActionListener() {
                @Override
                protected void actionPerformed(NodeActionEvent e) throws AzureCmdException {
                    DefaultLoader.getUIHelper().openDeploymentSlotPropertyView(DeploymentSlotNode.this);
                }
            }));

        super.loadActions();
    }

    @Override
    protected void onNodeClick(NodeActionEvent e) {
        // RefreshableNode refresh itself when the first time being clicked.
        // The deployment slot node is just a single node for the time being.
        // Override the function to do noting to disable the auto refresh functionality.
    }

    private void start() {
        try {
            presenter.onStartDeploymentSlot(this.subscriptionId, this.webAppId, this.slotName);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Error handling
        }
    }

    private void stop() {
        try {
            presenter.onStopDeploymentSlot(this.subscriptionId, this.webAppId, this.slotName);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Error handling
        }
    }

    private void restart() {
        try {
            presenter.onRestartDeploymentSlot(this.subscriptionId, this.webAppId, this.slotName);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Error handling
        }
    }

    private void swapWithProduction() {
        try {
            presenter.onSwapWithProduction(this.subscriptionId, this.webAppId, this.slotName);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Error handling
        }
    }

    @Override
    protected void refreshItems() {
        try {
            presenter.onRefreshNode(this.subscriptionId, this.webAppId, this.slotName);
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: Error handling
        }
    }

    private class DeleteDeploymentSlotAction extends AzureNodeActionPromptListener {
        DeleteDeploymentSlotAction() {
            super(DeploymentSlotNode.this, String.format(DELETE_SLOT_PROMPT_MESSAGE, getName()),
                DELETE_SLOT_PROGRESS_MESSAGE);
        }

        @Override
        protected void azureNodeAction(NodeActionEvent e) {
            getParent().removeNode(getSubscriptionId(), getName(), DeploymentSlotNode.this);
        }

        @Override
        protected void onSubscriptionsChanged(NodeActionEvent e) {
        }

        @Override
        protected String getServiceName(NodeActionEvent event) {
            return WEBAPP;
        }

        @Override
        protected String getOperationName(NodeActionEvent event) {
            return DELETE_WEBAPP_SLOT;
        }
    }
}
