package com.microsoft.azure.toolkit.intellij.monitor.view.right;

import com.azure.monitor.query.models.LogsTable;
import lombok.Setter;

import javax.swing.*;

public class TablePanel {
    private JPanel contentPanel;
    private JPanel filterPanel;
    private LogTable logTable;
    @Setter
    private boolean isTableTab;

    public TablePanel() {
    }

    public void setTableModel(LogsTable tableModel) {
        if (tableModel.getAllTableCells().size() == 0) {
            this.logTable.clearModel();
            return;
        }
        this.logTable.setModel(tableModel.getRows());
    }

    public JPanel getContentPanel() {
        return this.contentPanel;
    }

    private void createUIComponents() {
        this.logTable = new LogTable();
    }
}
