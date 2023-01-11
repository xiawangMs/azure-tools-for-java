package com.microsoft.azure.toolkit.intellij.monitor.view.right;

import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableCell;
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
import kotlin.Pair;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MonitorLogTablePanel {
    private JPanel contentPanel;
    private JPanel filterPanel;
    private LogTable logTable;
    private TimeRangeComboBox timeRangeComboBox;
    private ResourceComboBox resourceComboBox;
    private ResourceComboBox levelComboBox;
    private JButton runButton;
    private ActionLink exportAction;
    private SearchTextField searchField;
    private JPanel statusPanel;
    private JLabel statusLabel;
    private final static String[] RESOURCE_COMBOBOX_COLUMN_NAMES = {"_ResourceId", "ResourceId"};
    private final static String[] LEVEL_COMBOBOX_COLUMN = {"Level"};

    public MonitorLogTablePanel() {
        $$$setupUI$$$(); // tell IntelliJ to call createUIComponents() here.
        this.customizeTableUi();
        this.runButton.setIcon(AllIcons.Actions.Execute);
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
        runButton.setEnabled(false);
        AzureTaskManager.getInstance().runInBackground("Loading Azure Monitor data", () -> {
            final LogsTable result = selectedWorkspace.executeQuery(queryString);
            AzureTaskManager.getInstance().runLater(() -> {
                if (result.getAllTableCells().size() > 0) {
                    this.logTable.setModel(result.getRows());
                }
                this.logTable.setLoading(false);
                runButton.setEnabled(true);
            }, AzureTask.Modality.ANY);
        });
    }

    public void loadFilters(LogAnalyticsWorkspace selectedWorkspace, String tableName) {
        statusPanel.setVisible(true);
        timeRangeComboBox.setVisible(true);
        AzureTaskManager.getInstance().runInBackground("Loading filters", () -> {
            final List<String> tableColumns = queryColumnNameList(selectedWorkspace, tableName);
            final Pair<String, List<String>> resourceListPair = queryCellValueList(selectedWorkspace, tableName, RESOURCE_COMBOBOX_COLUMN_NAMES, tableColumns);
            final Pair<String, List<String>> levelListPair = queryCellValueList(selectedWorkspace, tableName, LEVEL_COMBOBOX_COLUMN, tableColumns);
            AzureTaskManager.getInstance().runLater(() -> {
                if (resourceListPair.component2().size() > 0) {
                    resourceComboBox.setVisible(true);
                    resourceComboBox.setItemsLoader(resourceListPair::component2);
                    resourceComboBox.setColumnName(resourceListPair.component1());
                } else {
                    resourceComboBox.setVisible(false);
                }
                if (levelListPair.component2().size() > 0) {
                    levelComboBox.setVisible(true);
                    levelComboBox.setItemsLoader(levelListPair::component2);
                    levelComboBox.setColumnName(levelListPair.component1());
                } else {
                    levelComboBox.setVisible(false);
                }
                statusPanel.setVisible(false);
                filterPanel.setVisible(true);
            });
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

    private List<String> queryColumnNameList(LogAnalyticsWorkspace selectedWorkspace, String tableName) {
        return selectedWorkspace.executeQuery(String.format("%s | take 1", tableName))
                .getAllTableCells().stream().map(LogsTableCell::getColumnName).toList();
    }

    private Pair<String, List<String>> queryCellValueList(LogAnalyticsWorkspace selectedWorkspace, String tableName,
                                                          String[] columnNames, List<String> tableColumns) {
        for (final String columnName: columnNames) {
            if (tableColumns.contains(columnName)) {
                final String queryResource = String.format("%s | distinct %s | project %s", tableName, columnName, columnName);
                return new Pair<>(columnName, selectedWorkspace.executeQuery(queryResource).getAllTableCells().stream().map(LogsTableCell::getValueAsString).toList());
            }
        }
        return new Pair<>(StringUtils.EMPTY, new ArrayList<>());
    }

    private void hideFilters() {
        this.timeRangeComboBox.setVisible(false);
        this.resourceComboBox.setVisible(false);
        this.levelComboBox.setVisible(false);
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
        this.resourceComboBox = new ResourceComboBox();
        this.levelComboBox = new ResourceComboBox() {
            @Override
            protected String getItemText(Object item) {
                return Objects.nonNull(item) ? item.toString() : StringUtils.EMPTY;
            }
        };
        this.hideFilters();
    }
}
