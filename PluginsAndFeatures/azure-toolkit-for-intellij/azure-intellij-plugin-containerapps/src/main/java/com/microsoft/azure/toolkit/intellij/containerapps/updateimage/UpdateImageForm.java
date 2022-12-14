/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerapps.updateimage;

import com.intellij.icons.AllIcons;
import com.intellij.uiDesigner.core.GridConstraints;
import com.microsoft.azure.toolkit.intellij.common.AzureCommentLabel;
import com.microsoft.azure.toolkit.intellij.common.AzureFormJPanel;
import com.microsoft.azure.toolkit.intellij.containerapps.component.ACRImageForm;
import com.microsoft.azure.toolkit.intellij.containerapps.component.ContainerAppComboBox;
import com.microsoft.azure.toolkit.intellij.containerapps.component.ContainerRegistryTypeComboBox;
import com.microsoft.azure.toolkit.intellij.containerapps.component.OtherPublicRegistryImageForm;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerApp;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerAppDraft;
import com.microsoft.azure.toolkit.lib.containerapps.model.RevisionMode;
import com.microsoft.azure.toolkit.lib.containerregistry.ContainerRegistry;
import com.microsoft.azure.toolkit.lib.containerregistry.model.IContainerRegistry;
import lombok.Data;
import lombok.Getter;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UpdateImageForm extends JPanel implements AzureFormJPanel<UpdateImageForm.UpdateImageConfig> {
    @Getter
    private JPanel contentPanel;
    private ContainerRegistryTypeComboBox selectorRegistryType;
    private ContainerAppComboBox selectorApp;
    private JPanel formImageContainer;
    private AzureCommentLabel commentApp;
    private AzureFormJPanel<ContainerAppDraft.ImageConfig> formImage;

    public UpdateImageForm() {
        super();
        $$$setupUI$$$(); // tell IntelliJ to call createUIComponents() here.
        this.init();
    }

    private void init() {
        this.selectorRegistryType.addItemListener(this::onRegistryTypeChanged);
        this.selectorApp.addItemListener(this::onAppChanged);
        this.selectorRegistryType.setRequired(true);
        this.selectorApp.setRequired(true);
        this.commentApp.setVisible(false);
        this.commentApp.setIconWithAlignment(AllIcons.General.Warning, SwingConstants.LEFT, SwingConstants.CENTER);
    }

    @Override
    public UpdateImageConfig getValue() {
        final UpdateImageConfig config = new UpdateImageConfig();
        config.setApp(this.selectorApp.getValue());
        config.setImage(this.formImage.getValue());
        return config;
    }

    @Override
    public void setValue(final UpdateImageConfig config) {
        final ContainerAppDraft.ImageConfig imageConfig = config.getImage();
        this.setImage(imageConfig);
        final ContainerApp app = config.getApp();
        this.setApp(app);
    }

    public void setApp(ContainerApp app) {
        this.selectorApp.setValue(app);
    }

    public void setImage(ContainerAppDraft.ImageConfig imageConfig) {
        final IContainerRegistry registry = imageConfig.getContainerRegistry();
        final String type = registry instanceof ContainerRegistry ? ContainerRegistryTypeComboBox.ACR : ContainerRegistryTypeComboBox.OTHER;
        this.formImage = this.updateImagePanel(type);
        this.selectorRegistryType.setValue(type);
        this.formImage.setValue(imageConfig);
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        final AzureFormInput<?>[] inputs = {
            this.selectorRegistryType,
            this.selectorApp
        };
        return Arrays.asList(inputs);
    }

    @Override
    public void setVisible(final boolean visible) {
        this.contentPanel.setVisible(visible);
        super.setVisible(visible);
    }

    private void onRegistryTypeChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            this.updateImagePanel((String) e.getItem());
        }
    }

    private void onAppChanged(final ItemEvent e) {
        this.commentApp.setVisible(false);
        if (e.getStateChange() == ItemEvent.SELECTED) {
            final ContainerApp app = (ContainerApp) e.getItem();
            if (app.hasUnsupportedFeatures()) {
                final RevisionMode revisionsMode = app.revisionModel();
                final String message = revisionsMode == RevisionMode.SINGLE ?
                    "This will overwrite the active revision and unsupported features in IntelliJ will be lost." :
                    "Unsupported features in IntelliJ will be lost in the new revision.";
                this.commentApp.setText(message);
                this.commentApp.setVisible(true);
            }
        }
    }

    private AzureFormJPanel<ContainerAppDraft.ImageConfig> updateImagePanel(String type) {
        final GridConstraints constraints = new GridConstraints();
        constraints.setFill(GridConstraints.FILL_BOTH);
        constraints.setHSizePolicy(GridConstraints.SIZEPOLICY_WANT_GROW);
        constraints.setUseParentLayout(true);
        final AzureFormJPanel<ContainerAppDraft.ImageConfig> newFormImage = Objects.equals(type, ContainerRegistryTypeComboBox.ACR) ?
            new ACRImageForm() : new OtherPublicRegistryImageForm();
        this.formImageContainer.removeAll();
        this.formImageContainer.add(newFormImage.getContentPanel(), constraints);
        return newFormImage;
    }

    private void createUIComponents() {
    }

    @Data
    public static class UpdateImageConfig {
        private ContainerApp app;
        private ContainerAppDraft.ImageConfig image;
    }
}
