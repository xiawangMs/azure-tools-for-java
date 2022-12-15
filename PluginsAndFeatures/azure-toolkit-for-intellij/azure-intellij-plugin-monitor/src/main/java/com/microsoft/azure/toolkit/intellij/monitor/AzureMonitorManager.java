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
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspace;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;


public class AzureMonitorManager {
    private static final String AZURE_MONITOR_WINDOW = "Azure Monitor";
    private static final Map<Project, ToolWindow> toolWindowMap = new HashMap<>();
    private static final AzureMonitorManager instance = new AzureMonitorManager();
    public static AzureMonitorManager getInstance() {
        return instance;
    }

    public void openMonitorWindow(@Nonnull Project project, @Nonnull Subscription subscription, @Nullable LogAnalyticsWorkspace workspace) {
        final ToolWindow azureMonitorWindow = getToolWindow(project);
        final ContentFactory contentFactory = ContentFactory.getInstance();
        final AzureMonitorView monitorView = new AzureMonitorView(project, subscription, workspace);
        final Content content = contentFactory.createContent(monitorView.getCenterPanel(), "", false);
        azureMonitorWindow.getContentManager().addContent(content);
        AzureTaskManager.getInstance().runLater(() -> azureMonitorWindow.activate(() -> {
            azureMonitorWindow.setAvailable(true);
            azureMonitorWindow.show();
//                monitorView.initListener();
        }));
    }

    private ToolWindow getToolWindow(Project project) {
        if (toolWindowMap.containsKey(project)) {
            return toolWindowMap.get(project);
        }
        final ToolWindow azureMonitorWindow = ToolWindowManager.getInstance(project).getToolWindow(AZURE_MONITOR_WINDOW);
        toolWindowMap.put(project, azureMonitorWindow);
        return azureMonitorWindow;
    }

    public static class AzureMonitorFactory implements ToolWindowFactory {
        @Override
        public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        }

        @Override
        public boolean shouldBeAvailable(@NotNull Project project) {
            return false;
        }
    }

}
