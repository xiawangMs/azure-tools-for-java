/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.database.sqlserver;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.database.sqlserver.SqlServerActionsContributor;
import com.microsoft.azure.toolkit.intellij.database.OpenInDatabaseToolsAction;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.database.JdbcUrl;
import com.microsoft.azure.toolkit.lib.sqlserver.MicrosoftSqlServer;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.BiConsumer;

public class IntellijSqlServerActionsContributorForUltimate implements IActionsContributor {
    private static final String NAME_PREFIX = "SQL Server - %s";
    private static final String DEFAULT_DRIVER_CLASS_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiConsumer<AzResource<?, ?, ?>, AnActionEvent> openDatabaseHandler = (c, e) -> openDatabaseTool(e.getProject(), (MicrosoftSqlServer) c);
        am.registerHandler(SqlServerActionsContributor.OPEN_DATABASE_TOOL, (r, e) -> true, openDatabaseHandler);
    }

    @AzureOperation(name = "sqlserver.open_by_database_tools.server", params = {"server.getName()"}, type = AzureOperation.Type.ACTION)
    private void openDatabaseTool(Project project, @Nonnull MicrosoftSqlServer server) {
        final OpenInDatabaseToolsAction.DatasourceProperties properties = OpenInDatabaseToolsAction.DatasourceProperties.builder()
            .name(String.format(NAME_PREFIX, server.getName()))
            .driverClassName(DEFAULT_DRIVER_CLASS_NAME)
            .url(JdbcUrl.sqlserver(Objects.requireNonNull(server.getFullyQualifiedDomainName())).toString())
            .username(server.getAdminName() + "@" + server.getName())
            .build();
        AzureTaskManager.getInstance().runLater(() -> OpenInDatabaseToolsAction.openDataSourceManagerDialog(project, properties));
    }

    @Override
    public int getOrder() {
        return SqlServerActionsContributor.INITIALIZE_ORDER + 1;
    }
}
