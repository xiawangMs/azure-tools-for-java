package com.microsoft.azure.toolkit.intellij.monitor.view;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.AnActionLink;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.intellij.common.action.WhatsNewAction;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

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
//    private JLabel workspaceName;
    private ActionLink selectAction;
    private LogAnalyticsWorkspace selectedWorkspace;

    public AzureMonitorView(Project project, @Nonnull Subscription subscription, @Nullable LogAnalyticsWorkspace logAnalyticsWorkspace) {
        this.selectedWorkspace = logAnalyticsWorkspace;
    }

    public JPanel getCenterPanel() {
        return contentPanel;
    }

    public void initListener() {
//        Optional.ofNullable(selectedWorkspace).ifPresent(workspace -> workspaceName.setText(workspace.getName()));
        executeButton.addActionListener(e -> {
            final String queryString = String.format("AppTraces | where TimeGenerated > datetime(%s) and TimeGenerated < datetime(%s)", "12-01-2022", "12-14-2022");
            monitorTablePanel.setTableModel(selectedWorkspace.executeQuery(queryString));
        });
        AzureTaskManager.getInstance().runInBackground(AzureString.fromString("loading Azure Monitor data"), () -> this.monitorTreePanel.refresh());
    }

    private void createUIComponents() {
        selectAction = new AnActionLink("Select Workspace", ActionManager.getInstance().getAction(WhatsNewAction.ID));
        final List<String> timeRangeList = new ArrayList<>();
        timeRangeList.add("Last 4 hours");
        timeRangeList.add("Last 12 hours");
        timeRangeList.add("Last 24 hours");
        timeRangeList.add("Last 2 days");
        timeRangeComboBox = new AzureComboBox<>(() -> timeRangeList);
//        workspaceName = new JLabel();
    }

}
