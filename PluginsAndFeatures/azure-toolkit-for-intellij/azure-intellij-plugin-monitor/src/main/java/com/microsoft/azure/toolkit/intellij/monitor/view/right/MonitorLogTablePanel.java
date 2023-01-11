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
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspace;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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
        final List<String> queryParams = Stream.of(tableName, timeRangeComboBox.getKustoString(),
                resourceComboBox.getKustoString(), levelComboBox.getKustoString())
                .filter(s -> !s.isBlank()).toList();
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
        filterPanel.setVisible(false);
        timeRangeComboBox.setVisible(true);
        AzureTaskManager.getInstance().runInBackground("Loading filters", () -> {
            final List<String> tableColumns = queryColumnNameList(selectedWorkspace, tableName);
            final Pair<String, List<String>> resourceListPair = queryCellValueList(selectedWorkspace, tableName, RESOURCE_COMBOBOX_COLUMN_NAMES, tableColumns);
            final Pair<String, List<String>> levelListPair = queryCellValueList(selectedWorkspace, tableName, LEVEL_COMBOBOX_COLUMN, tableColumns);
            AzureTaskManager.getInstance().runLater(() -> {
                updateComboboxItems(resourceComboBox, resourceListPair);
                updateComboboxItems(levelComboBox, levelListPair);
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
        return selectedWorkspace.executeQuery(String.format("%s | take 1", tableName))
                .getAllTableCells().stream().map(LogsTableCell::getColumnName).toList();
    }

    private Pair<String, List<String>> queryCellValueList(LogAnalyticsWorkspace selectedWorkspace, String tableName,
                                                          String[] columnNames, List<String> tableColumns) {
        for (final String columnName: columnNames) {
            if (tableColumns.contains(columnName)) {
                final String queryResource = String.format("%s | distinct %s | project %s", tableName, columnName, columnName);
                return Pair.of(columnName, selectedWorkspace.executeQuery(queryResource).getAllTableCells().stream().map(LogsTableCell::getValueAsString).toList());
            }
        }
        return Pair.of(StringUtils.EMPTY, new ArrayList<>());
    }

    private void hideFilters() {
        this.timeRangeComboBox.setVisible(false);
        this.resourceComboBox.setVisible(false);
        this.levelComboBox.setVisible(false);
    }

    private void exportQueryResult() {
        final FileSaverDescriptor fileDescriptor = new FileSaverDescriptor(message("azure.monitor.export.description"), "");
        final FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(fileDescriptor, (Project) null);
        final VirtualFile userHome = LocalFileSystem.getInstance().findFileByPath(System.getProperty("user.home"));
        final VirtualFileWrapper fileWrapper = dialog.save(userHome, RESULT_CSV_FILE);
        final File file = Optional.ofNullable(fileWrapper).map(VirtualFileWrapper::getFile).orElse(null);
        if (file != null) {
            AzureTaskManager.getInstance().runInBackground("Export query data", () -> exportTableData(file, logTable.getLogTableModel()));
        }
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
            final FileWriter writer = new FileWriter(target);
            writer.write(StringUtils.join(tableModel.getColumnNames(), ","));
            for (final LogsTableRow row : tableModel.getLogsTableRows()) {
                writer.write(StringUtils.join(row.getRow().stream().map(LogsTableCell::getValueAsString).toList(), ","));
            }
            writer.close();
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
