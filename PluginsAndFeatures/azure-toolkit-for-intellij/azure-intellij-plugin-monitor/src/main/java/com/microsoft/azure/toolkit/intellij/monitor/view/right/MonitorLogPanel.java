package com.microsoft.azure.toolkit.intellij.monitor.view.right;

import com.intellij.ui.JBSplitter;
import com.microsoft.azure.toolkit.intellij.monitor.view.AzureMonitorView;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspace;
import lombok.Getter;
import lombok.Setter;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Objects;
import java.util.Optional;

public class MonitorLogPanel {
    @Getter
    private final JBSplitter splitter;
    private final MonitorLogTablePanel monitorLogTablePanel;
    private final MonitorLogDetailsPanel monitorLogDetailsPanel;
    @Setter
    private boolean isTableTab;
    private AzureMonitorView parentView;

    public MonitorLogPanel() {
        this.splitter = new JBSplitter();
        this.monitorLogTablePanel = new MonitorLogTablePanel();
        this.monitorLogDetailsPanel = new MonitorLogDetailsPanel();
        this.splitter.setProportion(0.8f);
        this.splitter.setFirstComponent(this.monitorLogTablePanel.getContentPanel());
        this.splitter.setSecondComponent(this.monitorLogDetailsPanel.getContentPanel());
        this.initListener();
    }

    public void refresh() {
        loadFilters(this.parentView.getCurrentTreeNodeText());
        loadLogs();
    }

    public void setParentView(AzureMonitorView monitorView) {
        this.parentView = monitorView;
        this.parentView.getMonitorTreePanel().addTreeSelectionListener(e -> Optional.ofNullable(e.getNewLeadSelectionPath())
                .ifPresent(it -> {
                    final DefaultMutableTreeNode node = (DefaultMutableTreeNode) it.getLastPathComponent();
                    if (Objects.nonNull(node) && node.isLeaf()) {
                        loadFilters(node.getUserObject().toString());
                    }
                }));
    }

    private void initListener() {
        this.monitorLogTablePanel.addTableSelectionListener(e -> {
            final String viewerTitle = monitorLogTablePanel.getSelectedColumnName();
            final String viewerText = monitorLogTablePanel.getSelectedCellValue();
            if (!e.getValueIsAdjusting() || Objects.isNull(viewerText) || Objects.isNull(viewerTitle)) {
                return;
            }
            this.monitorLogDetailsPanel.setViewText(viewerTitle, viewerText);
        });
        this.monitorLogTablePanel.addRunActionListener(e -> loadLogs());
    }

    private void loadLogs() {
        final LogAnalyticsWorkspace selectedWorkspace = this.parentView.getSelectedWorkspace();
        if (Objects.isNull(selectedWorkspace)) {
            AzureMessager.getMessager().warning("Please select log analytics workspace first");
            return;
        }
        final String queryString = this.isTableTab ? this.monitorLogTablePanel.getQueryStringFromFilters(this.parentView.getCurrentTreeNodeText()) : this.parentView.getCurrentTreeNodeText();
        this.monitorLogTablePanel.loadTableModel(selectedWorkspace, queryString);
        this.monitorLogDetailsPanel.setStatus("No table cell is selected");
    }

    private void loadFilters(String tableName) {
        if (!this.isTableTab) {
            return;
        }
        final LogAnalyticsWorkspace selectedWorkspace = this.parentView.getSelectedWorkspace();
        this.monitorLogTablePanel.loadFilters(selectedWorkspace, tableName);
    }

}
