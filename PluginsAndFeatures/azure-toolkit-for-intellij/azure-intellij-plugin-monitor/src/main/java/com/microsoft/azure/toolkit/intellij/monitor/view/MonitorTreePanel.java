package com.microsoft.azure.toolkit.intellij.monitor.view;

import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.ui.RelativeFont;
import com.intellij.ui.render.RenderingUtil;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.ui.treeStructure.Tree;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspaceConfig;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

public class MonitorTreePanel {
    private JTabbedPane tabbedPane;
    private JPanel contentPanel;
    private JPanel tablePanel;
    private JPanel queryPanel;
    private Tree tableTree;
    private Tree queryTree;
    private DefaultTreeModel tableModel;
    private DefaultTreeModel queryModal;
    private final String[] resourceTypeList = new String[]{"Application Insights", "App Service", "Azure Spring Cloud", "Virtual Machines"};

    public synchronized void refresh() {
        LogAnalyticsWorkspaceConfig config;
    }

    private void loadData() {

    }

    private Tree initTree(DefaultTreeModel treeModel) {
        final SimpleTree tree = new SimpleTree(treeModel);
        tree.putClientProperty(RenderingUtil.ALWAYS_PAINT_SELECTION_AS_FOCUSED, true);
        tree.setCellRenderer(new NodeRenderer());
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        TreeUtil.installActions(tree);
        RelativeFont.BOLD.install(tree);
        return tree;
    }

    private void createUIComponents() {
        this.tableModel = new DefaultTreeModel(new DefaultMutableTreeNode("Azure Monitor Tables"));
        this.tableTree = this.initTree(this.tableModel);
        this.queryModal = new DefaultTreeModel(new DefaultMutableTreeNode("Azure Monitor Queries"));
        this.queryTree = this.initTree(this.queryModal);
    }
}
