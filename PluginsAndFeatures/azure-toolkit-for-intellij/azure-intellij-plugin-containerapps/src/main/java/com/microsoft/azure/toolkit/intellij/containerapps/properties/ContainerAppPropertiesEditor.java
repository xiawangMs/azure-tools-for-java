/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerapps.properties;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.table.JBTable;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.intellij.common.AzureHideableTitledSeparator;
import com.microsoft.azure.toolkit.intellij.common.component.TextFieldUtils;
import com.microsoft.azure.toolkit.intellij.common.properties.AzResourcePropertiesEditor;
import com.microsoft.azure.toolkit.intellij.containerapps.component.EnableComboBox;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.model.AzResourceBase;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerApp;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerAppDraft;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.Revision;
import com.microsoft.azure.toolkit.lib.containerapps.model.IngressConfig;
import com.microsoft.azure.toolkit.lib.containerapps.model.RevisionMode;
import com.microsoft.azure.toolkit.lib.containerapps.model.TransportMethod;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import rx.schedulers.Schedulers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ContainerAppPropertiesEditor extends AzResourcePropertiesEditor<ContainerApp> {
    public static final String N_A = "N/A";
    private JPanel pnlContent;
    private JPanel propertyActionPanel;
    private JButton btnRefresh;
    private AzureHideableTitledSeparator overviewSeparator;
    private JTextField resourceGroupTextField;
    private JTextField txtProvisioningStatus;
    private JTextField txtRevisionMode;
    private JTextField locationTextField;
    private JTextField txtLatestRevisionName;
    private JTextField subscriptionTextField;
    private JTextField txtContainerAppsEnvironment;
    private JTextField subscriptionIDTextField;
    private JPanel pnlNodePools;
    private JBTable revisionsTable;
    private JPanel pnlOverview;
    private JLabel lblResourceGroup;
    private JLabel lblApplicationUrl;
    private JLabel lblLocation;
    private JLabel lblSubscription;
    private JLabel lblSubscriptionId;
    private JLabel lblProvisioningStatus;
    private JLabel lblRevisionMode;
    private JLabel lblLatestRevisionName;
    private JLabel lblContainerAppsEnvironment;
    private JLabel lblIngress;
    private JLabel lblExternalAccess;
    private JLabel lblInsecureConnection;
    private JLabel lblTargetPort;
    private JTextField txtTargetPort;
    private JLabel lblTransportMethod;
    private AzureHideableTitledSeparator ingressSeparator;
    private JPanel pnlNetworking;
    private AzureHideableTitledSeparator revisionsSeparator;
    private JPanel pnlRoot;
    private JButton saveButton;
    private ActionLink resetButton;
    private EnableComboBox cbIngress;
    private EnableComboBox cbExternalAccess;
    private HyperlinkLabel linkApplicationUrl;
    private JTextField txtInsecureConnections;
    private JTextField txtTransportMethod;
    private final JTextField[] readOnlyComponents = new JTextField[]{resourceGroupTextField, locationTextField,
            subscriptionTextField, subscriptionIDTextField, txtRevisionMode, txtProvisioningStatus, txtLatestRevisionName,
            txtContainerAppsEnvironment, txtInsecureConnections, txtTransportMethod};

    private final ContainerApp containerApp;
    private final ContainerAppDraft draft;

    public ContainerAppPropertiesEditor(@Nonnull Project project, @Nonnull ContainerApp resource, @Nonnull VirtualFile virtualFile) {
        super(virtualFile, resource, project);
        this.containerApp = resource;
        this.draft = (ContainerAppDraft) containerApp.update();
        init();
        rerender();
    }

    private void init() {
        final DefaultTableModel model = new DefaultTableModel() {
            public boolean isCellEditable(int var1, int var2) {
                return false;
            }
        };
        model.addColumn("Name");
        model.addColumn("Date created");
        model.addColumn("Provision Status");
        model.addColumn("Traffic");
        model.addColumn("Active");
        this.revisionsTable.setModel(model);
        this.revisionsTable.setRowSelectionAllowed(true);
        this.revisionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.revisionsTable.getEmptyText().setText("Loading pools");
        this.revisionsTable.setBorder(BorderFactory.createEmptyBorder());

        // Overview
        this.lblResourceGroup.setLabelFor(resourceGroupTextField);
        this.lblLocation.setLabelFor(locationTextField);
        this.lblSubscription.setLabelFor(subscriptionTextField);
        this.lblSubscriptionId.setLabelFor(subscriptionIDTextField);
        this.lblProvisioningStatus.setLabelFor(txtProvisioningStatus);
        this.lblRevisionMode.setLabelFor(txtRevisionMode);
        this.lblLatestRevisionName.setLabelFor(txtLatestRevisionName);
        this.lblContainerAppsEnvironment.setLabelFor(txtContainerAppsEnvironment);
        this.lblApplicationUrl.setLabelFor(linkApplicationUrl);
        // Ingress
        this.lblIngress.setLabelFor(cbIngress);
        this.lblExternalAccess.setLabelFor(cbExternalAccess);
        this.lblInsecureConnection.setLabelFor(txtInsecureConnections);
        this.lblTargetPort.setLabelFor(txtTargetPort);
        this.lblTransportMethod.setLabelFor(txtInsecureConnections);
        TextFieldUtils.disableTextBoard(readOnlyComponents);
        TextFieldUtils.makeTextOpaque(readOnlyComponents);

        initListeners();
        this.overviewSeparator.addContentComponent(pnlOverview);
        this.revisionsSeparator.addContentComponent(pnlNodePools);
        this.ingressSeparator.addContentComponent(pnlNetworking);
    }

    private void initListeners() {
        this.resetButton.addActionListener(e -> this.reset());
        this.btnRefresh.addActionListener(e -> this.refresh());
        final AzureTaskManager tm = AzureTaskManager.getInstance();
        final String saveTitle = String.format("Saving updates of app(%s)", this.draft.getName());
        this.saveButton.addActionListener(e -> tm.runInBackground(saveTitle, this::save));
        final Runnable runnable = () -> AzureTaskManager.getInstance().runOnPooledThread(ContainerAppPropertiesEditor.this::refreshToolbar);
        this.txtTargetPort.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                runnable.run();
            }
        });
        this.cbExternalAccess.addValueChangedListener(ignore -> runnable.run());
        this.cbIngress.addValueChangedListener(ignore -> toggleIngress());
    }

    private void toggleIngress() {
        final Boolean enableIngress = Optional.ofNullable(cbIngress).map(AzureComboBox::getValue).orElse(false);
        lblExternalAccess.setVisible(enableIngress);
        cbExternalAccess.setVisible(enableIngress);
        lblInsecureConnection.setVisible(enableIngress);
        txtInsecureConnections.setVisible(enableIngress);
        lblTargetPort.setVisible(enableIngress);
        txtTargetPort.setVisible(enableIngress);
        lblTransportMethod.setVisible(enableIngress);
        txtTransportMethod.setVisible(enableIngress);
        AzureTaskManager.getInstance().runOnPooledThread(ContainerAppPropertiesEditor.this::refreshToolbar);
    }

    private void setEnabled(boolean enabled) {
        this.resetButton.setVisible(enabled);
        this.saveButton.setEnabled(enabled);
        this.txtTargetPort.setEnabled(enabled);
        this.txtTargetPort.setEditable(enabled);
        this.cbIngress.setEnabled(enabled);
        this.cbIngress.setEditable(enabled);
        this.cbExternalAccess.setEnabled(enabled);
        this.cbExternalAccess.setEditable(enabled);
    }

    private void refreshToolbar() {
        // get status from app instead of draft since status of draft is not correct
        final AzResourceBase.FormalStatus formalStatus = this.containerApp.getFormalStatus();
        final AzureTaskManager manager = AzureTaskManager.getInstance();
        manager.runLater(() -> {
            final boolean normal = formalStatus.isRunning() || formalStatus.isStopped();
            this.setEnabled(normal);
            if (normal) {
                manager.runOnPooledThread(() -> {
                    final boolean modified = this.isModified(); // checking modified is slow
                    manager.runLater(() -> {
                        this.resetButton.setVisible(modified);
                        this.saveButton.setEnabled(modified);
                    });
                });
            } else {
                this.resetButton.setVisible(false);
                this.saveButton.setEnabled(false);
            }
        });
    }

    @Override
    public boolean isModified() {
        final IngressConfig config = this.containerApp.getIngressConfig();
        final IngressConfig draftConfig = this.getConfig();
        return !Objects.equals(config, draftConfig);
    }

    @Nullable
    private IngressConfig getConfig() {
        // todo: replace with copy constructor
        final boolean enableIngress = Optional.ofNullable(cbIngress).map(AzureComboBox::getValue).orElse(false);
        if (enableIngress) {
            final IngressConfig previous = Optional.ofNullable(this.containerApp.getIngressConfig())
                    .orElseGet(() -> IngressConfig.builder().build());
            final IngressConfig result = IngressConfig.fromIngress(previous.toIngress());
            result.setEnableIngress(true);
            result.setExternal(Optional.ofNullable(cbExternalAccess.getValue()).orElse(false));
            result.setTargetPort(Integer.parseInt(txtTargetPort.getText()));
            return result;
        } else {
            return IngressConfig.fromIngress(null);
        }
    }

    private void save() {
        this.setEnabled(false);
        final IngressConfig ingressConfig = getConfig();
        final ContainerAppDraft.Config config = Optional.ofNullable(this.draft.getConfig()).orElseGet(ContainerAppDraft.Config::new);
        config.setIngressConfig(ingressConfig);
        draft.setConfig(config);
        AzureTaskManager.getInstance().runInBackground("Saving updates", this.draft::commit);
    }

    private void reset() {
        this.draft.reset();
        this.rerender();
    }

    private void refresh() {
        this.draft.reset();
        AzureTaskManager.getInstance().runInBackgroundAsObservable(new AzureTask<>("Refreshing...", containerApp::refresh))
                .subscribeOn(Schedulers.io())
                .subscribe(ignore -> rerender());
    }

    @Override
    protected void rerender() {
        AzureTaskManager.getInstance().runLater(() -> {
            this.refreshToolbar();
            this.setData(this.containerApp);
        });
    }

    private void setData(@Nonnull final ContainerApp containerApp) {
        resourceGroupTextField.setText(containerApp.getResourceGroupName());
        locationTextField.setText(Optional.ofNullable(containerApp.getRegion()).map(Region::getLabel).orElse(N_A)); // region
        final Subscription subscription = Azure.az(AzureAccount.class).account().getSubscription(containerApp.getSubscriptionId());
        subscriptionTextField.setText(subscription.getName());
        subscriptionIDTextField.setText(subscription.getId());
        txtProvisioningStatus.setText(containerApp.getProvisioningState()); // todo: replace with provision status
        txtRevisionMode.setText(Optional.ofNullable(containerApp.getRevisionMode()).map(RevisionMode::getValue).orElse(N_A));
        txtLatestRevisionName.setText(Optional.ofNullable(containerApp.getLatestRevisionName()).orElse(N_A));
        txtContainerAppsEnvironment.setText(Optional.ofNullable(containerApp.getManagedEnvironment()).map(AzResource::getName).orElse(N_A));
        final String latestRevisionFqdn = containerApp.getLatestRevisionFqdn();
        if (StringUtils.isEmpty(latestRevisionFqdn)) {
            linkApplicationUrl.setHyperlinkText(N_A);
        } else {
            linkApplicationUrl.setHyperlinkText("https://" + latestRevisionFqdn);
            linkApplicationUrl.setHyperlinkTarget("https://" + latestRevisionFqdn);
        }
        // ingress
        final IngressConfig ingressConfig = containerApp.getIngressConfig();
        cbIngress.setValue(Optional.ofNullable(ingressConfig).map(IngressConfig::isEnableIngress).orElse(false));
        cbExternalAccess.setValue(Optional.ofNullable(ingressConfig).map(IngressConfig::isExternal).orElse(false));
        txtInsecureConnections.setText(Optional.ofNullable(ingressConfig).map(IngressConfig::isAllowInsecure).map(String::valueOf).orElse("false"));
        txtTransportMethod.setText(Optional.ofNullable(ingressConfig).map(IngressConfig::getTransport).map(TransportMethod::getValue).orElse(null));
        txtTargetPort.setText(Optional.ofNullable(ingressConfig).map(IngressConfig::getTargetPort).map(String::valueOf).orElse(N_A));

        AzureTaskManager.getInstance().runInBackgroundAsObservable(new AzureTask<>("Loading node pools", () -> this.containerApp.revisions().list()))
                .subscribeOn(Schedulers.io())
                .subscribe(pools -> AzureTaskManager.getInstance().runLater(() -> fillRevisions(pools)));
    }

    private void fillRevisions(List<Revision> pools) {
        final DefaultTableModel model = (DefaultTableModel) this.revisionsTable.getModel();
        model.setRowCount(0);
        pools.forEach(i -> model.addRow(new Object[]{i.getName(), i.getCreatedTime(), i.getProvisioningState(), i.getTrafficWeight(), i.isActive()}));
        final int rows = model.getRowCount() < 5 ? 5 : pools.size();
        model.setRowCount(rows);
        this.revisionsTable.setVisibleRowCount(rows);
    }

    @Override
    public @NotNull JComponent getComponent() {
        return pnlRoot;
    }
}
