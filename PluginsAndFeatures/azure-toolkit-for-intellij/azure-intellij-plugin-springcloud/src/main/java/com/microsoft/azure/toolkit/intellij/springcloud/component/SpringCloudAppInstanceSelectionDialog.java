/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.springcloud.component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ListCellRendererWithRightAlignedComponent;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudAppInstance;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.List;

public class SpringCloudAppInstanceSelectionDialog extends DialogWrapper {

    private JPanel pnlRoot;
    private AzureComboBox<SpringCloudAppInstance> cbInstances;
    private JLabel tipsLabel;

    private SpringCloudAppInstance instance;

    public SpringCloudAppInstanceSelectionDialog(@Nullable final Project project, List<SpringCloudAppInstance> instances) {
        super(project, false);
        setTitle("Select Instance");
        cbInstances.setRenderer(new ListCellRendererWithRightAlignedComponent<>() {
            @Override
            protected void customize(final SpringCloudAppInstance deploymentInstance) {
                setLeftText(deploymentInstance.getName());
            }
        });
        cbInstances.setItemsLoader(() -> instances);
        tipsLabel.setVisible(instances.size() == 0);
        init();
        initListener();
    }

    public SpringCloudAppInstance getInstance() {
        return instance;
    }

    @Override
    protected void doOKAction() {
        instance = (SpringCloudAppInstance) cbInstances.getSelectedItem();
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        instance = null;
        super.doCancelAction();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return pnlRoot;
    }

    private void initListener() {
        cbInstances.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                tipsLabel.setVisible(false);
            }
        });
    }

}
