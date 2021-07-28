/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner.functions.component;

import com.microsoft.tooling.msservices.components.DefaultLoader;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.io.InterruptedIOException;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class ComponentUtils {

    public static <T> Disposable loadResourcesAsync(Callable<T> callable,
                                                    Consumer<T> resourceHandler,
                                                    Consumer<? super Throwable> errorHandler) {
        return Observable
                .fromCallable(() -> {
                    try {
                        return callable.call();
                    } catch (RuntimeException ex) {
                        if (ex.getCause() instanceof InterruptedIOException) {
                            // swallow InterruptedException caused by Disposable.dispose
                            return null;
                        } else {
                            throw ex;
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(
                    resource -> {
                        DefaultLoader.getIdeHelper().invokeLater(() -> resourceHandler.accept(resource));
                    },
                    exception -> {
                        DefaultLoader.getIdeHelper().invokeLater(() -> {
                            errorHandler.accept(exception);
                        });
                    });
    }

    private ComponentUtils(){

    }
}
