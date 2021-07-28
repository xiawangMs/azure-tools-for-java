/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.springcloud;

import com.microsoft.azure.common.utils.SneakyThrowUtils;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.DeploymentResourceStatus;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.implementation.AppResourceInner;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.implementation.DeploymentResourceInner;
import com.microsoft.azuretools.core.mvp.model.springcloud.AzureSpringCloudMvpModel;
import com.microsoft.azuretools.core.mvp.model.springcloud.SpringCloudIdHelper;
import com.microsoft.azuretools.core.mvp.ui.base.MvpPresenter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpringCloudAppNodePresenter<V extends SpringCloudAppNodeView> extends MvpPresenter<V> {
    private static final Logger LOGGER = Logger.getLogger(SpringCloudAppNodePresenter.class.getName());
    private static final String FAILED_TO_STOP_APP = "Failed to refresh Spring Cloud App: %s";

    public void onStartSpringCloudApp(String appId, String activeDeploymentName, DeploymentResourceStatus originalStatus) throws IOException {
        AzureSpringCloudMvpModel.startApp(appId, activeDeploymentName).await();
        waitUntilStatusChanged(appId, originalStatus);
    }

    public void onStopSpringCloudApp(String appId, String activeDeploymentName, DeploymentResourceStatus originalStatus) throws IOException {
        AzureSpringCloudMvpModel.stopApp(appId, activeDeploymentName).await();
        waitUntilStatusChanged(appId, originalStatus);
    }

    public void onReStartSpringCloudApp(String appId, String activeDeploymentName, DeploymentResourceStatus originalStatus) throws IOException {
        AzureSpringCloudMvpModel.restartApp(appId, activeDeploymentName).await();
        waitUntilStatusChanged(appId, originalStatus);
    }

    public void onDeleteApp(final String appId) throws IOException {
        AzureSpringCloudMvpModel.deleteApp(appId).await();
        waitUntilStatusChanged(appId, null);
    }

    public static void awaitAndMonitoringStatus(String appId, DeploymentResourceStatus originalStatus) throws IOException, InterruptedException {
        String clusterId = getParentSegment(appId);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future future = executor.submit(() -> {
            DeploymentResourceStatus status = null;
            do {
                try {
                    AppResourceInner app = AzureSpringCloudMvpModel.getAppById(appId);
                    if (app == null) {
                        SpringCloudStateManager.INSTANCE.notifySpringAppDelete(clusterId, appId);
                        return;
                    }
                    DeploymentResourceInner deployment = AzureSpringCloudMvpModel.getActiveDeploymentForApp(appId);
                    SpringCloudStateManager.INSTANCE.notifySpringAppUpdate(clusterId, app, deployment);
                    if (deployment == null) {
                        return;
                    }

                    status = deployment.properties().status();
                    SpringCloudStateManager.INSTANCE.notifySpringAppUpdate(clusterId, app, deployment);
                    Thread.sleep(1000 * 5);
                } catch (IOException | InterruptedException e) {
                    SneakyThrowUtils.sneakyThrow(e);
                }

            } while (originalStatus == status);
        });
        try {
            future.get(180, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Failed to get the spring cloud app status for app: %s.", SpringCloudIdHelper
                    .getAppName(appId)), e);
        }
    }

    private void waitUntilStatusChanged(String appId, DeploymentResourceStatus originalStatus) {
        try {
            awaitAndMonitoringStatus(appId, originalStatus);
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, String.format(FAILED_TO_STOP_APP, SpringCloudIdHelper.getAppName(appId)), e);
        }
    }

    private static String getParentSegment(String id) {
        if (StringUtils.isEmpty(id)) {
            return id;
        }
        final String[] attributes = id.split("/");
        return StringUtils.join(ArrayUtils.subarray(attributes, 0, attributes.length - 2), "/");
    }
}
