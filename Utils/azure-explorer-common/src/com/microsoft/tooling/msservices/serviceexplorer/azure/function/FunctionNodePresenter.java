/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.function;

import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azuretools.core.mvp.model.function.AzureFunctionMvpModel;
import com.microsoft.azuretools.core.mvp.ui.base.MvpPresenter;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.base.WebAppBaseState;

import java.io.IOException;

public class FunctionNodePresenter<V extends FunctionNodeView> extends MvpPresenter<V> {
    public void onStartFunctionApp(String subscriptionId, String appId) throws IOException {
        AzureFunctionMvpModel.getInstance().startFunction(subscriptionId, appId);
        renderFunctionStatus(subscriptionId, appId);
    }

    public void onRestartFunctionApp(String subscriptionId, String appId) throws IOException {
        AzureFunctionMvpModel.getInstance().restartFunction(subscriptionId, appId);
        renderFunctionStatus(subscriptionId, appId);
    }

    public void onStopFunctionApp(String subscriptionId, String appId) throws IOException {
        AzureFunctionMvpModel.getInstance().stopFunction(subscriptionId, appId);
        renderFunctionStatus(subscriptionId, appId);
    }

    public void onRefreshFunctionNode(String subscriptionId, String appId) throws IOException {
        final FunctionNodeView view = getMvpView();
        if (view != null) {
            view.renderSubModules(AzureFunctionMvpModel.getInstance().listFunctionEnvelopeInFunctionApp(subscriptionId, appId));
        }
    }

    private void renderFunctionStatus(String subscriptionId, String appId) throws IOException {
        final FunctionNodeView view = getMvpView();
        if (view != null) {
            final FunctionApp target = AzureFunctionMvpModel.getInstance().getFunctionById(subscriptionId, appId);
            view.renderNode(WebAppBaseState.fromString(target.state()));
        }
    }
}
