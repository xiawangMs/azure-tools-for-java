/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.bicep.activities;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginInstaller;
import com.intellij.ide.plugins.PluginStateListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.ui.EditorNotifications;
import com.microsoft.azure.toolkit.ide.common.dotnet.DotnetRuntimeHandler;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import com.microsoft.azure.toolkit.lib.common.messager.ExceptionNotification;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.intellij.CommonConst;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.wso2.lsp4intellij.IntellijLanguageClient;
import org.wso2.lsp4intellij.client.languageserver.serverdefinition.ProcessBuilderServerDefinition;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Optional;

public class BicepStartupActivity implements StartupActivity, PluginStateListener {
    protected static final Logger LOG = Logger.getInstance(BicepStartupActivity.class);
    public static final String BICEP_LANGSERVER = "bicep-langserver";
    public static final String BICEP_LANG_SERVER_DLL = "Bicep.LangServer.dll";
    public static final String STDIO = "--stdio";
    public static final String BICEP = "bicep";

    @Override
    @ExceptionNotification
    @AzureOperation(name = "platform/bicep.startup_language_server")
    public void runActivity(@Nonnull Project project) {
        final File bicep = FileUtils.getFile(CommonConst.PLUGIN_PATH, "bicep", BICEP_LANGSERVER, BICEP_LANG_SERVER_DLL);
        final String dotnet = Azure.az().config().getDotnetRuntimePath();
        if (ObjectUtils.anyNull(bicep, dotnet) || !bicep.exists()) {
            return;
        }
        if (!DotnetRuntimeHandler.isDotnetRuntimeInstalled(dotnet)) {
            AzureEventBus.on("dotnet_runtime.installed", new AzureEventBus.EventListener(e -> registerLanguageServerDefinition(project)));
            return;
        }
        PluginInstaller.addStateListener(this);
        registerLanguageServerDefinition(project);
    }

    public static void registerLanguageServerDefinition(@Nonnull Project project) {
        EditorNotifications.getInstance(project).updateAllNotifications();
        final File bicep = FileUtils.getFile(CommonConst.PLUGIN_PATH, "bicep", BICEP_LANGSERVER, BICEP_LANG_SERVER_DLL);
        final String dotnet = Azure.az().config().getDotnetRuntimePath();
        final ProcessBuilder process = SystemUtils.IS_OS_WINDOWS ?
            new ProcessBuilder("powershell.exe", "./dotnet", bicep.getAbsolutePath(), STDIO) :
            new ProcessBuilder("./dotnet", bicep.getAbsolutePath(), STDIO);
        Optional.of(dotnet)
            .filter(StringUtils::isNotEmpty).map(File::new)
            .filter(File::exists).ifPresent(process::directory);
        IntellijLanguageClient.addServerDefinition(new ProcessBuilderServerDefinition(BICEP, process), project);
    }

    @Override
    public void install(@Nonnull IdeaPluginDescriptor ideaPluginDescriptor) {
    }

    @Override
    public void uninstall(@Nonnull IdeaPluginDescriptor ideaPluginDescriptor) {
        if (ideaPluginDescriptor.getPluginId().getIdString().equalsIgnoreCase(CommonConst.PLUGIN_ID)) {
            LOG.info("-------------------------------------------------------");
            LOG.info("stopping all language servers at uninstalling plugin " + ideaPluginDescriptor.getName());
            IntellijLanguageClient.stopAllLanguageServers();
        }
    }
}
