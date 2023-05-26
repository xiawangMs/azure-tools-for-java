/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.uiDesigner.core.GridConstraints;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox.ItemReference;
import com.microsoft.azure.toolkit.intellij.common.AzureDialog;
import com.microsoft.azure.toolkit.intellij.common.AzureFormJPanel;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.AzureModule;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.ConnectionManager;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.ResourceManager;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.microsoft.azure.toolkit.intellij.connector.ResourceDefinition.CONSUMER;
import static com.microsoft.azure.toolkit.intellij.connector.ResourceDefinition.RESOURCE;

public class ConnectorDialog extends AzureDialog<Connection<?, ?>> implements AzureForm<Connection<?, ?>> {
    private final Project project;
    private JPanel contentPane;
    @SuppressWarnings("rawtypes")
    private AzureFormJPanel consumerPanel;
    @SuppressWarnings("rawtypes")
    private AzureFormJPanel resourcePanel;
    private AzureComboBox<ResourceDefinition<?>> consumerTypeSelector;
    private AzureComboBox<ResourceDefinition<?>> resourceTypeSelector;
    private JPanel consumerPanelContainer;
    private JPanel resourcePanelContainer;
    private JBLabel consumerTypeLabel;
    private JBLabel resourceTypeLabel;
    private TitledSeparator resourceTitle;
    private TitledSeparator consumerTitle;
    protected JTextField envPrefixTextField;
    private ResourceDefinition<?> resourceDefinition;
    private ResourceDefinition<?> consumerDefinition;

    private Connection<?,?> connection;

    @Getter
    private final String dialogTitle = "Azure Resource Connector";

    public ConnectorDialog(Project project) {
        super(project);
        this.project = project;
        $$$setupUI$$$();
        this.init();
    }

    @Override
    protected void init() {
        super.init();
        this.setOkActionListener(this::saveConnection);
        this.consumerTypeSelector.addItemListener(this::onResourceOrConsumerTypeChanged);
        this.resourceTypeSelector.addItemListener(this::onResourceOrConsumerTypeChanged);
        final var resourceDefinitions = ResourceManager.getDefinitions(RESOURCE);
        final var consumerDefinitions = ResourceManager.getDefinitions(CONSUMER);
        if (resourceDefinitions.size() == 1) {
            this.fixResourceType(resourceDefinitions.get(0));
        }
        if (consumerDefinitions.size() == 1) {
            this.fixConsumerType(consumerDefinitions.get(0));
        }
    }

    protected void onResourceOrConsumerTypeChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED && !tryOpenCustomDialog()) {
            if (Objects.equals(e.getSource(), this.consumerTypeSelector)) {
                this.setConsumerDefinition(this.consumerTypeSelector.getValue());
            } else {
                this.setResourceDefinition(this.resourceTypeSelector.getValue());
            }
        }
        this.contentPane.revalidate();
        this.contentPane.repaint();
        this.pack();
        this.centerRelativeToParent();
    }

    private boolean tryOpenCustomDialog() {
        final ResourceDefinition<?> cd = this.consumerTypeSelector.getValue();
        final ResourceDefinition<?> rd = this.resourceTypeSelector.getValue();
        if (Objects.nonNull(cd) && Objects.nonNull(rd)) {
            final ConnectionDefinition<?, ?> definition = ConnectionManager.getDefinitionOrDefault(rd, cd);
            final AzureDialog<? extends Connection<?, ?>> dialog = definition.getConnectorDialog();
            if (Objects.nonNull(dialog)) {
                dialog.show();
                return true;
            }
        }
        return false;
    }

    protected void saveConnection(Connection<?, ?> connection) {
        if (connection == null) {
            return;
        }
        this.close(0);
        final Resource<?> resource = connection.getResource();
        final Resource<?> consumer = connection.getConsumer();
        if (connection.validate(this.project)) {
            saveConnectionToDotAzure(connection, consumer);
            final String message = String.format("The connection between %s and %s has been successfully created/updated.", resource.getName(), consumer.getName());
            AzureMessager.getMessager().success(message);
        }
    }

    @AzureOperation(
        name = "user/connector.create_or_update_connection.consumer|resource",
        params = {"connection.getConsumer().getName()", "connection.getResource().getName()"}
    )
    private void saveConnectionToDotAzure(Connection<?, ?> connection, Resource<?> consumer) {
        if (consumer instanceof ModuleResource) {
            final ModuleManager moduleManager = ModuleManager.getInstance(project);
            final Module m = moduleManager.findModuleByName(consumer.getName());
            if (Objects.nonNull(m)) {
                final AzureModule module = AzureModule.from(m);
                final AzureTaskManager taskManager = AzureTaskManager.getInstance();
                taskManager.write(() -> module.initializeWithDefaultProfileIfNot().createOrUpdateConnection(connection).save());
            }
        }
    }

    @Override
    public AzureForm<Connection<?, ?>> getForm() {
        return this;
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        return this.contentPane;
    }

    @Nullable
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Connection<?, ?> getValue() {
        final ResourceDefinition resourceDef = this.resourceTypeSelector.getValue();
        final ResourceDefinition consumerDef = this.consumerTypeSelector.getValue();
        final Resource resource = (Resource<?>) this.resourcePanel.getValue();
        final Resource consumer = (Resource<?>) this.consumerPanel.getValue();
        if (Objects.isNull(resource) || Objects.isNull(consumer)) {
            return null;
        }
        final ConnectionDefinition<?, ?> connectionDefinition = ConnectionManager.getDefinitionOrDefault(resourceDef, consumerDef);
        final Connection connection;
        if (Objects.isNull(this.connection)) {
            connection = connectionDefinition.define(resource, consumer);
        } else {
            connection = this.connection;
            connection.setResource(resource);
            connection.setConsumer(consumer);
            connection.setDefinition(connectionDefinition);
        }
        connection.setEnvPrefix(this.envPrefixTextField.getText().trim());
        return connection;
    }

    @Override
    public void setValue(Connection<?, ?> connection) {
        this.setConsumer(connection.getConsumer());
        this.setResource(connection.getResource());
        this.envPrefixTextField.setText(connection.getEnvPrefix());
        this.connection = connection;
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        final List<AzureFormInput<?>> inputs = new ArrayList<>();
        //noinspection unchecked
        Optional.ofNullable(resourcePanel).ifPresent(p -> inputs.addAll(p.getInputs()));
        //noinspection unchecked
        Optional.ofNullable(consumerPanel).ifPresent(p -> inputs.addAll(p.getInputs()));
        return inputs;
    }

    public void setResource(@Nullable final Resource<?> resource) {
        if (Objects.nonNull(resource)) {
            this.setResourceDefinition(resource.getDefinition());
            //noinspection unchecked
            this.resourcePanel.setValue(resource);
        } else {
            ResourceManager.getDefinitions(RESOURCE).stream().findFirst().ifPresent(this::setResourceDefinition);
        }
    }

    public void setConsumer(@Nullable final Resource<?> consumer) {
        if (Objects.nonNull(consumer)) {
            this.setConsumerDefinition(consumer.getDefinition());
            //noinspection unchecked
            this.consumerPanel.setValue(consumer);
        } else {
            ResourceManager.getDefinitions(CONSUMER).stream().findFirst().ifPresent(this::setConsumerDefinition);
        }
    }

    public void setResourceDefinition(@Nonnull ResourceDefinition<?> definition) {
        if (!definition.equals(this.resourceDefinition) || Objects.isNull(this.resourcePanel)) {
            this.resourceDefinition = definition;
            this.envPrefixTextField.setText(definition.getDefaultEnvPrefix());
            this.resourceTypeSelector.setValue(new ItemReference<>(definition.getName(), ResourceDefinition::getName));
            this.resourcePanel = this.updatePanel(definition, this.resourcePanelContainer);
        }
    }

    public void setConsumerDefinition(@Nonnull ResourceDefinition<?> definition) {
        if (!definition.equals(this.consumerDefinition) || Objects.isNull(this.consumerPanel)) {
            this.consumerDefinition = definition;
            this.consumerTypeSelector.setValue(new ItemReference<>(definition.getName(), ResourceDefinition::getName));
            this.consumerPanel = this.updatePanel(definition, this.consumerPanelContainer);
        }
    }

    private AzureFormJPanel<?> updatePanel(ResourceDefinition<?> definition, JPanel container) {
        final GridConstraints constraints = new GridConstraints();
        constraints.setFill(GridConstraints.FILL_BOTH);
        constraints.setHSizePolicy(GridConstraints.SIZEPOLICY_WANT_GROW);
        constraints.setUseParentLayout(true);
        final AzureFormJPanel<?> newResourcePanel = definition.getResourcePanel(this.project);
        container.removeAll();
        container.add(newResourcePanel.getContentPanel(), constraints);
        return newResourcePanel;
    }

    private void fixResourceType(ResourceDefinition<?> definition) {
        this.resourceTitle.setText(definition.getTitle());
        this.resourceTypeLabel.setVisible(false);
        this.resourceTypeSelector.setVisible(false);
    }

    private void fixConsumerType(ResourceDefinition<?> definition) {
        this.consumerTitle.setText(String.format("Consumer (%s)", definition.getTitle()));
        this.consumerTypeLabel.setVisible(false);
        this.consumerTypeSelector.setVisible(false);
    }

    private void createUIComponents() {
        this.consumerTypeSelector = new AzureComboBox<>(() -> ResourceManager.getDefinitions(CONSUMER)) {
            @Nonnull
            @Override
            protected List<ExtendableTextComponent.Extension> getExtensions() {
                return Collections.emptyList();
            }
        };
        this.resourceTypeSelector = new AzureComboBox<>(() -> ResourceManager.getDefinitions(RESOURCE)) {
            @Nonnull
            @Override
            protected List<ExtendableTextComponent.Extension> getExtensions() {
                return Collections.emptyList();
            }
        };
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
    }
}
