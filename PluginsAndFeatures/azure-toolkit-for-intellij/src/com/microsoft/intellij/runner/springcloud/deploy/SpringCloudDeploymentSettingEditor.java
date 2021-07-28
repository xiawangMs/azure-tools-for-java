/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner.springcloud.deploy;

import com.intellij.openapi.project.Project;
import com.microsoft.intellij.runner.AzureSettingPanel;
import com.microsoft.intellij.runner.AzureSettingsEditor;
import com.microsoft.intellij.runner.springcloud.ui.SpringCloudAppSettingPanel;
import com.microsoft.intellij.util.MavenRunTaskUtil;
import org.jetbrains.annotations.NotNull;

public class SpringCloudDeploymentSettingEditor extends AzureSettingsEditor<SpringCloudDeployConfiguration> {
    private final Project project;
    private final AzureSettingPanel mainPanel;

    public SpringCloudDeploymentSettingEditor(Project project, @NotNull SpringCloudDeployConfiguration springCloudDeployConfiguration) {
        super(project);
        this.project = project;
        this.mainPanel = new SpringCloudAppSettingPanel(project, springCloudDeployConfiguration);
    }

    protected void disposeEditor() {
        this.mainPanel.disposeEditor();
    }

    @Override
    @NotNull
    protected AzureSettingPanel getPanel() {
        return this.mainPanel;
    }

    @Override
    protected void resetEditorFrom(@NotNull SpringCloudDeployConfiguration conf) {
        if (conf.isFirstTimeCreated()) {
            if (MavenRunTaskUtil.isMavenProject(project)) {
                MavenRunTaskUtil.addMavenPackageBeforeRunTask(conf);
            }
        }
        conf.setFirstTimeCreated(false);
        this.getPanel().reset(conf);
    }
}
