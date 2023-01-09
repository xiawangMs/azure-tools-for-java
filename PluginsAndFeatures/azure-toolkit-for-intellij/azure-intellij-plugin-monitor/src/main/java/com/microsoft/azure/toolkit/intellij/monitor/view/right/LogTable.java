package com.microsoft.azure.toolkit.intellij.monitor.view.right;

import com.azure.monitor.query.models.LogsTableRow;
import com.intellij.ui.table.JBTable;
import com.microsoft.intellij.CommonConst;

import javax.swing.*;
import java.util.List;

public class LogTable extends JBTable {
    private LogTableModel logTableModel = new LogTableModel();
    public LogTable() {
        super();
        this.setModel(logTableModel);
        this.setCellSelectionEnabled(true);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setAutoscrolls(true);
        this.setAutoResizeMode(JBTable.AUTO_RESIZE_OFF);
        this.setPreferredScrollableViewportSize(null);
        this.setAutoCreateRowSorter(true);
    }

    public void setModel(List<LogsTableRow> logsTableRows) {
        logTableModel = new LogTableModel(logsTableRows);
        this.setModel(logTableModel);
    }

    public void clearModel() {
        this.logTableModel.clear();
        this.setRowSorter(null);
    }

    public void setLoading(boolean isLoading) {
        this.setEnabled(!isLoading);
        if (isLoading) {
            this.getEmptyText().setText(CommonConst.LOADING_TEXT);
        } else {
            this.getEmptyText().setText("No results found");
        }
    }
}
