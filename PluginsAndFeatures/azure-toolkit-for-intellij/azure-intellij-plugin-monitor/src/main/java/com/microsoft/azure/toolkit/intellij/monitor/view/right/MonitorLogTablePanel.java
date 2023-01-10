package com.microsoft.azure.toolkit.intellij.monitor.view.right;

import com.azure.monitor.query.models.LogsTable;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.AnActionLink;
import com.intellij.util.ui.JBUI;
import com.microsoft.azure.toolkit.intellij.common.TextDocumentListenerAdapter;
import com.microsoft.azure.toolkit.intellij.common.component.HighLightedCellRenderer;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspace;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

public class MonitorLogTablePanel {
    private JPanel contentPanel;
    private JPanel filterPanel;
    private LogTable logTable;
    private TimeRangeComboBox timeRangeComboBox;
    private JComboBox comboBox2;
    private JComboBox comboBox3;
    private JComboBox comboBox4;
    private JButton runButton;
    private ActionLink exportAction;
    private SearchTextField searchField;
    private boolean isTableTab;

    public MonitorLogTablePanel() {
        $$$setupUI$$$(); // tell IntelliJ to call createUIComponents() here.
        this.customizeTableUi();
        this.runButton.setIcon(AllIcons.Actions.Execute);
    }

    public void setTableTab(boolean isTableTab) {
        this.isTableTab = isTableTab;
        this.setFiltersVisible(isTableTab);
    }

    public JPanel getContentPanel() {
        return this.contentPanel;
    }

    public String getQueryStringFromFilters(String tableName) {
        final List<String> queryParams = Arrays.asList(tableName, timeRangeComboBox.getKustoString());
        return StringUtils.join(queryParams, " | ");
    }

    public void loadTableModel(LogAnalyticsWorkspace selectedWorkspace, String queryString) {
        logTable.clearModel();
        logTable.setLoading(true);
        AzureTaskManager.getInstance().runInBackground("Loading Azure Monitor data", () -> {
            final LogsTable result = selectedWorkspace.executeQuery(queryString);
            AzureTaskManager.getInstance().runLater(() -> {
                if (result.getAllTableCells().size() > 0) {
                    this.logTable.setModel(result.getRows());
                }
                this.logTable.setLoading(false);
            }, AzureTask.Modality.ANY);
        });
    }

    public void addTableSelectionListener(ListSelectionListener selectionListener) {
        this.logTable.getColumnModel().getSelectionModel().addListSelectionListener(selectionListener);
        this.logTable.getSelectionModel().addListSelectionListener(selectionListener);
    }

    public void addRunActionListener(ActionListener listener) {
        this.runButton.addActionListener(listener);
    }

    @Nullable
    public String getSelectedCellValue() {
        return (String) this.logTable.getValueAt(this.logTable.getSelectedRow(), this.logTable.getSelectedColumn());
    }

    @Nullable
    public String getSelectedColumnName() {
        return this.logTable.getColumnName(this.logTable.getSelectedColumn());
    }

    private void customizeTableUi() {
        this.logTable.setDefaultRenderer(String.class, new HighLightedCellRenderer(searchField.getTextEditor()));
        this.logTable.setFont(JBUI.Fonts.create("JetBrains Mono", 12));
        this.logTable.getTableHeader().setFont(JBUI.Fonts.create("JetBrains Mono", 12));
        searchField.addDocumentListener((TextDocumentListenerAdapter) () -> logTable.filter(searchField.getText()));
    }

    private void setFiltersVisible(boolean isVisible) {
        this.timeRangeComboBox.setVisible(isVisible);
        this.comboBox2.setVisible(isVisible);
        this.comboBox3.setVisible(isVisible);
        this.comboBox4.setVisible(isVisible);
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
    }

    private void createUIComponents() {
        this.logTable = new LogTable();
        this.timeRangeComboBox = new TimeRangeComboBox();
        this.exportAction = new AnActionLink("Export", new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {

            }
        });
        this.exportAction.setExternalLinkIcon();
    }
}
