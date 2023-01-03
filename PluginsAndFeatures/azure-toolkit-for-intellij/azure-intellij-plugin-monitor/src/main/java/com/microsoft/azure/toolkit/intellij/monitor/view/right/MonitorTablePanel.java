package com.microsoft.azure.toolkit.intellij.monitor.view.right;

import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableCell;
import com.intellij.ui.components.JBTreeTable;
import com.microsoft.azure.toolkit.intellij.monitor.view.right.TreeTable.LogDataNode;
import com.microsoft.azure.toolkit.intellij.monitor.view.right.TreeTable.LogDataModel;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class MonitorTablePanel {
    private JPanel contentPanel;
    private JBTreeTable treeTableView;

    public MonitorTablePanel() {
    }

    public void setTableModel(LogsTable tableModel) {
        if (tableModel.getAllTableCells().size() == 0) {
            return;
        }
        final List<LogDataNode> rowNodeList = new ArrayList<>();
        tableModel.getRows().forEach(logsTableRow -> {
            final List<LogDataNode> rowPairNodeList = new ArrayList<>();
            logsTableRow.getRow().forEach(tableCell -> {
                final List<String> rowPairData = new ArrayList<>();
                rowPairData.add(tableCell.getColumnName());
                rowPairData.add(tableCell.getValueAsString());
                rowPairNodeList.add(new LogDataNode(rowPairData, null));
            });
            final LogDataNode rowNode = new LogDataNode(
                    logsTableRow.getRow().stream().map(LogsTableCell::getValueAsString).toList(), rowPairNodeList);
            rowNodeList.add(rowNode);
        });
        final List<String> columnNames = tableModel.getRows().get(0).getRow().stream().map(LogsTableCell::getColumnName).toList();
        final LogDataNode rootNode = new LogDataNode(columnNames, rowNodeList);
        AzureTaskManager.getInstance().runLater(() -> treeTableView.setModel(new LogDataModel(rootNode, columnNames)));
    }

    public void setStatus(String status) {
        treeTableView.getTable().getEmptyText().setText(status);
    }

    public JPanel getContentPanel() {
        return this.contentPanel;
    }

    private void createUIComponents() {
        treeTableView = new JBTreeTable(new LogDataModel());
    }
}
