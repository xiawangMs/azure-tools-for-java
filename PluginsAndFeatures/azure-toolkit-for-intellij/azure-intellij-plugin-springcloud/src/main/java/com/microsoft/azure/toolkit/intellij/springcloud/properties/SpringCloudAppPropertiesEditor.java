/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.springcloud.properties;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBLabel;
import com.microsoft.azure.toolkit.intellij.common.BaseEditor;
import com.microsoft.azure.toolkit.intellij.common.properties.IntellijShowPropertiesViewAction;
import com.microsoft.azure.toolkit.intellij.springcloud.component.SpringCloudAppConfigPanel;
import com.microsoft.azure.toolkit.intellij.springcloud.component.SpringCloudAppInstancesPanel;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudAppDraft;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudDeployment;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudDeploymentDraft;
import com.microsoft.azure.toolkit.lib.springcloud.task.DeploySpringCloudAppTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Objects;

public class SpringCloudAppPropertiesEditor extends BaseEditor {
    private JButton refreshButton;
    private JButton startButton;
    private JButton stopButton;
    private JButton restartButton;
    private JButton deleteButton;
    private JPanel contentPanel;
    private JButton saveButton;
    private ActionLink reset;
    private JBLabel lblSubscription;
    private JBLabel lblCluster;
    private JBLabel lblApp;
    private SpringCloudAppConfigPanel formConfig;
    private SpringCloudAppInstancesPanel panelInstances;

    @Nonnull
    private final Project project;
    @Nonnull
    private final SpringCloudAppDraft value;

    public SpringCloudAppPropertiesEditor(@Nonnull Project project, @Nonnull SpringCloudAppDraft value, @Nonnull final VirtualFile virtualFile) {
        super(virtualFile);
        this.project = project;
        this.value = value;
        this.rerender();
        this.initListeners();
    }

    private void rerender() {
        AzureTaskManager.getInstance().runLater(() -> {
            this.reset.setVisible(false);
            this.saveButton.setEnabled(false);
            this.lblSubscription.setText(this.value.getSubscription().getName());
            this.lblCluster.setText(this.value.getParent().getName());
            this.lblApp.setText(this.value.getName());
            AzureTaskManager.getInstance().runOnPooledThread((() -> {
                final SpringCloudDeployment deployment = this.value.getActiveDeployment();
                AzureTaskManager.getInstance().runLater(() -> this.resetToolbar(deployment));
            }));
            AzureTaskManager.getInstance().runLater(() -> this.formConfig.setValue(this.value));
            this.panelInstances.setApp(this.value);
        });
    }

    private void initListeners() {
        this.reset.addActionListener(e -> this.formConfig.reset());
        this.refreshButton.addActionListener(e -> refresh());
        final String deleteTitle = String.format("Deleting app(%s)", this.value.getName());
        this.deleteButton.addActionListener(e -> {
            final String message = String.format("Are you sure to delete Spring Cloud App(%s)", this.value.getName());
            if (AzureMessager.getMessager().confirm(message, "Delete Spring Cloud App")) {
                AzureTaskManager.getInstance().runInModal(deleteTitle, () -> {
                    this.setEnabled(false);
                    IntellijShowPropertiesViewAction.closePropertiesView(this.value, this.project);
                    this.value.delete();
                });
            }
        });
        final String startTitle = String.format("Starting app(%s)", this.value.getName());
        this.startButton.addActionListener(e -> AzureTaskManager.getInstance().runInBackground(startTitle, () -> {
            this.setEnabled(false);
            this.value.start();
            this.refresh();
        }));
        final String stopTitle = String.format("Stopping app(%s)", this.value.getName());
        this.stopButton.addActionListener(e -> AzureTaskManager.getInstance().runInBackground(stopTitle, () -> {
            this.setEnabled(false);
            this.value.stop();
            this.refresh();
        }));
        final String restartTitle = String.format("Restarting app(%s)", this.value.getName());
        this.restartButton.addActionListener(e -> AzureTaskManager.getInstance().runInBackground(restartTitle, () -> {
            this.setEnabled(false);
            this.value.restart();
            this.refresh();
        }));
        final String saveTitle = String.format("Saving updates of app(%s)", this.value.getName());
        this.saveButton.addActionListener(e -> AzureTaskManager.getInstance().runInBackground(saveTitle, () -> {
            this.setEnabled(false);
            this.reset.setVisible(false);
            final SpringCloudDeploymentDraft draft = (SpringCloudDeploymentDraft) Objects.requireNonNull(this.formConfig.getValue().getActiveDeployment());
            new DeploySpringCloudAppTask(draft).execute();
            this.refresh();
        }));
        this.formConfig.setDataChangedListener((data) -> {
            final boolean modified = this.formConfig.idModified();
            this.reset.setVisible(modified);
            this.saveButton.setEnabled(modified);
        });
        AzureEventBus.after("springcloud.remove_app.app", (SpringCloudApp app) -> {
            if (this.value.getName().equals(app.getName())) {
                AzureMessager.getMessager().info(String.format("Spring Cloud App(%s) is deleted", this.value.getName()), "");
                IntellijShowPropertiesViewAction.closePropertiesView(this.value, this.project);
            }
        });
        AzureEventBus.after("springcloud.update_app.app", (SpringCloudApp app) -> {
            if (this.value.getName().equals(app.getName())) {
                this.refresh();
            }
        });
    }

    private void refresh() {
        this.reset.setVisible(false);
        this.saveButton.setEnabled(false);
        AzureTaskManager.getInstance().runLater(() -> {
            final String refreshTitle = String.format("Refreshing app(%s)...", Objects.requireNonNull(this.value).getName());
            AzureTaskManager.getInstance().runInBackground(refreshTitle, () -> {
                this.value.refresh();
                final SpringCloudDeployment deployment = this.value.getActiveDeployment();
                if (Objects.nonNull(deployment)) {
                    deployment.refresh();
                }
                AzureTaskManager.getInstance().runLater(this::rerender);
            });
        });
    }

    private void setEnabled(boolean enabled) {
        this.saveButton.setEnabled(enabled);
        this.startButton.setEnabled(enabled);
        this.stopButton.setEnabled(enabled);
        this.restartButton.setEnabled(enabled);
        this.deleteButton.setEnabled(enabled);
        this.formConfig.setEnabled(enabled);
        this.panelInstances.setEnabled(enabled);
    }

    private void resetToolbar(@Nullable SpringCloudDeployment deployment) {
        if (Objects.isNull(deployment)) {
            AzureMessager.getMessager().warning(String.format("App(%s) has no active deployment", this.value.getName()), null);
            this.setEnabled(false);
            return;
        }
        final String status = deployment.getStatus();
        switch (status.toUpperCase()) {
            case "STOPPED":
                this.setEnabled(true);
                this.stopButton.setEnabled(false);
                this.restartButton.setEnabled(false);
                break;
            case "RUNNING":
                this.setEnabled(true);
                this.startButton.setEnabled(false);
                break;
            case "FAILED":
                this.setEnabled(false);
                this.deleteButton.setEnabled(true);
                break;
            case "ALLOCATING":
            case "UPGRADING":
            case "COMPILING":
            case "UNKNOWN":
                this.setEnabled(false);
                break;
            default:
        }
    }

    @Nonnull
    @Override
    public JComponent getComponent() {
        return contentPanel;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.value.getName();
    }

    @Override
    public void dispose() {
    }

    private void createUIComponents() {
    }
}
