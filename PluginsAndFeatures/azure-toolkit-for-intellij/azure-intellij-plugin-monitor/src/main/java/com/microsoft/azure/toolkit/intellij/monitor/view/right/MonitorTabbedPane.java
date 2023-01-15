package com.microsoft.azure.toolkit.intellij.monitor.view.right;

import com.intellij.icons.AllIcons;
import com.microsoft.azure.toolkit.intellij.monitor.view.AzureMonitorView;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MonitorTabbedPane {
    @Getter
    private JPanel contentPanel;
    private JTabbedPane closeableTabbedPane;
    @Setter
    private boolean isTableTab;
    private AzureMonitorView parentView;
    private final List<String> openedTabs = new ArrayList<>();

    public MonitorTabbedPane() {

    }

    public void setParentView(AzureMonitorView parentView) {
        this.parentView = parentView;
        this.parentView.getMonitorTreePanel().addTreeSelectionListener(e -> Optional.ofNullable(e.getNewLeadSelectionPath())
            .ifPresent(it -> {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) it.getLastPathComponent();
                if (Objects.nonNull(node) && node.isLeaf()) {
                    selectTab(node.toString());
                }
            }));
    }

    private void selectTab(String tabName) {
        if (!openedTabs.contains(tabName)) {
            final MonitorSingleTab singleTab = new MonitorSingleTab(this.isTableTab, tabName, this.parentView);
            this.closeableTabbedPane.addTab(tabName, AllIcons.Actions.Close, singleTab.getSplitter());
            final JLabel label = new JLabel(tabName);
            label.setHorizontalTextPosition(SwingConstants.LEFT);
            label.setIcon(AllIcons.Actions.Close);
            this.closeableTabbedPane.setTabComponentAt(this.closeableTabbedPane.getTabCount() - 1, label);
            this.openedTabs.add(tabName);
        }
        final int toSelectIndex = this.closeableTabbedPane.indexOfTab(tabName);
        this.closeableTabbedPane.setSelectedIndex(toSelectIndex);
    }
}
