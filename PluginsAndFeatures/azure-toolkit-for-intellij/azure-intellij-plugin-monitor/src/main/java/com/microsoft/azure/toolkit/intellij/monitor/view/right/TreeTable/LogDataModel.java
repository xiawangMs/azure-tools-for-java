package com.microsoft.azure.toolkit.intellij.monitor.view.right.TreeTable;

import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.List;

public class LogDataModel extends DefaultTreeModel implements TreeTableModel {
    private final List<String> columnNames;
    protected Object root;

    public LogDataModel() {
        super(null);
        this.columnNames = new ArrayList<>();
    }

    public LogDataModel(LogDataNode rootNode, List<String> columnNames) {
        super(null);
        this.root = rootNode;
        this.columnNames = columnNames;
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public String getColumnName(int i) {
        return columnNames.get(i);
    }

    @Override
    public Class getColumnClass(int i) {
        if (i == 0) {
            return TreeTableModel.class;
        }
        return String.class;
    }

    @Nullable
    @Override
    public Object getValueAt(Object o, int i) {
        if (i >= ((LogDataNode) o).getColumnValues().size()) {
            return StringUtils.EMPTY;
        }
        return ((LogDataNode) o).getColumnValues().get(i);
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
