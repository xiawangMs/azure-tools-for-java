/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerregistry.component;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.AzureTextInput;
import com.microsoft.azure.toolkit.intellij.container.model.DockerImage;
import com.microsoft.azure.toolkit.intellij.container.model.DockerPushConfiguration;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.containerregistry.AzureContainerRegistry;
import com.microsoft.azure.toolkit.lib.containerregistry.ContainerRegistry;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class DockerImageConfigurationPanel implements AzureForm<DockerPushConfiguration> {
    @Getter
    private JPanel pnlRoot;
    private AzureDockerHostComboBox cbDockerHost;
    private AzureDockerImageComboBox cbDockerImage;
    private AzureTextInput txtRepositoryName;
    private JLabel lblRepository;
    private JLabel lblTagName;
    private AzureTextInput txtTagName;
    private AzureContainerRegistryComboBox cbContainerRegistry;
    private JLabel lblRepositoryPrefix;
    private JPanel pnlImageName;
    private JPanel pnlContainerRegistry;

    private final Project project;
    private final AtomicReference<String> repositoryReference = new AtomicReference<>();
    @Getter
    @Setter
    private boolean enableCustomizedImageName = true;
    private DockerImage image;

    public DockerImageConfigurationPanel(final Project project) {
        this.project = project;
        $$$setupUI$$$();
        this.init();
    }

    private void init() {
        this.cbDockerHost.setRequired(true);
        this.cbDockerImage.setRequired(true);
        this.cbContainerRegistry.addItemListener(this::onSelectContainerRegistry);
        this.cbDockerHost.addItemListener(this::onDockerHostChanged);
        this.cbDockerImage.addValueChangedListener(this::onSelectNewDockerImage);
    }

    public void enableContainerRegistryPanel() {
        pnlContainerRegistry.setVisible(true);
        cbContainerRegistry.reloadItems();
        cbContainerRegistry.setRequired(true);
    }

    private void onSelectContainerRegistry(ItemEvent itemEvent) {
        final ContainerRegistry value = cbContainerRegistry.getValue();
        Mono.fromCallable(() -> Optional.ofNullable(value).map(r -> r.getLoginServerUrl()).orElse(null))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(loginServerUrl -> AzureTaskManager.getInstance().runLater(() -> {
                    lblRepositoryPrefix.setText(" " + loginServerUrl + "/");
                    lblRepositoryPrefix.setVisible(StringUtils.isNotBlank(loginServerUrl));
                    repositoryReference.set(loginServerUrl);
                    final String repositoryName = txtRepositoryName.getValue();
                    if (StringUtils.startsWith(repositoryName, loginServerUrl)) {
                        txtRepositoryName.setValue(StringUtils.removeStart(repositoryName, loginServerUrl));
                    }
                }, AzureTask.Modality.ANY));
    }

    private void onSelectNewDockerImage(final DockerImage image) {
        // workaround for reload issue
        if (Objects.equals(image, this.image)) {
            return;
        } else if (Objects.isNull(image)) {
            AzureTaskManager.getInstance().runLater(() -> {
                txtRepositoryName.setValue(StringUtils.EMPTY);
                txtTagName.setValue(StringUtils.EMPTY);
            }, AzureTask.Modality.ANY);
            return;
        }
        this.image = image;
        final boolean isDraftImage = image.isDraft();
        final String loginServerUrl = repositoryReference.get();
        final String repositoryName = image.getRepositoryName();
        final String fixedRepositoryName = StringUtils.isNotEmpty(loginServerUrl) && StringUtils.startsWith(repositoryName, loginServerUrl) ?
                StringUtils.removeStart(repositoryName, loginServerUrl + "/") : repositoryName;
        AzureTaskManager.getInstance().runLater(() -> {
            txtRepositoryName.setValue(fixedRepositoryName);
            txtTagName.setValue(image.getTagName());
            pnlImageName.setVisible(enableCustomizedImageName || isDraftImage);
        }, AzureTask.Modality.ANY);
    }

    private void onDockerHostChanged(@Nullable final ItemEvent event) {
        this.cbDockerImage.setDockerHost(cbDockerHost.getValue());
    }

    @Override
    public DockerPushConfiguration getValue() {
        final DockerPushConfiguration result = new DockerPushConfiguration();
        final DockerImage image = this.cbDockerImage.getValue();
        final ContainerRegistry registry = this.cbContainerRegistry.getValue();
        result.setDockerHost(this.cbDockerHost.getValue());
        result.setDockerImage(image);
        if (Optional.ofNullable(image).map(DockerImage::isDraft).orElse(false)) {
            image.setRepositoryName(this.txtRepositoryName.getValue());
            image.setTagName(this.txtTagName.getValue());
        }
        Optional.ofNullable(registry).ifPresent(cr -> {
            result.setContainerRegistryId(cr.getId());
            result.setFinalRepositoryName(cr.getLoginServerUrl() + "/" + this.txtRepositoryName.getValue());
            result.setFinalTagName(this.txtTagName.getValue());
        });
        return result;
    }

    @Override
    public void setValue(@Nonnull final DockerPushConfiguration data) {
        Optional.ofNullable(data.getDockerHost()).ifPresent(host -> {
            cbDockerHost.getDrafts().add(host);
            cbDockerHost.setValue(h -> StringUtils.equalsIgnoreCase(host.getDockerHost(), h.getDockerHost()));
        });
        Optional.ofNullable(data.getDockerImage()).ifPresent(i -> {
            this.image = i;
            this.cbDockerImage.setValue(i);
        });
        Optional.ofNullable(data.getContainerRegistryId())
            .map(id -> (ContainerRegistry) Azure.az(AzureContainerRegistry.class).getById(id))
            .ifPresent(cbContainerRegistry::setValue);
        final String repositoryName = Optional.ofNullable(data.getFinalRepositoryName())
                .orElseGet(() -> Optional.ofNullable(data.getDockerImage()).map(DockerImage::getRepositoryName).orElse(null));
        final String tagName = Optional.ofNullable(data.getFinalTagName())
                .orElseGet(() -> Optional.ofNullable(data.getDockerImage()).map(DockerImage::getTagName).orElse(null));
        Optional.ofNullable(repositoryName).ifPresent(txtRepositoryName::setValue);
        Optional.ofNullable(tagName).ifPresent(txtTagName::setValue);
        final boolean isDraftImage = Optional.ofNullable(data.getDockerImage()).map(DockerImage::isDraft).orElse(false);
        pnlImageName.setVisible(isDraftImage || enableCustomizedImageName);
    }

    public void addImageListener(@Nonnull final AzureValueChangeListener<DockerImage> listener) {
        this.cbDockerImage.addValueChangedListener(listener);
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Arrays.asList(cbDockerHost, cbDockerImage);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        this.cbDockerHost = new AzureDockerHostComboBox(project);
        this.cbDockerImage = new AzureDockerImageComboBox(project);
        this.cbContainerRegistry = new AzureContainerRegistryComboBox(true);
    }

    private void $$$setupUI$$$() {
    }
}
