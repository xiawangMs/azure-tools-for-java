/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.database.mysql;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.database.mysql.MySqlActionsContributor;
import com.microsoft.azure.toolkit.intellij.connector.ConnectorDialog;
import com.microsoft.azure.toolkit.intellij.database.connection.SqlDatabaseResource;
import com.microsoft.azure.toolkit.intellij.database.mysql.connection.MySqlDatabaseResourceDefinition;
import com.microsoft.azure.toolkit.intellij.database.mysql.creation.CreateMySqlAction;
import com.microsoft.azure.toolkit.intellij.database.mysql.creation.MySqlCreationDialog;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.database.DatabaseServerConfig;
import com.microsoft.azure.toolkit.lib.mysql.AzureMySql;
import com.microsoft.azure.toolkit.lib.mysql.MySqlServer;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class IntellijMySqlActionsContributor implements IActionsContributor {
    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiPredicate<Object, AnActionEvent> condition = (r, e) -> r instanceof AzureMySql;
        final BiConsumer<Object, AnActionEvent> handler = (c, e) -> CreateMySqlAction.create(e.getProject(), null);
        am.registerHandler(ResourceCommonActionsContributor.CREATE, condition, handler);

        am.<AzResource<?, ?, ?>, AnActionEvent>registerHandler(ResourceCommonActionsContributor.CONNECT, (r, e) -> r instanceof MySqlServer,
            (o, e) -> AzureTaskManager.getInstance().runLater(() -> {
                final ConnectorDialog dialog = new ConnectorDialog(e.getProject());
                final MySqlServer server = (MySqlServer) o;
                dialog.setResource(new SqlDatabaseResource<>(server.databases().list().get(0),
                    server.getAdminName() + "@" + server.getName(), MySqlDatabaseResourceDefinition.INSTANCE));
                dialog.show();
            }));

        final BiConsumer<ResourceGroup, AnActionEvent> groupCreateMySqlHandler = (r, e) -> {
            final DatabaseServerConfig config = MySqlCreationDialog.getDefaultConfig();
            config.setSubscription(r.getSubscription());
            config.setRegion(r.getRegion());
            config.setResourceGroup(r);
            CreateMySqlAction.create(e.getProject(), config);
        };
        am.registerHandler(MySqlActionsContributor.GROUP_CREATE_MYSQL, (r, e) -> true, groupCreateMySqlHandler);
    }

    @Override
    public int getOrder() {
        return MySqlActionsContributor.INITIALIZE_ORDER + 1;
    }
}
