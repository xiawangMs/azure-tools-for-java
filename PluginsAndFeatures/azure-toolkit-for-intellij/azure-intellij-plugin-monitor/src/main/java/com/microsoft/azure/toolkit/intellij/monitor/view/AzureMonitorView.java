package com.microsoft.azure.toolkit.intellij.monitor.view;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.AnActionLink;
import com.microsoft.azure.toolkit.intellij.monitor.view.left.TreePanel;
import com.microsoft.azure.toolkit.intellij.monitor.view.right.TablePanel;
import com.microsoft.azure.toolkit.intellij.monitor.view.right.TimeRangeComboBox;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspace;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AzureMonitorView {
    private JPanel contentPanel;
    private JPanel leftPanel;
    private ActionLink changeWorkspace;
    private TreePanel treePanel;
    private JLabel workspaceName;
    private JPanel workspaceHeader;
    private TablePanel monitorTablePanel;
    private LogAnalyticsWorkspace selectedWorkspace;

    public AzureMonitorView(Project project, @Nullable LogAnalyticsWorkspace logAnalyticsWorkspace) {
        this.selectedWorkspace = logAnalyticsWorkspace;
        AzureTaskManager.getInstance().runInBackground(AzureString.fromString("Loading logs"), () -> {
            this.treePanel.refresh();
            this.executeQuery();
        });
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
        final List<String> queryParams = Arrays.asList(tableName, TimeRangeComboBox.TimeRange.LAST_24_HOURS.getKustoString());
        final String queryString = StringUtils.join(queryParams, " | ");
        AzureTaskManager.getInstance().runInBackground("Loading Azure Monitor data", () -> monitorTablePanel.setTableModel(selectedWorkspace.executeQuery(queryString)));
    }

    private void createUIComponents() {
        this.changeWorkspace = new AnActionLink("Select Workspace", new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {

            }
        });
    }

}
