/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.appservice;

import com.intellij.openapi.project.Project;
import com.intellij.ui.TitledSeparator;
import com.microsoft.azure.toolkit.intellij.appservice.platform.RuntimeComboBox;
import com.microsoft.azure.toolkit.intellij.common.AzureFormPanel;
import com.microsoft.azure.toolkit.lib.appservice.AppServiceConfig;
import com.microsoft.azure.toolkit.lib.appservice.DraftServicePlan;
import com.microsoft.azure.toolkit.lib.appservice.model.Runtime;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.DraftResourceGroup;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import static com.microsoft.azure.toolkit.lib.Azure.az;

public class AppServiceInfoBasicPanel<T extends AppServiceConfig> extends JPanel implements AzureFormPanel<T> {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyMMddHHmmss");
    private static final int RG_NAME_MAX_LENGTH = 90;
    private static final int SP_NAME_MAX_LENGTH = 40;
    private final Project project;
    private final Supplier<? extends T> supplier;
    private T config;

    private JPanel contentPanel;

    private AppNameInput textName;
    private RuntimeComboBox selectorRuntime;
    private TitledSeparator deploymentTitle;
    private JLabel deploymentLabel;

    private Subscription subscription;

    public AppServiceInfoBasicPanel(final Project project, final Supplier<? extends T> defaultConfigSupplier) {
        super();
        this.project = project;
        this.supplier = defaultConfigSupplier;
        $$$setupUI$$$(); // tell IntelliJ to call createUIComponents() here.
        this.init();
    }

    private void init() {
        this.subscription = az(AzureAccount.class).account().getSelectedSubscriptions().get(0);
        this.textName.setRequired(true);
        this.textName.setSubscription(subscription);
        this.selectorRuntime.setRequired(true);

        this.setDeploymentVisible(false);
        this.config = initConfig();
        setData(this.config);
    }

    @SneakyThrows
    @Override
    public T getData() {
        final String name = this.textName.getValue();
        final Runtime platform = this.selectorRuntime.getValue();

        final T result = (T) (this.config == null ? initConfig() : this.config).toBuilder().build();
        result.setName(name);
        result.setRuntime(platform);

        this.config = result;
        return result;
    }

    private T initConfig() {
        final String appName = String.format("app-%s-%s", this.project.getName(), DATE_FORMAT.format(new Date()));
        final DraftResourceGroup group = new DraftResourceGroup(subscription, StringUtils.substring(String.format("rg-%s", appName), 0, RG_NAME_MAX_LENGTH));
        group.setSubscription(subscription);
        final T result = supplier.get(); // need platform region pricing
        final String planName = StringUtils.substring(String.format("sp-%s", appName), 0, SP_NAME_MAX_LENGTH);
        final DraftServicePlan plan = new DraftServicePlan(subscription, planName, result.getRegion(), result.getRuntime().getOperatingSystem(),
                result.getPricingTier());
        result.setName(appName);
        result.setResourceGroup(group);
        result.setSubscription(subscription);
        result.setResourceGroup(group);
        result.setServicePlan(plan);
        return result;
    }

    @Override
    public void setData(final T config) {
        this.textName.setValue(config.getName());
        this.selectorRuntime.setValue(config.getRuntime());
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        final AzureFormInput<?>[] inputs = {
            this.textName,
            this.selectorRuntime,
        };
        return Arrays.asList(inputs);
    }

    @Override
    public void setVisible(final boolean visible) {
        this.contentPanel.setVisible(visible);
        super.setVisible(visible);
    }

    public RuntimeComboBox getSelectorRuntime() {
        return selectorRuntime;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public void setDeploymentVisible(boolean visible) {
        this.deploymentTitle.setVisible(visible);
        this.deploymentLabel.setVisible(visible);
    }
}
