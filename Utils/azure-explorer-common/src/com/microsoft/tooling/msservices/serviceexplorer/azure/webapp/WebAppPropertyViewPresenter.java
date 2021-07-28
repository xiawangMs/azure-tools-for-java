/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.webapp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.microsoft.azure.management.appservice.DeploymentSlot;
import com.microsoft.azure.management.appservice.WebAppBase;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.azuretools.core.mvp.model.webapp.AzureWebAppMvpModel;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.base.WebAppBasePropertyViewPresenter;

public class WebAppPropertyViewPresenter extends WebAppBasePropertyViewPresenter {
    @Override
    protected void updateAppSettings(@NotNull final String sid, @NotNull final String webAppId,
                                     @Nullable final String name, final Map toUpdate,
                                     final Set toRemove) throws Exception {
        AzureWebAppMvpModel.getInstance().updateWebAppSettings(sid, webAppId, toUpdate, toRemove);
    }

    @Override
    protected boolean getPublishingProfile(@NotNull final String sid, @NotNull final String webAppId,
                                           @Nullable final String name,
                                           @NotNull final String filePath) throws Exception {
        return AzureWebAppMvpModel.getInstance().getPublishingProfileXmlWithSecrets(sid, webAppId, filePath);
    }

    @Override
    protected WebAppBase getWebAppBase(@NotNull final String sid, @NotNull final String webAppId,
                                       @Nullable final String name) throws Exception {
        return AzureWebAppMvpModel.getInstance().getWebAppById(sid, webAppId);
    }
}
