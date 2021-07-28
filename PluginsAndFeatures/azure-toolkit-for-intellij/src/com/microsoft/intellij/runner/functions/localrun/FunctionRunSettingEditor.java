/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner.functions.localrun;

import com.intellij.openapi.project.Project;
import com.microsoft.intellij.runner.AzureSettingPanel;
import com.microsoft.intellij.runner.AzureSettingsEditor;
import com.microsoft.intellij.runner.functions.localrun.ui.FunctionRunPanel;
import org.jetbrains.annotations.NotNull;

public class FunctionRunSettingEditor extends AzureSettingsEditor<FunctionRunConfiguration> {

    private AzureSettingPanel panel;
    private FunctionRunConfiguration functionRunConfiguration;

    public FunctionRunSettingEditor(@NotNull Project project, FunctionRunConfiguration functionRunConfiguration) {
        super(project);
        this.functionRunConfiguration = functionRunConfiguration;
        panel = new FunctionRunPanel(project, functionRunConfiguration);
    }

    @NotNull
    @Override
    protected AzureSettingPanel getPanel() {
        return panel;
    }
}
