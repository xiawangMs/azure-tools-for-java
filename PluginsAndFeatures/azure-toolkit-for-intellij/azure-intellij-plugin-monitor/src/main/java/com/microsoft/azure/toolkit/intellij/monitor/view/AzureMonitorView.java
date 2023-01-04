package com.microsoft.azure.toolkit.intellij.monitor.view;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.AnActionLink;
import com.microsoft.azure.toolkit.intellij.common.action.WhatsNewAction;
import com.microsoft.azure.toolkit.intellij.monitor.view.left.MonitorTreePanel;
import com.microsoft.azure.toolkit.intellij.monitor.view.right.MonitorTablePanel;
import com.microsoft.azure.toolkit.intellij.monitor.view.top.TimeRangeComboBox;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspace;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AzureMonitorView {
    private JPanel contentPanel;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private JPanel leftPanel;
    private JScrollPane rightPane;
    private MonitorTreePanel monitorTreePanel;
    private MonitorTablePanel monitorTablePanel;
    private JButton executeButton;
    private TimeRangeComboBox timeRangeComboBox;
    private ActionLink selectAction;
    private JLabel workspaceName;
    private LogAnalyticsWorkspace selectedWorkspace;

    public AzureMonitorView(Project project, @Nullable LogAnalyticsWorkspace logAnalyticsWorkspace) {
        this.selectedWorkspace = logAnalyticsWorkspace;
        executeButton.addActionListener(e -> executeQuery());
        AzureTaskManager.getInstance().runInBackground(AzureString.fromString("Loading tables"), () -> this.monitorTreePanel.refresh());
        AzureTaskManager.getInstance().runInBackground(AzureString.fromString("Loading logs"), this::executeQuery);
    }

    public JPanel getCenterPanel() {
        return contentPanel;
    }

    private void executeQuery() {
        if (Objects.isNull(selectedWorkspace)) {
            AzureMessager.getMessager().warning("Please select log analytics workspace");
            return;
        }
        final String tableName = "AppTraces";
        final List<String> queryParams = Arrays.asList(tableName, timeRangeComboBox.getValue().getKustoString());
        final String queryString = StringUtils.join(queryParams, " | ");
        AzureTaskManager.getInstance().runInBackground("Loading Azure Monitor data", () -> monitorTablePanel.setTableModel(selectedWorkspace.executeQuery(queryString)));
    }

    private void createUIComponents() {
        selectAction = new AnActionLink("Select Workspace", ActionManager.getInstance().getAction(WhatsNewAction.ID));
        timeRangeComboBox = new TimeRangeComboBox();
    }

}
