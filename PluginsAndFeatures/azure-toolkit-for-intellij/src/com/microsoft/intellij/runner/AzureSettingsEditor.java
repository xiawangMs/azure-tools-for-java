/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.impl.run.BuildArtifactsBeforeRunTaskProvider;
import com.microsoft.intellij.runner.functions.core.FunctionUtils;
import com.microsoft.intellij.util.MavenRunTaskUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import java.util.List;

public abstract class AzureSettingsEditor<T extends AzureRunConfigurationBase> extends SettingsEditor<T> {
    private final Project project;

    public AzureSettingsEditor(@NotNull Project project) {
        this.project = project;
    }

    @Override
    protected void applyEditorTo(@NotNull T conf) throws ConfigurationException {
        this.getPanel().apply(conf);
        conf.validate();
    }

    @Override
    protected void resetEditorFrom(@NotNull T conf) {
        if (conf.isFirstTimeCreated()) {
            if (FunctionUtils.isFunctionProject(conf.getProject())) {
                // Todo: Add before run build job
            } else if (MavenRunTaskUtil.isMavenProject(project)) {
                MavenRunTaskUtil.addMavenPackageBeforeRunTask(conf);
            } else {
                final List<Artifact> artifacts = MavenRunTaskUtil.collectProjectArtifact(project);
                if (artifacts.size() > 0) {
                    BuildArtifactsBeforeRunTaskProvider.setBuildArtifactBeforeRun(project, conf, artifacts.get(0));
                }
            }
        }
        conf.setFirstTimeCreated(false);
        this.getPanel().reset(conf);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return getPanel().getMainPanel();
    }

    @Override
    protected void disposeEditor() {
        getPanel().disposeEditor();
        super.disposeEditor();
    }

    @NotNull
    protected abstract AzureSettingPanel getPanel();
}
