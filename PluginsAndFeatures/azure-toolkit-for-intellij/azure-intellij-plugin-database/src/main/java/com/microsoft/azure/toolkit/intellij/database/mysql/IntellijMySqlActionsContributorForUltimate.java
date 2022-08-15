/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.database.mysql;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.database.mysql.MySqlActionsContributor;
import com.microsoft.azure.toolkit.intellij.database.OpenInDatabaseToolsAction;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.database.JdbcUrl;
import com.microsoft.azure.toolkit.lib.mysql.MySqlServer;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.BiConsumer;

public class IntellijMySqlActionsContributorForUltimate implements IActionsContributor {
    private static final String NAME_PREFIX = "MySQL - %s";
    private static final String DEFAULT_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiConsumer<AzResource<?, ?, ?>, AnActionEvent> openDatabaseHandler = (c, e) -> openDatabaseTool(e.getProject(), (MySqlServer) c);
        am.registerHandler(MySqlActionsContributor.OPEN_DATABASE_TOOL, (r, e) -> true, openDatabaseHandler);
    }

    @AzureOperation(name = "mysql.open_by_database_tools.server", params = {"server.getName()"}, type = AzureOperation.Type.ACTION)
    private void openDatabaseTool(Project project, @Nonnull MySqlServer server) {
        final OpenInDatabaseToolsAction.DatasourceProperties properties = OpenInDatabaseToolsAction.DatasourceProperties.builder()
            .name(String.format(NAME_PREFIX, server.getName()))
            .driverClassName(DEFAULT_DRIVER_CLASS_NAME)
            .url(JdbcUrl.mysql(Objects.requireNonNull(server.getFullyQualifiedDomainName())).toString())
            .username(server.getAdminName() + "@" + server.getName())
            .build();
        AzureTaskManager.getInstance().runLater(() -> OpenInDatabaseToolsAction.openDataSourceManagerDialog(project, properties));
    }

    @Override
    public int getOrder() {
        return MySqlActionsContributor.INITIALIZE_ORDER + 1;
    }
}
