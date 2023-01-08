package com.microsoft.azure.toolkit.intellij.monitor.view.left;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.microsoft.azure.toolkit.intellij.common.component.SubscriptionComboBox;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.monitor.AzureLogAnalyticsWorkspace;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspace;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspaceConfig;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.Objects;
import java.util.Optional;

public class WorkspaceSelectionDialog extends DialogWrapper {
    private JPanel centerPanel;
    private SubscriptionComboBox subComboBox;
    private WorkspaceComboBox workspaceComboBox;
    private LogAnalyticsWorkspace selectedWorkspace;

    public WorkspaceSelectionDialog(@Nullable final Project project, LogAnalyticsWorkspace value) {
        super(project, false);
        setTitle("Select Log Analytics Workspace");
        init();
        subComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                final Subscription subscription = (Subscription) e.getItem();
                workspaceComboBox.setSubscription(subscription);
            } else {
                workspaceComboBox.setSubscription(null);
            }
        });
        subComboBox.setValue(value.getSubscription());
        workspaceComboBox.setValue(
                LogAnalyticsWorkspaceConfig.builder()
                        .newCreate(false)
                        .subscriptionId(value.getSubscriptionId())
                        .resourceId(value.getId())
                        .name(value.getName())
                        .regionName(Optional.ofNullable(value.getRegion()).map(Region::getName).orElse(StringUtils.EMPTY))
                        .build());
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return centerPanel;
    }

    @Override
    protected void doOKAction() {
        final LogAnalyticsWorkspaceConfig config = this.workspaceComboBox.getValue();
        final Subscription subscription = this.subComboBox.getValue();
        selectedWorkspace = Azure.az(AzureLogAnalyticsWorkspace.class)
                .logAnalyticsWorkspaces(subscription.getId()).list().stream()
                .filter(logAnalyticsWorkspace -> Objects.equals(logAnalyticsWorkspace.getId(), config.getResourceId()))
                .findFirst().orElse(null);
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        selectedWorkspace = null;
        super.doCancelAction();
    }

    @Nullable
    public LogAnalyticsWorkspace getWorkspace() {
        return selectedWorkspace;
    }

    private void createUIComponents() {
        subComboBox = new SubscriptionComboBox();
        workspaceComboBox = new WorkspaceComboBox();
    }

}
