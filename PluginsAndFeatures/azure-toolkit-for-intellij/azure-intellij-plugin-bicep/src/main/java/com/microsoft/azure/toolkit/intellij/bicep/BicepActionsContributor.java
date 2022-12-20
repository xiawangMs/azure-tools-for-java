/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.bicep;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.common.dotnet.DotnetRuntimeHandler;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;

import java.io.File;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

public class BicepActionsContributor implements IActionsContributor {

    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiConsumer<Object, AnActionEvent> downloadFuncCoreToolsHandler = (v, e) -> {
            final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
            descriptor.setTitle("Select Path to Install .NET Runtime");
            AzureTaskManager.getInstance().runLater(() -> FileChooser.chooseFile(descriptor, null, getDefaultDotnetPath(), files -> {
                final String installPath = files.getPath();
                AzureTaskManager.getInstance().runInBackground("Download and Install .NET Runtime",
                        () -> DotnetRuntimeHandler.installDotnet(installPath));
            }));
        };
        am.registerHandler(ResourceCommonActionsContributor.INSTALL_DOTNET_RUNTIME, downloadFuncCoreToolsHandler);
    }

    private static VirtualFile getDefaultDotnetPath() {
        try {
            final File dotnet = Paths.get(System.getProperty("user.home"), ".dotnet-runtime").toFile();
            if (!dotnet.exists()) {
                dotnet.mkdirs();
            }
            return dotnet.isFile() ? null : LocalFileSystem.getInstance().findFileByIoFile(dotnet);
        } catch (RuntimeException e) {
            // swallow exception when get default location
            return null;
        }
    }
}
