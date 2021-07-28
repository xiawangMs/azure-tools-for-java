/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner.functions.library;

import com.microsoft.azure.common.function.configurations.RuntimeConfiguration;
import com.microsoft.azure.common.project.IProject;
import com.microsoft.azure.management.Azure;

import java.io.IOException;
import java.util.Map;

public interface IAppServiceContext {
    String getDeploymentStagingDirectoryPath();

    String getSubscription();

    String getAppName();

    String getResourceGroup();

    RuntimeConfiguration getRuntime();

    String getRegion();

    String getPricingTier();

    String getAppServicePlanResourceGroup();

    String getAppServicePlanName();

    Map getAppSettings();

    String getDeploymentType();

    Azure getAzureClient() throws IOException;

    IProject getProject();
}
