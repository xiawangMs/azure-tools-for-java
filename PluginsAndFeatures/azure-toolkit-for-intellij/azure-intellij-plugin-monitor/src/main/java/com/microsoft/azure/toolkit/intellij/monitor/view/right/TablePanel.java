package com.microsoft.azure.toolkit.intellij.monitor.view.right;

import com.azure.monitor.query.models.LogsTable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.AnActionLink;
import com.intellij.util.ui.JBUI;
import com.microsoft.azure.toolkit.intellij.common.component.HighLightedCellRenderer;
import com.microsoft.azure.toolkit.intellij.monitor.view.AzureMonitorView;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspace;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TablePanel {
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
    @Setter
    private AzureMonitorView parentView;

    public TablePanel() {
        $$$setupUI$$$(); // tell IntelliJ to call createUIComponents() here.
        this.runButton.addActionListener(e -> executeQuery());
        this.customizeTableUi();
    }


    public void setTableTab(boolean isTableTab) {
        this.isTableTab = isTableTab;
        this.setFiltersVisible(isTableTab);
    }

    public JPanel getContentPanel() {
        return this.contentPanel;
    }


    private void customizeTableUi() {
        this.logTable.setDefaultRenderer(String.class, new HighLightedCellRenderer(searchField.getTextEditor()));
        this.logTable.setFont(JBUI.Fonts.create("JetBrains Mono", 12));
        this.logTable.getTableHeader().setFont(JBUI.Fonts.create("JetBrains Mono", 12));
    }

    private void setFiltersVisible(boolean isVisible) {
        this.timeRangeComboBox.setVisible(isVisible);
        this.comboBox2.setVisible(isVisible);
        this.comboBox3.setVisible(isVisible);
        this.comboBox4.setVisible(isVisible);
    }

    private int getStringWidth(@Nonnull String preferredString, JComponent component) {
        final FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
        return fontMetrics.stringWidth(preferredString);
    }

    public void executeQuery() {
        final LogAnalyticsWorkspace selectedWorkspace = this.parentView.getSelectedWorkspace();
        if (Objects.isNull(selectedWorkspace)) {
            AzureMessager.getMessager().warning("Please select log analytics workspace first");
            return;
        }
        final String queryString = this.isTableTab ? getQueryStringFromFilters() : this.parentView.getCurrentTreeNodeText();
        loadTableModel(queryString);
    }

    private String getQueryStringFromFilters() {
        final List<String> queryParams = Arrays.asList(this.parentView.getCurrentTreeNodeText(), timeRangeComboBox.getKustoString());
        return StringUtils.join(queryParams, " | ");
    }

    private void loadTableModel(String queryString) {
        logTable.clearModel();
        logTable.setLoading(true);
        AzureTaskManager.getInstance().runInBackground("Loading Azure Monitor data", () -> {
            final LogsTable result = this.parentView.getSelectedWorkspace().executeQuery(queryString);
            AzureTaskManager.getInstance().runLater(() -> {
                if (result.getAllTableCells().size() > 0) {
                    this.logTable.setModel(result.getRows());
                }
                this.logTable.setLoading(false);
            }, AzureTask.Modality.ANY);
        });
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
