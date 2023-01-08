package com.microsoft.azure.toolkit.intellij.monitor.view.right;

import com.azure.monitor.query.models.LogsTableRow;
import com.intellij.ui.table.JBTable;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.intellij.CommonConst;

import java.util.List;

public class LogTable extends JBTable {
    private final LogTableModel logTableModel = new LogTableModel();
    private boolean loading = false;
    public LogTable() {
        super();
        this.setAutoResizeMode(JBTable.AUTO_RESIZE_OFF);
    }

    public void setModel(List<LogsTableRow> logsTableRows) {
        AzureTaskManager.getInstance().runLater(() -> {
            this.setLoading(true);
            this.logTableModel.addLogsTableRows(logsTableRows);
            this.setLoading(false);
        }, AzureTask.Modality.ANY);
    }

    public void clearModel() {
        this.logTableModel.clear();
        this.logTableModel.fireTableChanged();
    }

    private void setLoading(boolean isLoading) {
        this.loading = isLoading;
        this.setEnabled(!isLoading);
        if (isLoading) {
            this.clearModel();
            this.getEmptyText().setText(CommonConst.LOADING_TEXT);
        } else {
            this.getEmptyText().setText("Nothing");
        }
    }
}
