/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.storage;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.storage.StorageActionsContributor;
import com.microsoft.azure.toolkit.intellij.common.fileexplorer.VirtualFileActions;
import com.microsoft.azure.toolkit.intellij.connector.AzureServiceResource;
import com.microsoft.azure.toolkit.intellij.connector.ConnectorDialog;
import com.microsoft.azure.toolkit.intellij.storage.connection.StorageAccountResourceDefinition;
import com.microsoft.azure.toolkit.intellij.storage.creation.CreateStorageAccountAction;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.operation.OperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;
import com.microsoft.azure.toolkit.lib.storage.AzureStorageAccount;
import com.microsoft.azure.toolkit.lib.storage.StorageAccount;
import com.microsoft.azure.toolkit.lib.storage.model.StorageAccountConfig;
import com.microsoft.azure.toolkit.lib.storage.model.StorageFile;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.OutputStream;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class IntellijStorageActionsContributor implements IActionsContributor {
    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiPredicate<Object, AnActionEvent> condition = (r, e) -> r instanceof AzureStorageAccount;
        final BiConsumer<Object, AnActionEvent> handler = (c, e) -> CreateStorageAccountAction.create(e.getProject(), null);
        am.registerHandler(ResourceCommonActionsContributor.CREATE, condition, handler);

        am.<AzResource, AnActionEvent>registerHandler(ResourceCommonActionsContributor.CONNECT, (r, e) -> r instanceof StorageAccount,
            (r, e) -> AzureTaskManager.getInstance().runLater(OperationBundle.description("storage.open_azure_storage_explorer.account", r.getName()), () -> {
                final ConnectorDialog dialog = new ConnectorDialog(e.getProject());
                dialog.setResource(new AzureServiceResource<>(((StorageAccount) r), StorageAccountResourceDefinition.INSTANCE));
                dialog.show();
            }));

        am.registerHandler(StorageActionsContributor.OPEN_FILE, (r, e) -> r instanceof StorageFile && !r.isDirectory(),
            (file, e) -> openFileInEditor(file, ((AnActionEvent) e).getProject()));

        final BiConsumer<ResourceGroup, AnActionEvent> groupCreateAccountHandler = (r, e) -> {
            final StorageAccountConfig config = StorageAccountConfig.builder().build();
            config.setSubscription(r.getSubscription());
            config.setRegion(r.getRegion());
            config.setResourceGroup(r);
            CreateStorageAccountAction.create(e.getProject(), config);
        };
        am.registerHandler(StorageActionsContributor.GROUP_CREATE_ACCOUNT, (r, e) -> true, groupCreateAccountHandler);
    }

    @SneakyThrows
    private static void openFileInEditor(StorageFile file, Project project) {
        final String failure = String.format("Can not open file (%s). Try downloading it first and open it manually.", file.getName());
        if (file.getSize() > 10 * FileUtils.ONE_MB) {
            AzureTaskManager.getInstance().runLater(() -> Messages.showWarningDialog(failure, "Open File"));
            return;
        }
        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        final VirtualFile virtualFile = VirtualFileActions.getOrCreateVirtualFile(file.getId(), file.getName(), fileEditorManager);
        final OutputStream output = virtualFile.getOutputStream(null);
        final AzureString title = OperationBundle.description("storage.load_content.file", virtualFile.getName());
        final AzureTask<Void> task = new AzureTask<>(project, title, false, () -> {
            final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
            indicator.setIndeterminate(true);
            file.download(output);
            AzureTaskManager.getInstance().runLater(() -> {
                final Consumer<String> contentSaver = content -> {
                };
                if (!VirtualFileActions.openFileInEditor(contentSaver, virtualFile, fileEditorManager)) {
                    Messages.showWarningDialog(failure, "Open File");
                }
            });
            IOUtils.closeQuietly(output, null);
        });
        AzureTaskManager.getInstance().runInModal(task);
    }

    @Override
    public int getOrder() {
        return StorageActionsContributor.INITIALIZE_ORDER + 1;
    }
}
