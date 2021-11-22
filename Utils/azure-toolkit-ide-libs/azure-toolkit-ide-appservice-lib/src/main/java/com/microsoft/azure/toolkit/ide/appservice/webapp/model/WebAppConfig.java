/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.webapp.model;

import com.microsoft.azure.toolkit.ide.appservice.model.AppServiceConfig;
import com.microsoft.azure.toolkit.lib.appservice.entity.AppServicePlanEntity;
import com.microsoft.azure.toolkit.lib.appservice.model.PricingTier;
import com.microsoft.azure.toolkit.lib.appservice.model.Runtime;
import com.microsoft.azure.toolkit.lib.appservice.service.IWebApp;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class WebAppConfig extends AppServiceConfig {
    public static final Runtime DEFAULT_RUNTIME = Runtime.LINUX_JAVA8_TOMCAT9;
    public static final PricingTier DEFAULT_PRICING_TIER = PricingTier.BASIC_B2;
    @Builder.Default
    protected Runtime runtime = DEFAULT_RUNTIME;

    public static WebAppConfig getWebAppDefaultConfig() {
        return WebAppConfig.builder()
                           .runtime(WebAppConfig.DEFAULT_RUNTIME)
                           .pricingTier(WebAppConfig.DEFAULT_PRICING_TIER)
                           .region(AppServiceConfig.getDefaultRegion()).build();
    }

    public static WebAppConfig fromRemote(IWebApp webApp) {
        return WebAppConfig.builder()
                .name(webApp.name())
                .resourceId(webApp.id())
                .servicePlan(AppServicePlanEntity.builder().id(webApp.entity().getAppServicePlanId()).build())
                .subscription(Subscription.builder().id(webApp.subscriptionId()).build())
                .resourceGroup(ResourceGroup.builder().name(webApp.resourceGroup()).build())
                .runtime(webApp.getRuntime())
                .region(webApp.entity().getRegion())
                .build();
    }
}
