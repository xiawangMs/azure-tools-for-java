/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.cosmos.dbtools;

import com.intellij.credentialStore.OneTimeString;
import com.intellij.database.dataSource.LocalDataSource;
import com.intellij.database.dataSource.url.DataInterchange;
import com.intellij.database.dataSource.url.FieldSize;
import com.intellij.database.dataSource.url.TypesRegistry;
import com.intellij.database.dataSource.url.ui.ParamEditorBase;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.Consumer;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.cosmos.AzureCosmosService;
import com.microsoft.azure.toolkit.lib.cosmos.CosmosDBAccount;
import com.microsoft.azure.toolkit.lib.cosmos.cassandra.CassandraCosmosDBAccount;
import com.microsoft.azure.toolkit.lib.cosmos.model.CassandraDatabaseAccountConnectionString;
import com.microsoft.azure.toolkit.lib.cosmos.model.DatabaseAccountKind;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CassandraCosmosDbAccountParamEditor extends ParamEditorBase<ComboBox<CassandraCosmosDBAccount>> {
    public static final String KEY_COSMOS_ACCOUNT_ID = "AZURE_COSMOS_ACCOUNT";
    public static final String KEY_USERNAME = "username";
    @Getter
    @Setter
    private String text = "";
    private CassandraCosmosDBAccount account;
    private CassandraDatabaseAccountConnectionString connectionString;
    private boolean updating;

    public CassandraCosmosDbAccountParamEditor(@NotNull String label, @NotNull DataInterchange interchange) {
        super(new CassandraCosmosDbAccountComboBox(), interchange, FieldSize.LARGE, label);

        final CassandraCosmosDbAccountComboBox combox = (CassandraCosmosDbAccountComboBox) this.getEditorComponent();
        interchange.addPersistentProperty(KEY_COSMOS_ACCOUNT_ID);
        interchange.addPersistentProperty(KEY_USERNAME);
        interchange.addPersistentProperty("user");
        final String initialAccountId = interchange.getProperty(KEY_COSMOS_ACCOUNT_ID);
        combox.setValue(new AzureComboBox.ItemReference<>(i -> i.getId().equals(initialAccountId)));

        interchange.addPropertyChangeListener((evt -> onPropertiesChanged(evt.getPropertyName(), evt.getNewValue())), this);
        combox.addValueChangedListener(this::setAccount);
    }

    private void onPropertiesChanged(String propertyName, Object newValue) {
        if (!this.updating && Objects.nonNull(this.connectionString) && StringUtils.isNotEmpty((String) newValue)) {
            if (StringUtils.equals(propertyName, "host") && !Objects.equals(this.connectionString.getContactPoint(), newValue) ||
                StringUtils.equals(propertyName, "port") && !Objects.equals(this.connectionString.getPort() + "", newValue)) {
                ((CassandraCosmosDbAccountComboBox) this.getEditorComponent()).setValue((CassandraCosmosDBAccount) null);
                this.setAccount(null);
            }
        }
    }

    private void setAccount(@Nullable CassandraCosmosDBAccount account) {
        if (this.updating || Objects.equals(this.account, account)) {
            return;
        }
        final DataInterchange interchange = this.getInterchange();
        this.account = account;
        if (account == null) {
            this.connectionString = null;
            interchange.putProperty(KEY_COSMOS_ACCOUNT_ID, null);
            return;
        }
        this.updating = true;
        // AzureActionManager.getInstance().getAction(Action.REQUIRE_AUTH).handle(combox::reloadItems);
        AzureTaskManager.getInstance().runOnPooledThread(() -> {
            this.connectionString = account.getCassandraConnectionString();
            final LocalDataSource dataSource = interchange.getDataSource();
            final String host = connectionString.getContactPoint();
            final String port = String.valueOf(connectionString.getPort());
            final String user = connectionString.getUsername();
            final String password = String.valueOf(connectionString.getPassword());
            CassandraCosmosDbAccountParamEditor.this.text = connectionString.getConnectionString();
            AzureTaskManager.getInstance().runLater(() -> {
                interchange.getDataSource().setAuthProviderId(AzurePluginAuthProvider.ID);
                LocalDataSource.setUsername(dataSource, user);
                interchange.getCredentials().storePassword(dataSource, new OneTimeString(password));
                final Map<String, String> map = new HashMap<>();
                interchange.putProperties(consumer -> {
                    consumer.consume(KEY_COSMOS_ACCOUNT_ID, account.getId());
                    consumer.consume(KEY_USERNAME, user);
                    consumer.consume("host", host);
                    consumer.consume("user", user);
                    consumer.consume("port", port);
                    this.updating = false;
                });
            }, AzureTask.Modality.ANY);
        });
    }

    static class CassandraCosmosDbAccountFactory implements TypesRegistry.TypeDescriptorFactory {
        private static final String TYPE_NAME = "cosmos_account_cassandra";
        private static final String CAPTION = "Account";
        private static final String PARAM_NAME = "account";

        public void createTypeDescriptor(@NotNull Consumer<? super TypesRegistry.TypeDescriptor> consumer) {
            consumer.consume(new TypesRegistry.BaseTypeDescriptor(TYPE_NAME, ".", CAPTION) {
                @NotNull
                protected TypesRegistry.@NotNull ParamEditor createFieldImpl(@NotNull String caption, @Nullable String configuration, @NotNull DataInterchange interchange) {
                    return new CassandraCosmosDbAccountParamEditor(formatFieldCaption(CAPTION), interchange);
                }
            });
        }
    }

    private static class CassandraCosmosDbAccountComboBox extends AzureComboBox<CassandraCosmosDBAccount> {

        @Nonnull
        @Override
        protected List<CassandraCosmosDBAccount> loadItems() {
            if (!Azure.az(AzureAccount.class).isLoggedIn()) {
                return Collections.emptyList();
            }
            return Azure.az(AzureCosmosService.class).accounts(DatabaseAccountKind.CASSANDRA).stream()
                .filter(m -> !m.isDraftForCreating()).map(a -> ((CassandraCosmosDBAccount) a)).collect(Collectors.toList());
        }

        @Override
        protected String getItemText(Object item) {
            return Optional.ofNullable(item).map(i -> ((CosmosDBAccount) i)).map(AbstractAzResource::getName).orElse("");
        }
    }
}
