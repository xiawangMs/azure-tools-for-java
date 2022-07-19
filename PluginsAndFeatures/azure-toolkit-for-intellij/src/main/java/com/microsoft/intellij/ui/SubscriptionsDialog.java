/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.microsoft.azure.toolkit.intellij.common.TextDocumentListenerAdapter;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.Account;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.operation.OperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.common.utils.TailingDebouncer;
import com.microsoft.azuretools.telemetry.AppInsightsClient;
import com.microsoft.azuretools.telemetrywrapper.EventType;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import com.microsoft.intellij.ui.components.AzureDialogWrapper;
import com.microsoft.intellij.util.JTableUtils;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.microsoft.azuretools.telemetry.TelemetryConstants.ACCOUNT;
import static com.microsoft.azuretools.telemetry.TelemetryConstants.SELECT_SUBSCRIPTIONS;

public class SubscriptionsDialog extends AzureDialogWrapper implements TableModelListener {
    private static final int CHECKBOX_COLUMN = 0;
    private static final int SUBSCRIPTION_COLUMN = 2;
    private static final Logger LOGGER = Logger.getInstance(SubscriptionsDialog.class);
    private final Project project;
    private final TailingDebouncer filter;
    private JPanel contentPane;
    private JPanel panelTable;
    private SearchTextField searchBox;
    private JBTable table;

    private List<SimpleSubscription> candidates;

    public SubscriptionsDialog(@Nonnull Project project) {
        super(project, true, IdeModalityType.PROJECT);
        this.project = project;
        setModal(true);
        setTitle("Select Subscriptions");
        setOKButtonText("Select");
        init();
        this.filter = new TailingDebouncer(() -> this.updateTableView(), 300);
        table.setAutoCreateRowSorter(true);
    }

    /**
     * Open select-subscription dialog.
     */
    public void select(@Nonnull Consumer<List<String>> selectedSubscriptionsConsumer) {
        final AzureTaskManager manager = AzureTaskManager.getInstance();
        manager.runOnPooledThread(() -> {
            final List<Subscription> candidates = Azure.az(AzureAccount.class).account().getSubscriptions();
            manager.runLater(() -> {
                if (CollectionUtils.isNotEmpty(candidates)) {
                    this.setCandidates(candidates);
                    if (this.showAndGet()) {
                        final List<String> selected = this.candidates.stream().filter(SimpleSubscription::isSelected)
                            .map(SimpleSubscription::getId).collect(Collectors.toList());
                        selectedSubscriptionsConsumer.accept(selected);
                    }
                } else {
                    final int result = Messages.showOkCancelDialog(
                        "No subscription in current account", "No Subscription", "Try Azure for Free",
                        Messages.getCancelButton(), Messages.getWarningIcon());
                    if (result == Messages.OK) {
                        BrowserUtil.browse("https://azure.microsoft.com/en-us/free/");
                    }
                }
            });
        });
    }

    private void reloadSubscriptions() {
        final AzureTaskManager manager = AzureTaskManager.getInstance();
        final AzureAccount az = Azure.az(AzureAccount.class);
        if (!az.isLoggedIn()) {
            return;
        }
        manager.runOnPooledThread(() -> {
            final Account account = az.account();
            final List<Subscription> candidates = account.reloadSubscriptions();
            manager.runLater(() -> setCandidates(candidates), AzureTask.Modality.ANY);
        });
    }

    private void setCandidates(@Nonnull List<Subscription> subs) {
        this.candidates = subs.stream()
            .map(s -> new SimpleSubscription(s.getId(), s.getName(), s.isSelected()))
            .collect(Collectors.toList());
        this.updateTableView();
    }

