/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.intellij.helpers;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static com.intellij.execution.ui.ConsoleViewContentType.NORMAL_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT;

public class AppServiceStreamingLogConsoleView extends ConsoleViewImpl {

    private static final String SEPARATOR = System.getProperty("line.separator");
    private static final String START_LOG_STREAMING = "Connecting to log stream...";
    private static final String STOP_LOG_STREAMING = "Disconnected from log-streaming service.";

    private boolean isDisposed;
    private String resourceId;
    private Subscription subscription;

    public AppServiceStreamingLogConsoleView(@NotNull Project project, String resourceId) {
        super(project, true);
        this.isDisposed = false;
        this.resourceId = resourceId;
    }

    public void startStreamingLog(Observable<String> logStreaming) {
        if (!isActive()) {
            printlnToConsole(START_LOG_STREAMING, SYSTEM_OUTPUT);
            subscription = logStreaming.subscribeOn(Schedulers.io())
                                       .doAfterTerminate(() -> printlnToConsole(STOP_LOG_STREAMING, SYSTEM_OUTPUT))
                                       .subscribe((log) -> printlnToConsole(log, NORMAL_OUTPUT));
        }
    }

    public void closeStreamingLog() {
        if (isActive()) {
            subscription.unsubscribe();
            printlnToConsole(STOP_LOG_STREAMING, SYSTEM_OUTPUT);
        }
    }

    public boolean isActive() {
        return subscription != null && !subscription.isUnsubscribed();
    }

    public boolean isDisposed() {
        return this.isDisposed;
    }

    private void printlnToConsole(String message, ConsoleViewContentType consoleViewContentType) {
        this.print(message + SEPARATOR, consoleViewContentType);
    }

    @Override
    public void dispose() {
        super.dispose();
        this.isDisposed = true;
        closeStreamingLog();
    }
}
