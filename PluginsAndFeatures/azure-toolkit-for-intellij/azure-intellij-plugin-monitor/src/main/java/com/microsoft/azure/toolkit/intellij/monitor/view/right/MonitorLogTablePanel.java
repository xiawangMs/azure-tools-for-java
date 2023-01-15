package com.microsoft.azure.toolkit.intellij.monitor.view.right;

import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableCell;
import com.azure.monitor.query.models.LogsTableRow;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.AnActionLink;
import com.intellij.util.ui.JBUI;
import com.microsoft.azure.toolkit.intellij.common.TextDocumentListenerAdapter;
import com.microsoft.azure.toolkit.intellij.common.component.HighLightedCellRenderer;
import com.microsoft.azure.toolkit.intellij.monitor.view.right.filter.ResourceComboBox;
import com.microsoft.azure.toolkit.intellij.monitor.view.right.filter.TimeRangeComboBox;
import com.microsoft.azure.toolkit.intellij.monitor.view.right.table.LogTable;
import com.microsoft.azure.toolkit.intellij.monitor.view.right.table.LogTableModel;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspace;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.microsoft.azure.toolkit.intellij.common.AzureBundle.message;

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
    private final static String RESULT_CSV_FILE = "result.csv";

    public MonitorLogTablePanel() {
        $$$setupUI$$$(); // tell IntelliJ to call createUIComponents() here.
        this.customizeTableUi();
        this.hideFilters();
        this.runButton.setIcon(AllIcons.Actions.Execute);
    }

    public JPanel getContentPanel() {
        return this.contentPanel;
    }

    public String getQueryStringFromFilters(String tableName) {
        final List<String> queryParams = new ArrayList<>(Arrays.asList(tableName, timeRangeComboBox.getKustoString()));
        if (resourceComboBox.isVisible() && StringUtils.isNotBlank(resourceComboBox.getKustoString())) {
            queryParams.add(resourceComboBox.getKustoString());
        }
        if (levelComboBox.isVisible() && StringUtils.isNotBlank(levelComboBox.getKustoString())) {
            queryParams.add(levelComboBox.getKustoString());
        }
        final String rowNumberLimitation = String.format("take %s", Azure.az().config().getMonitorQueryRowNumber());
        queryParams.add(rowNumberLimitation);
        return StringUtils.join(queryParams, " | ");
    }

    public void loadTableModel(LogAnalyticsWorkspace selectedWorkspace, String queryString) {
        logTable.clearModel();
        logTable.setLoading(true);
        runButton.setEnabled(false);
        AzureTaskManager.getInstance().runInBackground("Loading Azure Monitor data", () -> {
            try {
                final LogsTable result = selectedWorkspace.executeQuery(queryString);
                AzureTaskManager.getInstance().runLater(() -> {
                    if (Objects.isNull(result)) {
                        return;
                    }
                    if (result.getAllTableCells().size() > 0) {
                        this.logTable.setModel(result.getRows());
                    }
                }, AzureTask.Modality.ANY);
            } catch (final Exception e) {
                throw new AzureToolkitRuntimeException(e);
            } finally {
                AzureTaskManager.getInstance().runLater(() -> {
                    logTable.setLoading(false);
                    runButton.setEnabled(true);
                }, AzureTask.Modality.ANY);
            }
        });
    }

    public void loadFilters(LogAnalyticsWorkspace selectedWorkspace, String tableName) {
        statusPanel.setVisible(true);
        filterPanel.setVisible(false);
        timeRangeComboBox.setVisible(true);
        runButton.setVisible(true);
        AzureTaskManager.getInstance().runInBackground("Loading filters", () -> {
            try {
                final List<String> tableColumns = queryColumnNameList(selectedWorkspace, tableName);
                final CountDownLatch countDownLatch = new CountDownLatch(2);
                loadFilters(selectedWorkspace, tableName, RESOURCE_COMBOBOX_COLUMN_NAMES, tableColumns, resourceComboBox, countDownLatch);
                loadFilters(selectedWorkspace, tableName, LEVEL_COMBOBOX_COLUMN, tableColumns, levelComboBox, countDownLatch);
                countDownLatch.await();
            } catch (final Exception e) {
                throw new AzureToolkitRuntimeException(e);
            } finally {
                AzureTaskManager.getInstance().runLater(() -> {
                    statusPanel.setVisible(false);
                    filterPanel.setVisible(true);
                }, AzureTask.Modality.ANY);
            }
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

    private void updateComboboxItems(ResourceComboBox comboBox, Pair<String, List<String>> pair) {
        if (pair.getValue().size() <=0 ) {
            comboBox.setVisible(false);
            return;
        }
        comboBox.setVisible(true);
        comboBox.setItemsLoader(() -> {
            final List<String> result = new ArrayList<>();
            result.add(ResourceComboBox.ALL);
            result.addAll(pair.getValue());
            return result;
        });
        comboBox.setColumnName(pair.getKey());
        comboBox.setValue(ResourceComboBox.ALL);
    }

    private List<String> queryColumnNameList(LogAnalyticsWorkspace selectedWorkspace, String tableName) {
        return Optional.ofNullable(selectedWorkspace.executeQuery(String.format("%s | take 1", tableName)))
                .map(LogsTable::getAllTableCells).orElse(new ArrayList<>())
                .stream().map(LogsTableCell::getColumnName).toList();
    }

    private Pair<String, List<String>> queryCellValueList(LogAnalyticsWorkspace selectedWorkspace, String tableName,
                                                          String[] columnNames, List<String> tableColumns) {
        for (final String columnName: columnNames) {
            if (tableColumns.contains(columnName)) {
                final String queryResource = String.format("%s | distinct %s | project %s", tableName, columnName, columnName);
                return Pair.of(columnName, Optional.ofNullable(selectedWorkspace.executeQuery(queryResource))
                        .map(LogsTable::getAllTableCells).orElse(new ArrayList<>())
                        .stream().map(LogsTableCell::getValueAsString).toList());
            }
        }
        return Pair.of(StringUtils.EMPTY, new ArrayList<>());
    }

    private void loadFilters(LogAnalyticsWorkspace selectedWorkspace, String tableName,
                             String[] columnNames, List<String> tableColumns,
                             ResourceComboBox comboBox, CountDownLatch latch) {
        AzureTaskManager.getInstance().runInBackground("Loading filters", () -> {
            try {
                final Pair<String, List<String>> valueListPair = queryCellValueList(selectedWorkspace, tableName, columnNames, tableColumns);
                AzureTaskManager.getInstance().runLater(() -> updateComboboxItems(comboBox, valueListPair), AzureTask.Modality.ANY);
            } catch (final Exception e) {
                throw new AzureToolkitRuntimeException(e);
            } finally {
                latch.countDown();
            }
        });
    }

    private void hideFilters() {
        this.timeRangeComboBox.setVisible(false);
        this.resourceComboBox.setVisible(false);
        this.levelComboBox.setVisible(false);
        this.runButton.setVisible(false);
    }

    @AzureOperation(name = "user/monitor.export_query_result")
    private void exportQueryResult() {
        final FileSaverDescriptor fileDescriptor = new FileSaverDescriptor(message("azure.monitor.export.description"), "");
        final FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(fileDescriptor, (Project) null);
        final VirtualFile userHome = LocalFileSystem.getInstance().findFileByPath(System.getProperty("user.home"));
        final VirtualFileWrapper fileWrapper = dialog.save(userHome, RESULT_CSV_FILE);
        Optional.ofNullable(fileWrapper).map(VirtualFileWrapper::getFile).ifPresent(it ->
                AzureTaskManager.getInstance().runInBackground("Export query data", () -> exportTableData(it, logTable.getLogTableModel())));
    }

    private void exportTableData(File target, LogTableModel tableModel) {
        try {
            if (target == null) {
                return;
            }
            final File parentFolder = target.getParentFile();
            if (!parentFolder.exists()) {
                parentFolder.mkdirs();
            }
            if (!target.exists()) {
                target.createNewFile();
            }
            final CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(target),
                    CSVFormat.Builder.create().setHeader(tableModel.getColumnNames().toArray(new String[0])).build());
            for (final LogsTableRow row : tableModel.getLogsTableRows()) {
                csvPrinter.printRecord(row.getRow().stream().map(LogsTableCell::getValueAsString).toList());
            }
            csvPrinter.close();
            AzureMessager.getMessager().info(message("azure.monitor.export.succeed.message", target.getAbsolutePath()),
                    message("azure.monitor.export.succeed.title"));
        } catch (final Exception e) {
            throw new AzureToolkitRuntimeException(e);
        }
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
                exportQueryResult();
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
    }
}
