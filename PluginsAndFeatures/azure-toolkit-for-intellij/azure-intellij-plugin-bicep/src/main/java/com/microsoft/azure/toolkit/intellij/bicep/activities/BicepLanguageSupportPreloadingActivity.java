/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.bicep.activities;

import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.progress.ProgressIndicator;
import com.microsoft.azure.toolkit.intellij.bicep.highlight.ZipResourceUtils;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.wso2.lsp4intellij.IntellijLanguageClient;
import org.wso2.lsp4intellij.client.languageserver.serverdefinition.RawCommandServerDefinition;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static com.microsoft.azure.toolkit.intellij.bicep.highlight.BicepEditorHighlighterProvider.LIB_ROOT;
import static com.microsoft.azure.toolkit.lib.common.utils.CommandUtils.exec;

public class BicepLanguageSupportPreloadingActivity extends PreloadingActivity {
    public static final String BICEP_SERVER = "/bicep-langserver.zip";
    public static final String BICEP_LANGSERVER = "bicep-langserver";
    public static final String BICEP_LANG_SERVER_DLL = "Bicep.LangServer.dll";

    @Override
    public void preload(@NotNull ProgressIndicator indicator) {
        ZipResourceUtils.copyTextMateBundlesFromJar(BICEP_SERVER, "bicep-langserver");
        final File bicep = FileUtils.getFile(LIB_ROOT, BICEP_LANGSERVER, BICEP_LANG_SERVER_DLL);
        if (Objects.isNull(bicep) || !bicep.exists()) {
            AzureMessager.getMessager().warning("Bicep Language Server was not found, disable related features.");
            return;
        }
        if (!isDotnetRuntimeReady()) {
            AzureMessager.getMessager().warning(".NET runtime was not founded, Bicep language support was disabled. To use bicep features, please install .NET runtime and reopen the project");
            return;
        }
        IntellijLanguageClient.addServerDefinition(new RawCommandServerDefinition("bicep", new String[]{"dotnet", bicep.getAbsolutePath(), "--stdio"}));
    }

    private boolean isDotnetRuntimeReady() {
        try {
            return StringUtils.isNoneBlank(exec("dotnet --version"));
        } catch (final IOException e) {
            return false;
        }
    }
}
