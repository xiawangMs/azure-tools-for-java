/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.monitor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.microsoft.azure.toolkit.intellij.monitor.view.AzureMonitorView;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class AzureMonitorManager {
    private static final String AZURE_MONITOR_WINDOW = "Azure Monitor";

    private static final AzureMonitorManager instance = new AzureMonitorManager();
    public static AzureMonitorManager getInstance() {
        return instance;
    }

    public void openMonitorWindow(@Nonnull Project project) {
        final ToolWindow azureMonitorWindow = ToolWindowManager.getInstance(project).getToolWindow(AZURE_MONITOR_WINDOW);
        AzureTaskManager.getInstance().runLater(() -> {
            assert azureMonitorWindow != null;
            azureMonitorWindow.activate(() -> {
                azureMonitorWindow.setAvailable(true);
                azureMonitorWindow.show();
            });
        });
    }

    public static class AzureMonitorFactory implements ToolWindowFactory {
        private static final Map<Project, AzureMonitorView> viewMap = new HashMap<>();

        @Override
        public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
            AzureMonitorView monitorView = getMonitorView(project);
            if (Objects.isNull(monitorView)) {
                monitorView = new AzureMonitorView(project);
                viewMap.put(project, monitorView);
            }
            final ContentFactory contentFactory = ContentFactory.getInstance();
            final Content content = contentFactory.createContent(monitorView.getCenterPanel(), "", false);
            toolWindow.getContentManager().addContent(content);
        }

        @Override
        public boolean shouldBeAvailable(@NotNull Project project) {
            return false;
        }

        public static AzureMonitorView getMonitorView(@Nonnull final Project project) {
            return viewMap.get(project);
        }
    }

}
