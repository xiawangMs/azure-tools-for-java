package com.microsoft.azure.toolkit.intellij.monitor.view.right.TreeTable;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;

public class LogDataModel extends DefaultTreeModel implements TreeTableModel {

    static protected String[] columnNames = { "TimeGenerated [UTC]", "Message", "SeverityLevel", "Properties" };
    static protected Class<?>[] columnTypes = { TreeTableModel.class, String.class, String.class, String.class };

    protected Object root;

    public LogDataModel(LogDataNode rootNode) {
        super(null);
        root = rootNode;
    }
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public @NlsContexts.ColumnName String getColumnName(int i) {
        return columnNames[i];
    }

    @Override
    public Class getColumnClass(int i) {
        return columnTypes[i];
    }

    @Nullable
    @Override
    public Object getValueAt(Object o, int i) {
        switch (i) {
            case 0 -> {
                return ((LogDataNode) o).getName();
            }
            case 1 -> {
                return ((LogDataNode) o).getMessage();
            }
            case 2 -> {
                return ((LogDataNode) o).getSeverityLevel();
            }
            case 3 -> {
                return ((LogDataNode) o).getProperties();
            }
            default -> {
            }
        }
        return null;
    }

    @Override
    public boolean isCellEditable(Object o, int i) {
        return true;
    }

    @Override
    public void setValueAt(Object o, Object o1, int i) {

    }

    @Override
    public void setTree(JTree jTree) {
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((LogDataNode) parent).getChildren().get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((LogDataNode) parent).getChildren().size();
    }

    @Override
    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return 0;
    }
}
