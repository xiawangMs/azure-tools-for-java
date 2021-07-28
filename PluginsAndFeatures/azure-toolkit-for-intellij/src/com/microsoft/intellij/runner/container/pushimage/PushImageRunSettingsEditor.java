/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner.container.pushimage;

import com.intellij.openapi.project.Project;
import com.microsoft.intellij.runner.AzureSettingPanel;
import com.microsoft.intellij.runner.AzureSettingsEditor;
import com.microsoft.intellij.runner.container.pushimage.ui.SettingPanel;

import org.jetbrains.annotations.NotNull;

public class PushImageRunSettingsEditor extends AzureSettingsEditor<PushImageRunConfiguration> {
    private final SettingPanel settingPanel;

    public PushImageRunSettingsEditor(Project project) {
        super(project);
        this.settingPanel = new SettingPanel(project);
    }

    @Override
    @NotNull
    protected AzureSettingPanel getPanel() {
        return this.settingPanel;
    }
}
