package com.microsoft.azure.toolkit.intellij.monitor.view.right;

import com.azure.monitor.query.models.LogsTableCell;
import com.intellij.ui.JBSplitter;
import com.microsoft.azure.toolkit.intellij.monitor.view.AzureMonitorView;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspace;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MonitorLogPanel {
    @Getter
    private final JBSplitter splitter;
    private final MonitorLogTablePanel monitorLogTablePanel;
    private final MonitorLogDetailsPanel monitorLogDetailsPanel;
    @Setter
    private boolean isTableTab;
    @Setter
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
        this.monitorLogTablePanel.setTableTab(this.isTableTab);
        executeQuery();
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
        this.monitorLogTablePanel.addRunActionListener(e -> executeQuery());
        this.parentView.getMonitorTreePanel().addTreeSelectionListener(e -> {
            if (!this.isTableTab) {
                return;
            }
            queryColumnNameList(e.getSource().toString());
            queryCellValueList(e.getSource().toString(), "_ResourceId");
            queryCellValueList(e.getSource().toString(), "SecurityLevel");
        });
    }

    private void executeQuery() {
        final LogAnalyticsWorkspace selectedWorkspace = this.parentView.getSelectedWorkspace();
        if (Objects.isNull(selectedWorkspace)) {
            AzureMessager.getMessager().warning("Please select log analytics workspace first");
            return;
        }
        final String queryString = this.isTableTab ? this.monitorLogTablePanel.getQueryStringFromFilters(this.parentView.getCurrentTreeNodeText()) : this.parentView.getCurrentTreeNodeText();
        this.monitorLogTablePanel.loadTableModel(selectedWorkspace, queryString);
        this.monitorLogDetailsPanel.setStatus("No table cell is selected");
    }

    private List<String> queryColumnNameList(String tableName) {
        return this.parentView.getSelectedWorkspace().getTableColumnNames(tableName);
    }

    private List<String> queryCellValueList(String tableName, String columnName) {
        final LogAnalyticsWorkspace selectedWorkspace = this.parentView.getSelectedWorkspace();
        final List<String> columnList = queryColumnNameList(tableName);
        if (!columnList.contains(columnName)) {
            return new ArrayList<>();
        }
        final String queryResource = String.format("%s | distinct %s | project %s", tableName, columnName, columnName);
        return selectedWorkspace.executeQuery(queryResource).getAllTableCells().stream().map(LogsTableCell::getValueAsString).toList();
    }
}
