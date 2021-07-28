/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.appservice;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.HashMap;
import java.util.Map;

public class CLIConsoleViewManager {
    public static final String LOG_TOOL_WINDOW = "CLI Output";

    private Map<Project, ToolWindow> toolWindowMap = new HashMap<>();
    private Map<ToolWindow, ConsoleView> consoleViewMap = new HashMap<>();
    private BidiMap<String, String> resourceIdToNameMap = new DualHashBidiMap<>();

    public static CLIConsoleViewManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void showStreamingLogConsole(Project project, String resourceId, String resourceName,
                                        ConsoleView consoleView) {
        ApplicationManager.getApplication().invokeLater(() -> {
            final ToolWindow toolWindow = getToolWindow(project);
            toolWindow.show(null);
            final String consoleName = getConsoleViewName(resourceId, resourceName);
            Content content = toolWindow.getContentManager().findContent(consoleName);
            if (content == null) {
                    content = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), consoleName, false);
                    content.setDisposer(consoleView);
                    if(toolWindow.getContentManager().findContent(consoleName) == null) {
                        toolWindow.getContentManager().addContent(content);
                    }

            }
            toolWindow.getContentManager().setSelectedContent(content);
        });
    }

    public void removeConsoleView(String resourceId) {
        resourceIdToNameMap.remove(resourceId);
    }

    private String getConsoleViewName(String resourceId, String resourceName) {
        if (resourceIdToNameMap.containsKey(resourceId)) {
            return resourceIdToNameMap.get(resourceId);
        }
        String result = resourceName;
        int i = 1;
        while (resourceIdToNameMap.containsValue(result)) {
            result = String.format("%s(%s)", resourceName, i++);
        }
        resourceIdToNameMap.put(resourceId, result);
        return result;
    }

    public ConsoleView getConsoleView(Project project) {
        ToolWindow toolWindow = getToolWindow(project);
        if(toolWindow != null) {
            if (consoleViewMap.containsKey(toolWindow)) {
                return consoleViewMap.get(toolWindow);
            }
            final ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            consoleViewMap.put(toolWindow, consoleView);
            return consoleView;
        }
        return null;
    }

    private ToolWindow getToolWindow(Project project) {
        if (toolWindowMap.containsKey(project)) {
            return toolWindowMap.get(project);
        }
        // Add content manager listener when get tool window at the first time
        final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(LOG_TOOL_WINDOW);
        if(toolWindow != null) {
            toolWindow.getContentManager().addContentManagerListener(new ContentManagerAdapter() {
                @Override
                public void contentRemoved(ContentManagerEvent contentManagerEvent) {
                    final String displayName = contentManagerEvent.getContent().getDisplayName();
                    resourceIdToNameMap.removeValue(displayName);
                }
            });
            toolWindowMap.put(project, toolWindow);
        }
        return toolWindow;
    }

    private static final class SingletonHolder {
        private static final CLIConsoleViewManager INSTANCE = new CLIConsoleViewManager();

        private SingletonHolder() {
        }
    }
}
