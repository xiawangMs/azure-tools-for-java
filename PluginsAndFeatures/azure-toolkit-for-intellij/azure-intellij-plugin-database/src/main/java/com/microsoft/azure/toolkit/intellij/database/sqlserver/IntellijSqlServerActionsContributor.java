/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.database.sqlserver;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.database.sqlserver.SqlServerActionsContributor;
import com.microsoft.azure.toolkit.intellij.connector.ConnectorDialog;
import com.microsoft.azure.toolkit.intellij.database.connection.SqlDatabaseResource;
import com.microsoft.azure.toolkit.intellij.database.sqlserver.connection.SqlServerDatabaseResourceDefinition;
import com.microsoft.azure.toolkit.intellij.database.sqlserver.creation.CreateSqlServerAction;
import com.microsoft.azure.toolkit.intellij.database.sqlserver.creation.SqlServerCreationDialog;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.database.DatabaseServerConfig;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;
import com.microsoft.azure.toolkit.lib.sqlserver.AzureSqlServer;
import com.microsoft.azure.toolkit.lib.sqlserver.MicrosoftSqlServer;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class IntellijSqlServerActionsContributor implements IActionsContributor {
    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiPredicate<Object, AnActionEvent> condition = (r, e) -> r instanceof AzureSqlServer;
        final BiConsumer<Object, AnActionEvent> handler = (c, e) -> CreateSqlServerAction.create(e.getProject(), null);
        am.registerHandler(ResourceCommonActionsContributor.CREATE, condition, handler);

        am.<AzResource<?, ?, ?>, AnActionEvent>registerHandler(ResourceCommonActionsContributor.CONNECT, (r, e) -> r instanceof MicrosoftSqlServer,
            (o, e) -> AzureTaskManager.getInstance().runLater(() -> {
                final ConnectorDialog dialog = new ConnectorDialog(e.getProject());
                final MicrosoftSqlServer server = (MicrosoftSqlServer) o;
                dialog.setResource(new SqlDatabaseResource<>(server.databases().list().get(0),
                    server.getAdminName() + "@" + server.getName(), SqlServerDatabaseResourceDefinition.INSTANCE));
                dialog.show();
            }));

        final BiConsumer<ResourceGroup, AnActionEvent> groupCreateServerHandler = (r, e) -> {
            final DatabaseServerConfig config = SqlServerCreationDialog.getDefaultConfig();
            config.setSubscription(r.getSubscription());
            config.setRegion(r.getRegion());
            config.setResourceGroup(r);
            CreateSqlServerAction.create(e.getProject(), config);
        };
        am.registerHandler(SqlServerActionsContributor.GROUP_CREATE_SQLSERVER, (r, e) -> true, groupCreateServerHandler);
    }

    @Override
    public int getOrder() {
        return SqlServerActionsContributor.INITIALIZE_ORDER + 1;
    }
}
