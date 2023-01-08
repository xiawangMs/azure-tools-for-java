package com.microsoft.azure.toolkit.intellij.monitor.view;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.AnActionLink;
import com.microsoft.azure.toolkit.intellij.monitor.view.left.TreePanel;
import com.microsoft.azure.toolkit.intellij.monitor.view.left.WorkspaceSelectionDialog;
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
import java.util.Optional;

public class AzureMonitorView {
    private JPanel contentPanel;
    private JPanel leftPanel;
    private ActionLink changeWorkspace;
    private TreePanel treePanel;
    private JLabel workspaceName;
    private JPanel workspaceHeader;
    private TablePanel logTablePanel;
    private LogAnalyticsWorkspace selectedWorkspace;
    private final boolean isTableTab;

    public AzureMonitorView(Project project, @Nullable LogAnalyticsWorkspace logAnalyticsWorkspace, boolean isTableTab) {
        this.selectedWorkspace = logAnalyticsWorkspace;
        this.workspaceName.setText(Optional.ofNullable(selectedWorkspace).map(LogAnalyticsWorkspace::getName).orElse(StringUtils.EMPTY));
        this.isTableTab = isTableTab;
        this.treePanel.setTableTab(isTableTab);
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
            AzureMessager.getMessager().warning("Please select log analytics workspace first");
            return;
        }
        final String queryString = this.isTableTab ? getQueryStringFromFilters() : treePanel.getCurrentNodeText();
        AzureTaskManager.getInstance().runInBackground("Loading Azure Monitor data", () -> logTablePanel.setTableModel(selectedWorkspace.executeQuery(queryString)));
    }

    private String getQueryStringFromFilters() {
        final List<String> queryParams = Arrays.asList(treePanel.getCurrentNodeText(), TimeRangeComboBox.TimeRange.LAST_24_HOURS.getKustoString());
        return StringUtils.join(queryParams, " | ");
    }

    private void createUIComponents() {
        this.changeWorkspace = new AnActionLink("Change", new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                final WorkspaceSelectionDialog dialog = new WorkspaceSelectionDialog(e.getProject(), selectedWorkspace);
                AzureTaskManager.getInstance().runLater(() -> {
                    if (dialog.showAndGet()) {
                        Optional.ofNullable(dialog.getWorkspace()).ifPresent(w -> {
                            selectedWorkspace = w;
                            workspaceName.setText(selectedWorkspace.getName());
                        });
                    }
                });
            }
        });
    }

}
