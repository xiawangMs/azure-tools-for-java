/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner.functions.component.table;

import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azuretools.core.mvp.model.function.AzureFunctionMvpModel;
import com.microsoft.azuretools.core.mvp.ui.base.MvpPresenter;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import rx.Observable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AppSettingsDialogPresenter<V extends ImportAppSettingsView> extends MvpPresenter<V> {
    public void onLoadFunctionApps() {
        Observable.fromCallable(() -> AzureFunctionMvpModel.getInstance().listAllFunctions(false))
                .subscribeOn(getSchedulerProvider().io())
                .subscribe(functionApps -> DefaultLoader.getIdeHelper().invokeLater(() -> {
                    if (isViewDetached()) {
                        return;
                    }
                    getMvpView().fillFunctionApps(functionApps);
                }), this::errorHandler);
    }

    public void onLoadFunctionAppSettings(String subscriptionId, String functionId) {
        Observable.fromCallable(() -> {
            getMvpView().beforeFillAppSettings();
            final FunctionApp functionApp = AzureFunctionMvpModel.getInstance().getFunctionById(subscriptionId, functionId);
            return functionApp.getAppSettings();
        }).subscribeOn(getSchedulerProvider().io())
                .subscribe(appSettings -> DefaultLoader.getIdeHelper().invokeLater(() -> {
                    if (isViewDetached()) {
                        return;
                    }
                    final Map<String, String> result = new HashMap<>();
                    appSettings.entrySet().forEach(entry -> result.put(entry.getKey(), entry.getValue().value()));
                    getMvpView().fillFunctionAppSettings(result);
                }), this::errorHandler);
    }

    public void onLoadLocalSettings(Path localSettingsJsonPath) {
        Observable.fromCallable(() -> {
            getMvpView().beforeFillAppSettings();
            return AppSettingsTableUtils.getAppSettingsFromLocalSettingsJson(localSettingsJsonPath.toFile());
        }).subscribeOn(getSchedulerProvider().io())
                .subscribe(appSettings -> DefaultLoader.getIdeHelper().invokeLater(() -> {
                    if (isViewDetached()) {
                        return;
                    }
                    getMvpView().fillFunctionAppSettings(appSettings);
                }), this::errorHandler);
    }

    private void errorHandler(Throwable e) {
        DefaultLoader.getIdeHelper().invokeLater(() -> getMvpView().onErrorWithException("Failed to load app settings", (Exception) e));
    }
}
