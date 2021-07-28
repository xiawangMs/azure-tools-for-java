/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner.functions.deploy.ui;

import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azuretools.core.mvp.model.function.AzureFunctionMvpModel;
import com.microsoft.azuretools.core.mvp.ui.base.MvpPresenter;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FunctionDeployViewPresenter<V extends FunctionDeployMvpView> extends MvpPresenter<V> {

    private static final String CANNOT_LIST_WEB_APP = "Failed to list function apps.";
    private static final String CANNOT_SHOW_APP_SETTINGS = "Failed to show app settings";

    public void loadFunctionApps(boolean forceRefresh, boolean fillAppSettings) {
        Observable.fromCallable(() -> {
            DefaultLoader.getIdeHelper().invokeAndWait(() -> getMvpView().beforeFillFunctionApps());
            return AzureFunctionMvpModel.getInstance().listAllFunctions(forceRefresh)
                    .stream()
                    .sorted((a, b) -> a.getResource().name().compareToIgnoreCase(b.getResource().name()))
                    .collect(Collectors.toList());
        }).subscribeOn(getSchedulerProvider().io())
                .subscribe(functionApps -> DefaultLoader.getIdeHelper().invokeLater(() -> {
                    if (!isViewDetached()) {
                        getMvpView().fillFunctionApps(functionApps, fillAppSettings);
                    }
                }), e -> errorHandler(CANNOT_LIST_WEB_APP, (Exception) e));
    }

    public void loadAppSettings(FunctionApp functionApp) {
        Observable.fromCallable(() -> {
            DefaultLoader.getIdeHelper().invokeAndWait(() -> getMvpView().beforeFillAppSettings());
            return functionApp.getAppSettings();
        }).subscribeOn(getSchedulerProvider().io())
                .subscribe(appSettings -> DefaultLoader.getIdeHelper().invokeLater(() -> {
                    if (isViewDetached()) {
                        return;
                    }
                    final Map<String, String> result = new HashMap<>();
                    appSettings.entrySet().forEach(entry -> result.put(entry.getKey(), entry.getValue().value()));
                    getMvpView().fillAppSettings(result);
                }), e -> errorHandler(CANNOT_SHOW_APP_SETTINGS, (Exception) e));
    }

    private void errorHandler(String msg, Exception e) {
        DefaultLoader.getIdeHelper().invokeLater(() -> {
            if (isViewDetached()) {
                return;
            }
            getMvpView().onErrorWithException(msg, e);
        });
    }
}
