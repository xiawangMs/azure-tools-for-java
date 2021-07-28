/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner.webapp.webappconfig.ui;

import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.DeploymentSlot;
import com.microsoft.azure.management.appservice.PricingTier;
import com.microsoft.azure.management.appservice.RuntimeStack;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.resources.Location;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azuretools.core.mvp.model.ResourceEx;
import com.microsoft.azuretools.core.mvp.model.webapp.JdkModel;
import com.microsoft.azuretools.core.mvp.ui.base.MvpView;
import com.microsoft.azuretools.utils.WebAppUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WebAppDeployMvpView extends MvpView {

    void renderWebAppsTable(@NotNull List<ResourceEx<WebApp>> webAppLists);

    void enableDeploymentSlotPanel();

    void fillDeploymentSlots(@NotNull List<DeploymentSlot> slots);

    void fillSubscription(@NotNull List<Subscription> subscriptions);

    void fillResourceGroup(@NotNull List<ResourceGroup> resourceGroups);

    void fillAppServicePlan(@NotNull List<AppServicePlan> appServicePlans);

    void fillLocation(@NotNull List<Location> locations);

    void fillPricingTier(@NotNull List<PricingTier> prices);

    void fillWebContainer(@NotNull List<WebAppUtils.WebContainerMod> webContainers);

    void fillJdkVersion(@NotNull List<JdkModel> jdks);

    void fillLinuxRuntime(@NotNull List<RuntimeStack> linuxRuntimes);
}
