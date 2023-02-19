/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.eventhubs.view;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.eventhubs.AzureEventHubsNamespace;
import com.microsoft.azure.toolkit.lib.eventhubs.EventHubsInstance;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.Optional;

public class EventHubsToolWindowManager {
    private static final String EVENT_HUBS_TOOL_WINDOW = "Azure Event Hubs";
    private static final EventHubsToolWindowManager instance = new EventHubsToolWindowManager();
    private BidiMap<String, String> resourceIdToNameMap = new DualHashBidiMap<>();

    public static  EventHubsToolWindowManager getInstance() {
        return instance;
    }

    public void showEventHubsConsole(Project project, String resourceId, String resourceName, ConsoleView consoleView) {
        final ToolWindow toolWindow = getToolWindow(project);
        final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        final String consoleName = getConsoleViewName(resourceId, resourceName);
        Content content = toolWindow.getContentManager().findContent(consoleName);
        if (content == null) {
            content = contentFactory.createContent(consoleView.getComponent(), consoleName, false);
            content.setDisposer(consoleView);
            toolWindow.getContentManager().addContent(content);
        }
        toolWindow.getContentManager().setSelectedContent(content);
        toolWindow.setAvailable(true);
        toolWindow.activate(null);
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

    private ToolWindow getToolWindow(Project project) {
        final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(EVENT_HUBS_TOOL_WINDOW);
        Optional.ofNullable(toolWindow).ifPresent(w -> w.getContentManager().addContentManagerListener(
                new ContentManagerAdapter() {
                    @Override
                    public void contentRemoved(ContentManagerEvent contentManagerEvent) {
                        final String displayName = contentManagerEvent.getContent().getDisplayName();
                        final String removeResourceId = resourceIdToNameMap.getKey(displayName);
                        final EventHubsInstance instance = Azure.az(AzureEventHubsNamespace.class).getById(removeResourceId);
                        Optional.ofNullable(instance).ifPresent(EventHubsInstance::stopListening);
                        resourceIdToNameMap.removeValue(displayName);
                    }
                }));
        return toolWindow;
    }
}
