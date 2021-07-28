/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.microsoft.azuretools.ijidea.utility.AzureAnAction;
import com.microsoft.azuretools.telemetrywrapper.Operation;
import com.microsoft.intellij.helpers.WhatsNewManager;
import com.microsoft.intellij.util.PluginUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ShowWhatsNewAction extends AzureAnAction {

    private static final String FAILED_TO_LOAD_WHATS_NEW = "Failed to load what's new document";

    @Override
    public boolean onActionPerformed(@NotNull final AnActionEvent anActionEvent, @Nullable final Operation operation) {
        final Project project = anActionEvent.getProject();
        try {
            WhatsNewManager.INSTANCE.showWhatsNew(true, project);
        } catch (IOException e) {
            PluginUtil.showInfoNotificationProject(project, FAILED_TO_LOAD_WHATS_NEW, e.getMessage());
        }
        return true;
    }
}
