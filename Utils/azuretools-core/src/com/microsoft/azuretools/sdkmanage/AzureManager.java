/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.sdkmanage;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.applicationinsights.v2015_05_01.implementation.InsightsManager;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.implementation.AppPlatformManager;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.Tenant;
import com.microsoft.azuretools.adauth.PromptBehavior;
import com.microsoft.azuretools.authmanage.CommonSettings;
import com.microsoft.azuretools.authmanage.Environment;
import com.microsoft.azuretools.authmanage.SubscriptionManager;
import com.microsoft.azuretools.utils.Pair;

import java.io.IOException;
import java.util.List;

public interface AzureManager {
    Azure getAzure(String sid) throws IOException;

    AppPlatformManager getAzureSpringCloudClient(String sid) throws IOException;

    InsightsManager getInsightsManager(String sid) throws IOException;

    List<Subscription> getSubscriptions() throws IOException;

    List<Pair<Subscription, Tenant>> getSubscriptionsWithTenant() throws IOException;

    Settings getSettings();

    SubscriptionManager getSubscriptionManager();

    void drop() throws IOException;

    String getCurrentUserId() throws IOException;

    String getAccessToken(String tid, String resource, PromptBehavior promptBehavior) throws IOException;

    String getManagementURI() throws IOException;

    String getStorageEndpointSuffix();

    String getTenantIdBySubscription(String subscriptionId) throws IOException;

    String getScmSuffix();

    Environment getEnvironment();

    String getPortalUrl();

    default String getAccessToken(String tid) throws IOException {
        return getAccessToken(tid, CommonSettings.getAdEnvironment().resourceManagerEndpoint(), PromptBehavior.Auto);
    }
}
