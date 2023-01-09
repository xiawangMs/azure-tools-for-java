package com.microsoft.azure.toolkit.intellij.monitor.view.right;

import com.azure.monitor.query.models.LogsColumnType;
import com.azure.monitor.query.models.LogsTableCell;
import com.azure.monitor.query.models.LogsTableRow;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import org.jetbrains.annotations.Nls;

import javax.annotation.Nullable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.azure.toolkit.intellij.common.AzureBundle.message;

public class LogTableModel implements TableModel {
    private final List<String> columnNames = new ArrayList<>();
    private final List<LogsTableRow> logsTableRows = new ArrayList<>();
    private final List<TableModelListener> tableModelListenerList = new ArrayList<>();


    public LogTableModel() {
    }

    public LogTableModel(List<LogsTableRow> logsTableRows ) {
        this.columnNames.addAll(logsTableRows.get(0).getRow().stream().map(LogsTableCell::getColumnName).toList());
        this.logsTableRows.addAll(logsTableRows);
    }

    @Override
    public int getRowCount() {
        return this.logsTableRows.size();
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.size();
    }

    @Nls
    @Override
    public String getColumnName(int columnIndex) {
        return this.columnNames.get(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return !isRowInvalid(rowIndex);
    }

    @Override
    @Nullable
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (isRowInvalid(rowIndex)) {
            return null;
        }
        return this.logsTableRows.get(rowIndex).getRow().get(columnIndex).getValueAsString();
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (value != null && !(value instanceof String)) {
            throw new IllegalArgumentException(message("function.appSettings.validate.illegalType"));
        }
        while (rowIndex >= this.logsTableRows.size()) {
            final LogsTableCell newCell = new LogsTableCell(columnNames.get(columnIndex), LogsColumnType.STRING, columnIndex, rowIndex, value);
            final LogsTableRow newRow = new LogsTableRow(rowIndex, List.of(newCell));
            this.logsTableRows.add(newRow);
        }
        this.logsTableRows.get(rowIndex).getRow().add(columnIndex, new LogsTableCell(columnNames.get(columnIndex), LogsColumnType.STRING, columnIndex, rowIndex, value));
        fireTableChanged();
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        tableModelListenerList.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        tableModelListenerList.remove(l);
    }

    public void clear() {
        this.columnNames.clear();
        this.logsTableRows.clear();
        fireTableChanged();
    }

    private void fireTableChanged() {
        tableModelListenerList.forEach(listener ->
                AzureTaskManager.getInstance().runLater(() -> listener.tableChanged(new TableModelEvent(this))));
    }

    private boolean isRowInvalid(int row) {
        return row < 0 || row >= this.logsTableRows.size();
    }
}
