/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner.functions.deploy.ui;

import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.core.mvp.model.ResourceEx;
import com.microsoft.azuretools.core.mvp.ui.base.MvpView;

import java.util.List;
import java.util.Map;

public interface FunctionDeployMvpView extends MvpView {
    void beforeFillFunctionApps();

    void fillFunctionApps(@NotNull List<ResourceEx<FunctionApp>> webAppLists, boolean fillAppSettings);

    void beforeFillAppSettings();

    void fillAppSettings(Map<String, String> appSettings);
}
