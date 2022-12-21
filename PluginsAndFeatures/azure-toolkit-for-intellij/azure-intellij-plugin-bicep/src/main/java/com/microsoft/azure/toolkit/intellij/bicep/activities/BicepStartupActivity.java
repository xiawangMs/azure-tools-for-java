/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.bicep.activities;

import com.intellij.openapi.application.PluginPathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.messager.ExceptionNotification;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.intellij.CommonConst;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.wso2.lsp4intellij.IntellijLanguageClient;
import org.wso2.lsp4intellij.client.languageserver.serverdefinition.RawCommandServerDefinition;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;

import static com.microsoft.azure.toolkit.lib.common.utils.CommandUtils.exec;

public class BicepStartupActivity implements StartupActivity {
    protected static final Logger LOG = Logger.getInstance(BicepStartupActivity.class);
    public static final String BICEP_LANGSERVER = "bicep-langserver";
    public static final String BICEP_LANG_SERVER_DLL = "Bicep.LangServer.dll";
    public static final String DOTNET = "dotnet";

    @Override
    @ExceptionNotification
    @AzureOperation(name = "platform/bicep.prepare_textmate_bundles")
    public void runActivity(@Nonnull Project project) {
        final String pluginHome = PluginPathManager.getPluginHomePath(CommonConst.PLUGIN_NAME);
        final File bicep = FileUtils.getFile(pluginHome, "bicep", BICEP_LANGSERVER, BICEP_LANG_SERVER_DLL);
        if (Objects.isNull(bicep) || !bicep.exists()) {
            AzureMessager.getMessager().warning("Bicep Language Server was not found, disable related features.");
            return;
        }
        final String dotnetRuntimePath = Azure.az().config().getDotnetRuntimePath();
        final String command = StringUtils.isEmpty(dotnetRuntimePath) ? DOTNET : Paths.get(dotnetRuntimePath, DOTNET).toString();
        if (!isDotnetRuntimeReady(command)) {
            final Action<Object> openSettingsAction = AzureActionManager.getInstance().getAction(ResourceCommonActionsContributor.OPEN_AZURE_SETTINGS);
            AzureMessager.getMessager().warning(".NET runtime is required for Bicep language support, please configure the path for .NET runtime and reopen the project", null, openSettingsAction);
            return;
        }
        IntellijLanguageClient.addServerDefinition(new RawCommandServerDefinition("bicep", new String[]{command, bicep.getAbsolutePath(), "--stdio"}));
    }

    private static boolean isDotnetRuntimeReady(final String command) {
        try {
            return StringUtils.isNoneBlank(exec(String.format("%s --version", command)));
        } catch (final IOException e) {
            return false;
        }
    }
}
