/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.springcloud.creation;

import com.microsoft.azure.toolkit.intellij.common.AzureComboBox.ItemReference;
import com.microsoft.azure.toolkit.intellij.common.AzureFormPanel;
import com.microsoft.azure.toolkit.intellij.common.AzureTextInput;
import com.microsoft.azure.toolkit.intellij.common.component.SubscriptionComboBox;
import com.microsoft.azure.toolkit.intellij.springcloud.component.SpringCloudClusterComboBox;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo.AzureValidationInfoBuilder;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessageBundle;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudAppDraft;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudCluster;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudDeploymentDraft;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractSpringCloudAppInfoPanel extends JPanel implements AzureFormPanel<SpringCloudAppDraft> {
    private static final String SPRING_CLOUD_APP_NAME_PATTERN = "^[a-z][a-z0-9-]{2,30}[a-z0-9]$";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    @Nullable
    private final SpringCloudCluster cluster;
    private final String defaultAppName;
    private SpringCloudAppDraft value;

    public AbstractSpringCloudAppInfoPanel(@Nullable final SpringCloudCluster cluster) {
        super();
        this.cluster = cluster;
        this.defaultAppName = String.format("springcloud-app-%s", DATE_FORMAT.format(new Date()));
    }

    protected void init() {
        final SubscriptionComboBox selectorSubscription = this.getSelectorSubscription();
        final SpringCloudClusterComboBox selectorCluster = this.getSelectorCluster();
        final AzureTextInput textName = this.getTextName();
        selectorSubscription.setRequired(true);
        selectorSubscription.addItemListener(this::onSubscriptionChanged);
        selectorCluster.setRequired(true);
        selectorCluster.addItemListener(this::onClusterChanged);
        textName.setRequired(true);
        textName.setValue(this.defaultAppName);
        textName.addValidator(() -> {
            try {
                validateSpringCloudAppName(textName.getValue(), this.cluster);
            } catch (final IllegalArgumentException e) {
                final AzureValidationInfoBuilder builder = AzureValidationInfo.builder();
                return builder.input(textName).type(AzureValidationInfo.Type.ERROR).message(e.getMessage()).build();
            }
            return AzureValidationInfo.success(this);
        });
        if (Objects.nonNull(this.cluster)) {
            selectorSubscription.setValue(new ItemReference<>(this.cluster.getSubscriptionId(), Subscription::getId));
            selectorCluster.setValue(new ItemReference<>(this.cluster.getName(), AzResource::getName));
        }
    }

    private void onSubscriptionChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            final Subscription subscription = this.getSelectorSubscription().getValue();
            this.getSelectorCluster().setSubscription(subscription);
        }
    }

    private void onClusterChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            final SpringCloudCluster c = this.getSelectorCluster().getValue();
            final String appName = StringUtils.firstNonBlank(this.getTextName().getName(), this.defaultAppName);
            if (Objects.nonNull(c)) {
                final SpringCloudAppDraft appDraft = c.apps().updateOrCreate(appName, c.getResourceGroup());
                final SpringCloudDeploymentDraft deploymentDraft = appDraft.updateOrCreateActiveDeployment();
                this.onAppChanged(appDraft);
            }
        }
    }

    protected void onAppChanged(SpringCloudAppDraft appDraft) {
        AzureTaskManager.getInstance().runLater(() -> this.setValue(appDraft), AzureTask.Modality.ANY);
    }

    protected SpringCloudAppDraft getValue(@Nonnull SpringCloudAppDraft draft) {
        draft.setName(this.getTextName().getValue());
        return draft;
    }

    @Override
    public SpringCloudAppDraft getValue() {
        return this.getValue(this.value);
    }

    @Override
    public synchronized void setValue(final SpringCloudAppDraft app) {
        this.value = app;
        final SpringCloudDeploymentDraft deploymentDraft = app.updateOrCreateActiveDeployment();
        final Integer count = deploymentDraft.getInstanceNum();
        deploymentDraft.setInstanceNum(Objects.isNull(count) || count == 0 ? 1 : count);
        this.getTextName().setValue(app.getName());
        this.getSelectorCluster().setValue(new ItemReference<>(app.getParent().getName(), AzResource::getName));
        this.getSelectorSubscription().setValue(new ItemReference<>(app.getSubscriptionId(), Subscription::getId));
    }

    @Override
    public void setVisible(final boolean visible) {
        this.getContentPanel().setVisible(visible);
        super.setVisible(visible);
    }

    public static void validateSpringCloudAppName(final String name, final SpringCloudCluster cluster) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException(AzureMessageBundle.message("springcloud.app.name.validate.empty").toString());
        } else if (!name.matches(SPRING_CLOUD_APP_NAME_PATTERN)) {
            throw new IllegalArgumentException(AzureMessageBundle.message("springcloud.app.name.validate.invalid").toString());
        } else if (Objects.nonNull(cluster) && Objects.nonNull(cluster.apps().get(name, cluster.getResourceGroup()))) {
            throw new IllegalArgumentException(AzureMessageBundle.message("springcloud.app.name.validate.exist", name).toString());
        }
    }

    protected abstract SubscriptionComboBox getSelectorSubscription();

    protected abstract SpringCloudClusterComboBox getSelectorCluster();

    protected abstract AzureTextInput getTextName();

    protected abstract JPanel getContentPanel();

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Arrays.asList(
            this.getSelectorSubscription(),
            this.getSelectorCluster(),
            this.getTextName()
        );
    }
}
