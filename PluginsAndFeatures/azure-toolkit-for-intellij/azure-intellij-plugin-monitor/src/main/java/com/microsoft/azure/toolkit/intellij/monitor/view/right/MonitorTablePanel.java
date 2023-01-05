package com.microsoft.azure.toolkit.intellij.monitor.view.right;

import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableCell;
import com.intellij.ui.components.JBTreeTable;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.microsoft.azure.toolkit.intellij.monitor.view.right.TreeTable.LogDataNode;
import com.microsoft.azure.toolkit.intellij.monitor.view.right.TreeTable.LogDataModel;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MonitorTablePanel {
    private JPanel contentPanel;
    private JBTreeTable treeTableView;

    public MonitorTablePanel() {
    }

    public void setTableModel(LogsTable tableModel) {
        if (tableModel.getAllTableCells().size() == 0) {
            treeTableView.setModel(new LogDataModel());
            treeTableView.repaint();
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
        AzureTaskManager.getInstance().runLater(() -> {
            treeTableView.setModel(new LogDataModel(rootNode, columnNames));
            this.setWidth();
            treeTableView.repaint();
        });
    }

    public void setStatus(String status) {
        treeTableView.getTable().getEmptyText().setText(status);
    }

    public JPanel getContentPanel() {
        return this.contentPanel;
    }

    private void setWidth() {
        final int columns = treeTableView.getTable().getColumnCount();
        final TableColumnModel columnModel = treeTableView.getTable().getColumnModel();
        final TreeTableModel myModel = treeTableView.getModel();
        int tableWidth = 0;
        final int nameWidth;
        for (int column = 0; column < columns; column++) {
            final int width = Math.max(getStringWidth(myModel.getColumnName(column)), columnModel.getColumn(column).getWidth());
            columnModel.getColumn(column).setPreferredWidth(width);
            tableWidth += width;
        }
        nameWidth = Math.max(getStringWidth(myModel.getColumnName(0)), JBUIScale.scale(150));
        treeTableView.setColumnProportion(((float)tableWidth) / (nameWidth + tableWidth) / columns);
    }

    private int getStringWidth(@NotNull String preferredString) {
        final FontMetrics fontMetrics = treeTableView.getFontMetrics(treeTableView.getFont());
        return fontMetrics.stringWidth(preferredString);
    }

    private void createUIComponents() {
        treeTableView = new JBTreeTable(new LogDataModel());
        treeTableView.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }
}
