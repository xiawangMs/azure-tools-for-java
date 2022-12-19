package com.microsoft.azure.toolkit.intellij.monitor.view.left;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.ui.RelativeFont;
import com.intellij.ui.render.RenderingUtil;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.ui.treeStructure.Tree;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class MonitorTreePanel {
    private JTabbedPane tabbedPane;
    private JPanel contentPanel;
    private JPanel tablePanel;
    private JPanel queryPanel;
    private Tree tableTree;
    private Tree queryTree;
    private DefaultTreeModel tableModel;
    private DefaultTreeModel queryModal;

    public synchronized void refresh() {
        loadQueryData();
        loadTableData();
    }

    private void loadQueryData() {
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.queryModal.getRoot();
        root.removeAllChildren();
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("table-query-tree/QueryTree.json")) {
            final Map<String, List<QueryData>> queryTreeData = new JsonMapper()
                    .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(inputStream, new TypeReference<>() {});
            queryTreeData.forEach((key,value) -> {
                final DefaultMutableTreeNode resourceNode = new DefaultMutableTreeNode(key);
                value.forEach(queryData -> {
                    final DefaultMutableTreeNode queryNode = new DefaultMutableTreeNode(queryData.getDisplayName());
                    resourceNode.add(queryNode);
                });
                root.add(resourceNode);
            });
        } catch (final Exception ignored) {
        }
        this.queryModal.reload();
        TreeUtil.expandAll(this.queryTree);
    }

    private void loadTableData() {
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.tableModel.getRoot();
        root.removeAllChildren();
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("table-query-tree/TableTree.json")) {
            final Map<String, List<String>> tableTreeData = new JsonMapper()
                    .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(inputStream, new TypeReference<>() {});
            tableTreeData.forEach((key,value) -> {
                final DefaultMutableTreeNode resourceNode = new DefaultMutableTreeNode(key);
                value.forEach(tableName -> {
                    final DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(tableName);
                    resourceNode.add(tableNode);
                });
                root.add(resourceNode);
            });
        } catch (final Exception ignored) {
        }
        this.tableModel.reload();
        TreeUtil.expandAll(this.tableTree);
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

    @Getter
    @Setter
    public static class QueryData {
        private String displayName;
        private String queryString;
    }
}
