/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.eventhubs.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.microsoft.azure.toolkit.intellij.common.AzureTextInput;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.eventhubs.EventHubsInstance;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class EventHubsMessageDialog extends DialogWrapper {
    private JPanel contentPanel;
    private AzureTextInput inputMessage;
    private final EventHubsInstance eventHubsInstance;

    public EventHubsMessageDialog(@Nullable Project project, EventHubsInstance instance) {
        super(project, false);
        setTitle("Enter Message");
        this.eventHubsInstance = instance;
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPanel;
    }

    @Override
    protected void doOKAction() {
        AzureTaskManager.getInstance().runInBackground("sending message",
                () -> this.eventHubsInstance.publishEvents(inputMessage.getValue()));
        super.doOKAction();
    }
}
