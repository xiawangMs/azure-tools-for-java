/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner.webapp.webappconfig;

import com.intellij.openapi.project.Project;
import com.microsoft.intellij.runner.AzureSettingPanel;
import com.microsoft.intellij.runner.AzureSettingsEditor;
import com.microsoft.intellij.runner.webapp.webappconfig.slimui.WebAppSlimSettingPanel;
import com.microsoft.intellij.runner.webapp.webappconfig.ui.WebAppSettingPanel;
import org.jetbrains.annotations.NotNull;

public class WebAppSettingEditor extends AzureSettingsEditor<WebAppConfiguration> {

    private final AzureSettingPanel mainPanel;
    private final WebAppConfiguration webAppConfiguration;

    public WebAppSettingEditor(Project project, @NotNull WebAppConfiguration webAppConfiguration) {
        super(project);
        if (webAppConfiguration.getUiVersion() == IntelliJWebAppSettingModel.UIVersion.NEW) {
            mainPanel = new WebAppSlimSettingPanel(project, webAppConfiguration);
        } else {
            mainPanel = new WebAppSettingPanel(project, webAppConfiguration);
        }
        this.webAppConfiguration = webAppConfiguration;
    }

    @Override
    @NotNull
    protected AzureSettingPanel getPanel() {
        return this.mainPanel;
    }
}
