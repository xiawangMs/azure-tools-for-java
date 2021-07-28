/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner.webapp.webappconfig.slimui;

import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azuretools.core.mvp.model.ResourceEx;
import com.microsoft.azuretools.core.mvp.model.webapp.AzureWebAppMvpModel;
import com.microsoft.azuretools.core.mvp.ui.base.MvpPresenter;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import org.apache.commons.lang3.exception.ExceptionUtils;
import rx.Observable;
import rx.Subscription;

import java.io.InterruptedIOException;
import java.util.List;

import static com.microsoft.intellij.util.RxJavaUtils.unsubscribeSubscription;

public class WebAppDeployViewPresenterSlim<V extends WebAppDeployMvpViewSlim> extends MvpPresenter<V> {

    private static final String CANNOT_LIST_WEB_APP = "Failed to list web apps.";
    private static final String CANNOT_GET_DEPLOYMENT_SLOTS = "Failed to get the deployment slots.";

    private Subscription loadSlotsSubscription;
    private Subscription loadWebAppsSubscription;

    public void onLoadDeploymentSlots(final ResourceEx<WebApp> selectedWebApp) {
        if (selectedWebApp == null) {
            return;
        }
        unsubscribeSubscription(loadSlotsSubscription);
        loadSlotsSubscription = Observable.fromCallable(() -> AzureWebAppMvpModel.getInstance().getDeploymentSlots(
                selectedWebApp.getSubscriptionId(), selectedWebApp.getResource().id()))
                  .subscribeOn(getSchedulerProvider().io())
                  .subscribe(slots -> DefaultLoader.getIdeHelper().invokeLater(() -> {
                      if (isViewDetached()) {
                          return;
                      }
                      getMvpView().fillDeploymentSlots(slots, selectedWebApp);
                  }), e -> errorHandler(CANNOT_GET_DEPLOYMENT_SLOTS, (Exception) e));
    }

    public void loadWebApps(boolean forceRefresh, String defaultWebAppId) {
        unsubscribeSubscription(loadWebAppsSubscription);
        loadWebAppsSubscription = Observable.fromCallable(() -> {
                List<ResourceEx<WebApp>> result = AzureWebAppMvpModel.getInstance().listAllWebApps(forceRefresh);
                return result;
            }
        )
            .subscribeOn(getSchedulerProvider().io())
            .subscribe(webAppList -> DefaultLoader.getIdeHelper().invokeLater(() -> {
                if (isViewDetached()) {
                    return;
                }
                getMvpView().fillWebApps(webAppList, defaultWebAppId);
            }), e -> errorHandler(CANNOT_LIST_WEB_APP, (Exception) e));
    }

    private void errorHandler(String msg, Exception e) {
        final Throwable rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause instanceof InterruptedIOException || rootCause instanceof InterruptedException) {
            // Swallow interrupted exception caused by unsubscribe
            return;
        }
        DefaultLoader.getIdeHelper().invokeLater(() -> {
            if (isViewDetached()) {
                return;
            }
            getMvpView().onErrorWithException(msg, e);
        });
    }
}
