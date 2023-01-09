package com.microsoft.azure.toolkit.intellij.monitor.view;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.AnActionLink;
import com.microsoft.azure.toolkit.intellij.monitor.view.left.TreePanel;
import com.microsoft.azure.toolkit.intellij.monitor.view.left.WorkspaceSelectionDialog;
import com.microsoft.azure.toolkit.intellij.monitor.view.right.TablePanel;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspace;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Optional;

public class AzureMonitorView {
    private JPanel contentPanel;
    private JPanel leftPanel;
    private ActionLink changeWorkspace;
    private TreePanel treePanel;
    private JLabel workspaceName;
    private JPanel workspaceHeader;
    private TablePanel logTablePanel;
    @Getter
    private LogAnalyticsWorkspace selectedWorkspace;
    private final boolean isTableTab;

    public AzureMonitorView(Project project, @Nullable LogAnalyticsWorkspace logAnalyticsWorkspace, boolean isTableTab) {
        this.selectedWorkspace = logAnalyticsWorkspace;
        $$$setupUI$$$(); // tell IntelliJ to call createUIComponents() here.
        this.workspaceName.setText(Optional.ofNullable(selectedWorkspace).map(LogAnalyticsWorkspace::getName).orElse(StringUtils.EMPTY));
        this.isTableTab = isTableTab;
        this.treePanel.setTableTab(isTableTab);
        this.logTablePanel.setTableTab(isTableTab);
        this.logTablePanel.setParentView(this);
        AzureTaskManager.getInstance().runInBackground(AzureString.fromString("Loading logs"), () -> {
            this.treePanel.refresh();
            this.logTablePanel.executeQuery();
        });
    }

    public JPanel getCenterPanel() {
        return contentPanel;
    }

    public String getCurrentTreeNodeText() {
        return this.treePanel.getCurrentNodeText();
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
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
        this.logTablePanel = new TablePanel();
        this.treePanel = new TreePanel();
    }

}
