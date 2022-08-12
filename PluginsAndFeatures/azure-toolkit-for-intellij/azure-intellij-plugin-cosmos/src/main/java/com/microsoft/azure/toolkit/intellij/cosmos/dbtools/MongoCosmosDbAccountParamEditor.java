/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.cosmos.dbtools;

import com.intellij.credentialStore.OneTimeString;
import com.intellij.database.dataSource.DataSourceConfigurable;
import com.intellij.database.dataSource.LocalDataSource;
import com.intellij.database.dataSource.url.DataInterchange;
import com.intellij.database.dataSource.url.FieldSize;
import com.intellij.database.dataSource.url.TypesRegistry;
import com.intellij.database.dataSource.url.template.UrlEditorModel;
import com.intellij.database.dataSource.url.ui.ParamEditorBase;
import com.intellij.database.dataSource.url.ui.UrlPropertiesPanel;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.Consumer;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.cosmos.AzureCosmosService;
import com.microsoft.azure.toolkit.lib.cosmos.CosmosDBAccount;
import com.microsoft.azure.toolkit.lib.cosmos.model.DatabaseAccountKind;
import com.microsoft.azure.toolkit.lib.cosmos.model.MongoDatabaseAccountConnectionString;
import com.microsoft.azure.toolkit.lib.cosmos.mongo.MongoCosmosDBAccount;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class MongoCosmosDbAccountParamEditor extends ParamEditorBase<ComboBox<MongoCosmosDBAccount>> {
    public static final String KEY_COSMOS_ACCOUNT_ID = "AZURE_COSMOS_ACCOUNT";
    public static final String KEY_USERNAME = "username";
    @Getter
    @Setter
    private String text = "";
    private MongoCosmosDBAccount account;
    private MongoDatabaseAccountConnectionString connectionString;
    private boolean updating;

    public MongoCosmosDbAccountParamEditor(@NotNull String label, @NotNull DataInterchange interchange) {
        super(new MongoCosmosDbAccountComboBox(), interchange, FieldSize.LARGE, label);

        final MongoCosmosDbAccountComboBox combox = (MongoCosmosDbAccountComboBox) this.getEditorComponent();
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
            if (StringUtils.equals(propertyName, "host") && !Objects.equals(this.connectionString.getHost(), newValue) ||
                StringUtils.equals(propertyName, "port") && !Objects.equals(this.connectionString.getPort() + "", newValue)) {
                ((MongoCosmosDbAccountComboBox) this.getEditorComponent()).setValue((MongoCosmosDBAccount) null);
                this.setAccount(null);
            }
        }
    }

    private void setAccount(@Nullable MongoCosmosDBAccount account) {
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
            this.connectionString = account.getMongoConnectionString();
            final LocalDataSource dataSource = interchange.getDataSource();
            final String host = connectionString.getHost();
            final String port = String.valueOf(connectionString.getPort());
            final String user = connectionString.getUsername();
            final String password = String.valueOf(connectionString.getPassword());
            MongoCosmosDbAccountParamEditor.this.text = connectionString.getConnection();
            AzureTaskManager.getInstance().runLater(() -> {
                LocalDataSource.setUsername(dataSource, user);
                interchange.getCredentials().storePassword(dataSource, new OneTimeString(password));
                this.setUseSsl(true);
                this.getDataSourceConfigurable().getDataSource().setAuthProviderId(AzurePluginAuthProvider.ID);
                final Map<String, String> map = new HashMap<>();
                interchange.putProperties(consumer -> {
                    consumer.consume(KEY_COSMOS_ACCOUNT_ID, account.getId());
                    consumer.consume(KEY_USERNAME, user);
                    consumer.consume("host", host);
                    consumer.consume("user", user);
                    consumer.consume("port", port);
                    this.updating = false;
                });
                this.setUsernameAndPassword(user, password);
            }, AzureTask.Modality.ANY);
        });
    }

    static class MongoCosmosDbAccountFactory implements TypesRegistry.TypeDescriptorFactory {
        private static final String TYPE_NAME = "cosmos_account_mongo";
        private static final String CAPTION = "Account";
        private static final String PARAM_NAME = "account";

        MongoCosmosDbAccountFactory() {
            makeAccountShowAtTop();
        }

        @SuppressWarnings("unchecked")
        private static void makeAccountShowAtTop() {
            try {
                final Field HEADS = FieldUtils.getField(UrlPropertiesPanel.class, "HEADS", true);
                final List<String> heads = (List<String>) FieldUtils.readStaticField(HEADS, true);
                if (!heads.contains(PARAM_NAME)) {
                    final Object[] old = heads.toArray();
                    heads.set(0, PARAM_NAME);
                    for (int i = 0; i < old.length - 1; i++) {
                        heads.set(i + 1, (String) old[i]);
                    }
                }
                System.out.println(heads.size());
            } catch (final Throwable ignored) {
            }
        }

        public void createTypeDescriptor(@NotNull Consumer<? super TypesRegistry.TypeDescriptor> consumer) {
            consumer.consume(new TypesRegistry.BaseTypeDescriptor(TYPE_NAME, ".", CAPTION) {
                @NotNull
                protected TypesRegistry.@NotNull ParamEditor createFieldImpl(@NotNull String caption, @Nullable String configuration, @NotNull DataInterchange interchange) {
                    return new MongoCosmosDbAccountParamEditor(formatFieldCaption(CAPTION), interchange);
                }
            });
        }
    }

    private static class MongoCosmosDbAccountComboBox extends AzureComboBox<MongoCosmosDBAccount> {

        @Nonnull
        @Override
        protected List<MongoCosmosDBAccount> loadItems() {
            if (!Azure.az(AzureAccount.class).isLoggedIn()) {
                return Collections.emptyList();
            }
            return Azure.az(AzureCosmosService.class).accounts(DatabaseAccountKind.MONGO_DB).stream()
                .filter(m -> !m.isDraftForCreating()).map(a -> ((MongoCosmosDBAccount) a)).collect(Collectors.toList());
        }

        @Override
        protected String getItemText(Object item) {
            return Optional.ofNullable(item).map(i -> ((CosmosDBAccount) i)).map(AbstractAzResource::getName).orElse("");
        }
    }

    private void setUsernameAndPassword(String user, String password) {
        final UrlEditorModel model = this.getDataSourceConfigurable().getUrlEditor().getEditorModel();
        model.setParameter("user", user);
        model.setParameter("password", password);
        model.commit(true);
    }

    @SneakyThrows
    private void setUseSsl(boolean useSsl) {
        final DataSourceConfigurable configurable = this.getDataSourceConfigurable();
        final JBCheckBox useSSLCheckBox = (JBCheckBox) FieldUtils.readField(configurable.getSshSslPanel(), "myUseSSLJBCheckBox", true);
        useSSLCheckBox.setSelected(useSsl);
    }

    @SneakyThrows
    private DataSourceConfigurable getDataSourceConfigurable() {
        return (DataSourceConfigurable) FieldUtils.readField(this.getInterchange(), "myConfigurable", true);
    }
}
