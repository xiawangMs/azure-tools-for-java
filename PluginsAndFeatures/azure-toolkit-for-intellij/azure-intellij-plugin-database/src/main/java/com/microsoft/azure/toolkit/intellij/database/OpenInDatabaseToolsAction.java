/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.database;

import com.intellij.database.autoconfig.DataSourceDetector;
import com.intellij.database.autoconfig.DataSourceRegistry;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.view.ui.DataSourceManagerDialog;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.FUSEventSource;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import lombok.Builder;
import lombok.Getter;

import java.util.function.Consumer;

public class OpenInDatabaseToolsAction {

    private static final String DATABASE_TOOLS_PLUGIN_ID = "com.intellij.database";
    private static final String DATABASE_PLUGIN_NOT_INSTALLED = "\"Database tools and SQL\" plugin is not installed.";
    private static final String NOT_SUPPORT_ERROR_ACTION = "\"Database tools and SQL\" plugin is only provided in IntelliJ Ultimate edition.";
    private static final String ERROR_MESSAGE_PATTERN = "Failed to open \"Data Sources and Drivers\" dialog for %s";
    private static final String IDE_DOWNLOAD_URL = "https://www.jetbrains.com/idea/download/";

    public static void openDataSourceManagerDialog(Project project, DatasourceProperties properties) {
        if (PluginManagerCore.getPlugin(PluginId.findId(DATABASE_TOOLS_PLUGIN_ID)) == null) {
            final Consumer<Object> handler = (r) -> FUSEventSource.NOTIFICATION.openDownloadPageAndLog(project, IDE_DOWNLOAD_URL);
            final ActionView.Builder view = new ActionView.Builder(IdeBundle.message("plugins.advertiser.action.try.ultimate", "IntelliJ IDEA Ultimate"));
            final Action.Id<Object> TRY_ULTIMATE = Action.Id.of("database.try_ultimate");
            final Action<Object> tryUltimate = new Action<>(TRY_ULTIMATE, handler, view);
            throw new AzureToolkitRuntimeException(DATABASE_PLUGIN_NOT_INSTALLED, NOT_SUPPORT_ERROR_ACTION, tryUltimate);
        }
        final DataSourceRegistry registry = new DataSourceRegistry(project);
        final DbPsiFacade dbPsiFacade = DbPsiFacade.getInstance(project);
        final DataSourceDetector.Builder builder = registry.getBuilder()
            .withName(properties.name)
            .withDriverClass(properties.driverClassName)
            .withUrl(properties.url)
            .withUser(properties.username)
            .commit();
        DataSourceManagerDialog.showDialog(dbPsiFacade, registry);
    }

    @Builder
    @Getter
    public static class DatasourceProperties {
        private String name;
        @Builder.Default
        private String driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        private String url;
        private String username;
    }
}
