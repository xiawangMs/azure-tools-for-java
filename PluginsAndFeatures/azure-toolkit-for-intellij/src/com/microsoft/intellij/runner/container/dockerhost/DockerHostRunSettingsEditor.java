/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner.container.dockerhost;

import com.intellij.openapi.project.Project;
import com.microsoft.intellij.runner.AzureSettingPanel;
import com.microsoft.intellij.runner.AzureSettingsEditor;
import com.microsoft.intellij.runner.container.dockerhost.ui.SettingPanel;

public class DockerHostRunSettingsEditor extends AzureSettingsEditor<DockerHostRunConfiguration> {
    private SettingPanel settingPanel;

    public DockerHostRunSettingsEditor(Project project) {
        super(project);
        this.settingPanel = new SettingPanel(project);
    }

   @Override
   protected AzureSettingPanel getPanel() {
        return this.settingPanel;
   }
}
