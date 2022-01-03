/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.springcloud.creation;

import com.microsoft.azure.toolkit.intellij.common.AzureTextInput;
import com.microsoft.azure.toolkit.intellij.common.component.SubscriptionComboBox;
import com.microsoft.azure.toolkit.intellij.springcloud.component.SpringCloudAppConfigPanel;
import com.microsoft.azure.toolkit.intellij.springcloud.component.SpringCloudClusterComboBox;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudAppDraft;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudCluster;
import lombok.AccessLevel;
import lombok.Getter;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.List;

@Getter(AccessLevel.PROTECTED)
public class SpringCloudAppInfoAdvancedPanel extends AbstractSpringCloudAppInfoPanel {
    private JPanel contentPanel;
    private SubscriptionComboBox selectorSubscription;
    private SpringCloudClusterComboBox selectorCluster;
    private AzureTextInput textName;
    private SpringCloudAppConfigPanel formConfig;

    public SpringCloudAppInfoAdvancedPanel(@Nullable final SpringCloudCluster cluster) {
        super(cluster);
        $$$setupUI$$$();
        this.init();
    }

    @Override
    public SpringCloudAppDraft getValue() {
        final SpringCloudAppDraft appDraft = this.formConfig.getValue();
        return super.getValue(appDraft);
    }

    @Override
    public void setValue(final SpringCloudAppDraft config) {
        super.setValue(config);
        this.formConfig.setValue(config);
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        final List<AzureFormInput<?>> inputs = this.formConfig.getInputs();
        inputs.addAll(super.getInputs());
        return inputs;
    }
}
