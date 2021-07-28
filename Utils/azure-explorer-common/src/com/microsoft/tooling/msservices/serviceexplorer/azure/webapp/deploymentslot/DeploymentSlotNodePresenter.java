/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.deploymentslot;

import java.io.IOException;

import com.microsoft.azure.management.appservice.DeploymentSlot;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azuretools.core.mvp.model.webapp.AzureWebAppMvpModel;
import com.microsoft.azuretools.core.mvp.ui.base.MvpPresenter;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.base.WebAppBaseState;

public class DeploymentSlotNodePresenter<V extends DeploymentSlotNodeView> extends MvpPresenter<V> {
    public void onStartDeploymentSlot(final String subscriptionId, final String webAppId,
                                      final String slotName) throws IOException {
        AzureWebAppMvpModel.getInstance().startDeploymentSlot(subscriptionId, webAppId, slotName);
        final DeploymentSlotNodeView view = getMvpView();
        if (!isViewDetached()) {
            view.renderNode(WebAppBaseState.RUNNING);
        }
    }

    public void onStopDeploymentSlot(final String subscriptionId, final String webAppId,
                                     final String slotName) throws IOException {
        AzureWebAppMvpModel.getInstance().stopDeploymentSlot(subscriptionId, webAppId, slotName);
        final DeploymentSlotNodeView view = getMvpView();
        if (!isViewDetached()) {
            view.renderNode(WebAppBaseState.STOPPED);
        }
    }

    public void onRestartDeploymentSlot(final String subscriptionId, final String webAppId,
                                        final String slotName) throws IOException {
        AzureWebAppMvpModel.getInstance().restartDeploymentSlot(subscriptionId, webAppId, slotName);
        final DeploymentSlotNodeView view = getMvpView();
        if (!isViewDetached()) {
            view.renderNode(WebAppBaseState.RUNNING);
        }
    }

    public void onRefreshNode(final String subscriptionId, final String webAppId,
                              final String slotName) throws Exception {
        final WebApp app = AzureWebAppMvpModel.getInstance().getWebAppById(subscriptionId, webAppId);
        final DeploymentSlot slot = app.deploymentSlots().getByName(slotName);
        final DeploymentSlotNodeView view = getMvpView();
        if (!isViewDetached()) {
            view.renderNode(WebAppBaseState.fromString(slot.state()));
        }
    }

    public void onSwapWithProduction(final String subscriptionId, final String webAppId,
                                     final String slotName) throws IOException {
        AzureWebAppMvpModel.getInstance().swapSlotWithProduction(subscriptionId, webAppId, slotName);
    }
}