    private synchronized void updateTableView() {
        final DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        final String k = this.searchBox.getText();
        final List<SimpleSubscription> subs = this.candidates.stream()
            .filter(s -> StringUtils.isBlank(k) || StringUtils.containsIgnoreCase(s.getName(), k) || StringUtils.containsIgnoreCase(s.getId(), k))
            .sorted(Comparator
                .comparing(SimpleSubscription::isSelected).reversed()
                .thenComparing(s -> s.getName().toLowerCase()))
            .collect(Collectors.toList());
        final boolean noneSelected = StringUtils.isBlank(k) && subs.size() > 0 && !subs.get(0).isSelected();
        for (final SimpleSubscription sd : subs) {
            model.addRow(new Object[]{noneSelected || sd.isSelected(), sd.getName(), sd});
        }
        model.fireTableDataChanged();
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == CHECKBOX_COLUMN) {
            final DefaultTableModel model = (DefaultTableModel) table.getModel();
            for (int rowIndex = e.getFirstRow(); rowIndex <= e.getLastRow(); ++rowIndex) {
                final boolean selected = (boolean) model.getValueAt(rowIndex, CHECKBOX_COLUMN);
                final SimpleSubscription sub = (SimpleSubscription) model.getValueAt(rowIndex, SUBSCRIPTION_COLUMN);
                sub.setSelected(selected);
            }
        }
    }

    private void createUIComponents() {
        contentPane = new JPanel();
        contentPane.setPreferredSize(new Dimension(460, 500));
        searchBox = new SearchTextField(false);
        searchBox.addDocumentListener((TextDocumentListenerAdapter) () -> this.filter.debounce());
        searchBox.setToolTipText("Subscription ID/name");
        final DefaultTableModel model = new SubscriptionTableModel();
        model.addColumn("Selected"); // Set the text read by JAWS
        model.addColumn("Subscription name");
        model.addColumn("Subscription ID");

        table = new JBTable(model);
        final TableColumn column = table.getColumnModel().getColumn(CHECKBOX_COLUMN);
        column.setHeaderValue(""); // Don't show title text
        column.setMinWidth(23);
        column.setMaxWidth(23);
        JTableUtils.enableBatchSelection(table, CHECKBOX_COLUMN);
        table.getTableHeader().setReorderingAllowed(false);
        model.addTableModelListener(this);
        // new TableSpeedSearch(table);
        final AnActionButton refreshAction = new AnActionButton("Refresh", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                this.setEnabled(false);
                model.setRowCount(0);
                model.fireTableDataChanged();
                table.getEmptyText().setText("Refreshing...");
                AppInsightsClient.createByType(AppInsightsClient.EventType.Subscription, "", "Refresh", null);
                final AzureString title = OperationBundle.description("account.refresh_subscriptions");
                final AzureTask<Void> task = new AzureTask<>(project, title, true, () -> {
                    try {
                        SubscriptionsDialog.this.reloadSubscriptions();
                    } finally {
                        this.setEnabled(true);
                    }
                }, AzureTask.Modality.ANY);
                AzureTaskManager.getInstance().runInBackground(task);
            }
        };
        refreshAction.registerCustomShortcutSet(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK, contentPane);
        final ToolbarDecorator tableToolbarDecorator =
            ToolbarDecorator.createDecorator(table)
                .disableUpDownActions()
                .addExtraActions(refreshAction);

        panelTable = tableToolbarDecorator.createPanel();
    }

    @Override
    protected void doOKAction() {
        final long selected = this.candidates.stream().filter(SimpleSubscription::isSelected).count();
        if (this.candidates.size() > 0 && selected == 0) {
            DefaultLoader.getUIHelper().showMessageDialog(
                contentPane, "Please select at least one subscription",
                "Subscription dialog info", Messages.getInformationIcon());
            return;
        }

        final Map<String, String> properties = new HashMap<>();
        properties.put("subsCount", String.valueOf(this.candidates.size()));
        properties.put("selectedSubsCount", String.valueOf(selected));
        EventUtil.logEvent(EventType.info, ACCOUNT, SELECT_SUBSCRIPTIONS, null);
        super.doOKAction();
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{this.getOKAction(), this.getCancelAction()};
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "SubscriptionsDialog";
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return this.table;
    }

    private static class SubscriptionTableModel extends DefaultTableModel {
        @Override
        public boolean isCellEditable(int row, int col) {
            return col == CHECKBOX_COLUMN;
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return col == CHECKBOX_COLUMN ? Boolean.class : super.getColumnClass(col);
        }
    }

    @Setter
    @Getter
    @AllArgsConstructor
    private static class SimpleSubscription {
        private final String id;
        private final String name;
        private boolean selected;

        @Override
        public String toString() {
            return this.id;
        }
    }
}
