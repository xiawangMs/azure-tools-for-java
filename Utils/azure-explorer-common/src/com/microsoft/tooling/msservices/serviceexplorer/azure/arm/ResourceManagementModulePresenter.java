/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.arm;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.core.mvp.model.AzureMvpModel;
import com.microsoft.azuretools.core.mvp.model.ResourceEx;
import com.microsoft.azuretools.core.mvp.ui.base.MvpPresenter;
import com.microsoft.azuretools.utils.CanceledByUserException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.schedulers.Schedulers;

public class ResourceManagementModulePresenter<V extends ResourceManagementModuleView> extends MvpPresenter<V> {

    public void onModuleRefresh() throws IOException, CanceledByUserException {
        final ResourceManagementModuleView view = getMvpView();
        if (view != null) {
            view.renderChildren(AzureMvpModel.getInstance().getResourceGroups(true));
        }
    }

    public void onDeleteResourceGroup(String sid, String rgName) throws IOException {
        Azure azure = AuthMethodManager.getInstance().getAzureClient(sid);
        azure.resourceGroups().deleteByName(rgName);
    }
}
