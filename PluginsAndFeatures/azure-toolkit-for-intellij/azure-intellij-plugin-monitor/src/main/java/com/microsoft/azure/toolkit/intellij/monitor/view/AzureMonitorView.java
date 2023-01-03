package com.microsoft.azure.toolkit.intellij.monitor.view;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.AnActionLink;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.intellij.common.action.WhatsNewAction;
import com.microsoft.azure.toolkit.intellij.monitor.view.left.MonitorTreePanel;
import com.microsoft.azure.toolkit.intellij.monitor.view.right.MonitorTablePanel;
import com.microsoft.azure.toolkit.intellij.monitor.view.top.TimeRangeComboBox;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;

public class AzureMonitorView {
    private JPanel contentPanel;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private JPanel leftPanel;
    private JScrollPane rightPane;
    private MonitorTreePanel monitorTreePanel;
    private MonitorTablePanel monitorTablePanel;
    private JButton executeButton;
    private AzureComboBox<String> timeRangeComboBox;
    private ActionLink selectAction;
    private JLabel workspaceName;
    private LogAnalyticsWorkspace selectedWorkspace;

    public AzureMonitorView(Project project, @Nonnull Subscription subscription, @Nullable LogAnalyticsWorkspace logAnalyticsWorkspace) {
        this.selectedWorkspace = logAnalyticsWorkspace;
//        executeButton.addActionListener(e -> {
//            final String queryString = String.format("AppTraces | where TimeGenerated > datetime(%s) and TimeGenerated < datetime(%s)", "12-01-2022", "12-14-2022");
//            monitorTablePanel.setStatus("Loading");
//            AzureTaskManager.getInstance().runInBackground("Loading Azure Monitor data", () -> monitorTablePanel.setTableModel(selectedWorkspace.executeQuery(queryString)));
//        });
        monitorTablePanel.setStatus("Loading");
        AzureTaskManager.getInstance().runInBackground(AzureString.fromString("Loading tables"), () -> this.monitorTreePanel.refresh());
        AzureTaskManager.getInstance().runInBackground(AzureString.fromString("Loading logs"), () -> {
            final String queryString = "AppTraces | where TimeGenerated > ago(1d)";
            monitorTablePanel.setTableModel(selectedWorkspace.executeQuery(queryString));
        });
    }

    public JPanel getCenterPanel() {
        return contentPanel;
    }

    private void createUIComponents() {
        selectAction = new AnActionLink("Select Workspace", ActionManager.getInstance().getAction(WhatsNewAction.ID));
        timeRangeComboBox = new TimeRangeComboBox();
    }

}
