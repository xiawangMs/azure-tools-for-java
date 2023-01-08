package com.microsoft.azure.toolkit.intellij.monitor.view.right;

import com.azure.monitor.query.models.LogsTable;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.ActionLink;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;

public class TablePanel {
    private JPanel contentPanel;
    private JPanel filterPanel;
    private LogTable logTable;
    private TimeRangeComboBox timeRangeComboBox;
    private JComboBox comboBox2;
    private JComboBox comboBox3;
    private JComboBox comboBox4;
    private JButton runButton;
    private ActionLink export;
    private SearchTextField searchField;
    @Setter
    private boolean isTableTab;

    public TablePanel() {
        final Dimension runButtonSize = new Dimension(getStringWidth(runButton.getText(), runButton), runButton.getPreferredSize().height);
        this.runButton.setMaximumSize(runButtonSize);
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


    private int getStringWidth(@Nonnull String preferredString, JComponent component) {
        final FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
        return fontMetrics.stringWidth(preferredString);
    }

    private void createUIComponents() {
        this.logTable = new LogTable();
        this.timeRangeComboBox = new TimeRangeComboBox();
        this.timeRangeComboBox.setMaximumSize(new Dimension(40, timeRangeComboBox.getPreferredSize().height));
    }
}
