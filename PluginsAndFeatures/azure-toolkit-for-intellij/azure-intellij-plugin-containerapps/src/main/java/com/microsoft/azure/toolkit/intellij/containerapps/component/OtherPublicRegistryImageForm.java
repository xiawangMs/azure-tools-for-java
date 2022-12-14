/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerapps.component;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.AzureFormJPanel;
import com.microsoft.azure.toolkit.intellij.common.AzureFormPanel;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerAppDraft;
import lombok.Getter;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public class OtherPublicRegistryImageForm implements AzureFormJPanel<ContainerAppDraft.ImageConfig> {
    private final Project project;
    @Getter
    private JPanel contentPanel;
    private com.microsoft.azure.toolkit.intellij.common.AzureTextInput txtImage;

    public OtherPublicRegistryImageForm(Project project) {
        super();
        this.project = project;
        $$$setupUI$$$(); // tell IntelliJ to call createUIComponents() here.
        this.init();
    }

    private void init() {
    }

    @Override
    public ContainerAppDraft.ImageConfig getValue() {
        final String fullImageName = this.txtImage.getValue();
        final ContainerAppDraft.ImageConfig config = new ContainerAppDraft.ImageConfig();
        config.setFullImageName(fullImageName);
        return config;
    }

    @Override
    public void setValue(final ContainerAppDraft.ImageConfig config) {
        this.txtImage.setValue(config.getFullImageName());
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Collections.singletonList(this.txtImage);
    }

    @Override
    public void setVisible(final boolean visible) {
        this.contentPanel.setVisible(visible);
    }
}
