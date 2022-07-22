/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.vm.runtarget;

import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.application.ModalityState;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.ConnectionData;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetWizardModel;
import com.microsoft.azure.toolkit.intellij.common.component.SubscriptionComboBox;
import com.microsoft.azure.toolkit.intellij.vm.creation.component.VirtualMachineComboBox;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.compute.virtualmachine.VirtualMachine;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.Objects;
import java.util.Optional;

public class VirtualMachineConnectionConfigurable {
    private final SshTargetWizardModel model;
    @Getter
    private JPanel contentPanel;
    private SubscriptionComboBox cbSubscription;
    private VirtualMachineComboBox cbVirtualMachine;
    private JTextField txtUsername;

    public VirtualMachineConnectionConfigurable(SshTargetWizardModel model) {
        this.model = model;
        this.$$$setupUI$$$();
        this.init();
    }

    public void init() {
        this.txtUsername.setText(System.getProperty("user.name"));
        cbSubscription.addItemListener(this::onSubscriptionChanged);
        cbVirtualMachine.addItemListener(this::onVirtualMachineChanged);
    }

    public VirtualMachine apply() throws CommitStepException {
        final VirtualMachine vm = this.cbVirtualMachine.getValue();
        if (Objects.isNull(vm)) {
            throw new CommitStepException("no vm is selected.");
        }
        this.model.getSubject().setDisplayName(vm.getName());
        final ConnectionData connectionData = this.model.getConnectionData();
        connectionData.setUseExistingConfig(false);
        connectionData.setSavePassphrase(true);
        connectionData.setOpenSshAgentConnectionState(ConnectionData.OpenSshAgentConnectionState.NOT_STARTED);
        connectionData.setUsername(txtUsername.getText());
//        AzureTaskManager.getInstance().runOnPooledThread(() -> {
        final String hostIp = vm.getHostIp();
        if (StringUtils.isBlank(hostIp)) {
            throw new CommitStepException("ssh is not enabled for the selected vm.");
        }
        connectionData.setHost(Optional.of(hostIp).orElse(""));
        connectionData.checkAgentConnection(model.getProject(), ModalityState.any());
        return vm;
//        });
    }

    private void onSubscriptionChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Subscription) {
            final Subscription subscription = (Subscription) e.getItem();
            this.cbVirtualMachine.setSubscription(subscription);
        }
    }

    private void onVirtualMachineChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Subscription) {
            final VirtualMachine vm = (VirtualMachine) e.getItem();
        }
    }

    public JComponent getPreferredFocusedComponent() {
        return this.cbVirtualMachine;
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    private void $$$setupUI$$$() {
    }
}
