/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.legacy.appservice;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jetbrains.annotations.NotNull;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.intellij.execution.ui.ConsoleViewContentType.NORMAL_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT;
import static com.microsoft.azure.toolkit.intellij.common.AzureBundle.message;

public class AppServiceStreamingLogConsoleView extends ConsoleViewImpl {

    private static final String SEPARATOR = System.getProperty("line.separator");

    private boolean isDisposed;
    private Disposable subscription;

    private Flux<String> logStreaming;

    public AppServiceStreamingLogConsoleView(@NotNull Project project, String resourceId) {
        super(project, true);
        this.isDisposed = false;
        AzureTaskManager.getInstance().runLater(this::initUI);
    }

    public void setLogStreaming(Flux<String> logStreaming) {
        this.logStreaming = logStreaming;
    }

    public void startStreamingLog() {
        if (!isActive()) {
            printlnToConsole(message("appService.logStreaming.hint.connect"), SYSTEM_OUTPUT);
            subscription = this.logStreaming.subscribeOn(Schedulers.boundedElastic())
                                       .doAfterTerminate(() -> printlnToConsole(message("appService.logStreaming.hint.disconnected"), SYSTEM_OUTPUT))
                                       .subscribe((log) -> printlnToConsole(log, NORMAL_OUTPUT));
        }
    }

    public void closeStreamingLog() {
        if (isActive()) {
            subscription.dispose();
            printlnToConsole(message("appService.logStreaming.hint.disconnected"), SYSTEM_OUTPUT);
        }
    }

    public boolean isActive() {
        return subscription != null && !subscription.isDisposed();
    }

    public boolean isDisposed() {
        return this.isDisposed;
    }

    private void printlnToConsole(String message, ConsoleViewContentType consoleViewContentType) {
        this.print(message + SEPARATOR, consoleViewContentType);
    }

    private void initUI() {
        this.getComponent();
        final AzureComboBox<LogFilterData> comboBox = this.initComboBox();
        try {
            final Field field2 = ConsoleViewImpl.class.getDeclaredField("myJLayeredPane");
            field2.setAccessible(true);
            final JPanel value = (JPanel) MethodUtils.invokeMethod(field2.get(this), true, "getContent");
            value.add(BorderLayout.NORTH, this.initComboBox());
        } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            System.out.println("wy " + e.toString());
        }
    }

    private AzureComboBox<LogFilterData> initComboBox() {
        final List<LogFilterData> filterList = new ArrayList<>();
        filterList.add(new LogFilterData("Last 6 hours", 6));
        filterList.add(new LogFilterData("Last 24 hours", 24));
        filterList.add(new LogFilterData("Last 2 days", 48));
        final Supplier<List<LogFilterData>> loader = () -> filterList;
        final AzureComboBox<LogFilterData> comboBox = new AzureComboBox<>(loader) {
            @Override
            protected String getItemText(Object item) {
                return ((LogFilterData) item).getText();
            }

            @Override
            protected void refreshItems() {
                super.refreshItems();
            }
        };
        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                final int timeRange = ((LogFilterData) e.getItem()).getTimeRange();
                System.out.println("wy time range: " + timeRange);
            }
        });
        return comboBox;
    }

    @Override
    public void dispose() {
        super.dispose();
        this.isDisposed = true;
        closeStreamingLog();
    }
}

class LogFilterData {
    private final String text;
    private final int timeRange;
    public LogFilterData(String text, int timeRange) {
        this.text = text;
        this.timeRange = timeRange;
    }

    public String getText() {
        return text;
    }

    public int getTimeRange() {
        return timeRange;
    }
}
