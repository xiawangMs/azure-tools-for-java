/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.cosmos.creation;

import com.intellij.openapi.project.Project;
import com.microsoft.applicationinsights.core.dependencies.javaxannotation.Nonnull;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.cache.CacheManager;
import com.microsoft.azure.toolkit.lib.common.cache.LRUStack;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.operation.OperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.cosmos.CosmosDBAccount;
import com.microsoft.azure.toolkit.lib.cosmos.ICosmosDatabaseDraft;
import com.microsoft.azure.toolkit.lib.cosmos.model.DatabaseConfig;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BiFunction;

public class CreateCosmosDatabaseAction {

    public static <T extends CosmosDBAccount> void create(@Nonnull Project project, @Nonnull T account,
                                                          @Nonnull BiFunction<T, DatabaseConfig, ICosmosDatabaseDraft<?, ?>> draftSupplier,
                                                          @Nullable final DatabaseConfig data) {
        AzureTaskManager.getInstance().runLater(() -> {
            final CosmosDatabaseCreationDialog dialog = new CosmosDatabaseCreationDialog(project, account);
            if (Objects.nonNull(data)) {
                dialog.getForm().setValue(data);
            }
            dialog.setOkActionListener((config) -> {
                dialog.close();
                doCreate(account, draftSupplier, config);
            });
            dialog.show();
        });
    }

    @AzureOperation(name = "user/cosmos.create_database.database|account", params = {"config.getName()", "account.getName()"})
    private static <T extends CosmosDBAccount> void doCreate(@Nonnull T account,
                                                             @Nonnull BiFunction<T, DatabaseConfig, ICosmosDatabaseDraft<?, ?>> draftSupplier,
                                                             @Nullable final DatabaseConfig config) {
        final AzureString title = OperationBundle.description("user/cosmos.create_database.database|account", Objects.requireNonNull(config).getName(), account.getName());
        AzureTaskManager.getInstance().runInBackground(title, () -> {
            final ICosmosDatabaseDraft<?, ?> draft = draftSupplier.apply(account, config);
            draft.setConfig(config);
            draft.commit();
            final LRUStack history = CacheManager.getUsageHistory(draft.getClass());
            history.push(draft);
        });
    }
}
