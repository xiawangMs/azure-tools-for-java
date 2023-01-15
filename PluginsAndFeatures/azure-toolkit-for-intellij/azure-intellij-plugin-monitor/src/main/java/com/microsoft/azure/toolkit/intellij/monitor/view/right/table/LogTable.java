package com.microsoft.azure.toolkit.intellij.monitor.view.right.table;

import com.azure.monitor.query.models.LogsTableRow;
import com.intellij.ui.table.JBTable;
import com.microsoft.intellij.CommonConst;
import lombok.Getter;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.util.List;

public class LogTable extends JBTable {
    @Getter
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

    @Override
    @Nullable
    public String getColumnName(int column) {
        return isValidColumnIndex(column) ? super.getColumnName(column) : null;
    }

    @Nullable
    @Override
    public Object getValueAt(int row, int column) {
        return isValidRowIndex(row) && isValidColumnIndex(column) ? super.getValueAt(row, column) : null;
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

    public void filter(String stringToFilter) {
        final RowFilter<TableModel, Object> rf;
        try {
            rf = RowFilter.regexFilter("(?i)" + stringToFilter);
        } catch (final java.util.regex.PatternSyntaxException e) {
            return;
        }
        if (this.getRowSorter() instanceof TableRowSorter<? extends TableModel>) {
            ((TableRowSorter<? extends TableModel>) this.getRowSorter()).setRowFilter(rf);
        }
    }

    private boolean isValidColumnIndex(int columnIndex) {
        return columnIndex >=0 && columnIndex < logTableModel.getColumnCount();
    }

    private boolean isValidRowIndex(int rowIndex) {
        return rowIndex >=0 && rowIndex < logTableModel.getRowCount();
    }
}
