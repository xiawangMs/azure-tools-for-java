/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.actions;

import com.microsoft.azuretools.azureexplorer.forms.ExternalStorageAccountForm;
import com.microsoft.azuretools.core.utils.PluginUtil;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.ExternalStorageHelper;
import com.microsoft.tooling.msservices.helpers.Name;
import com.microsoft.tooling.msservices.model.storage.ClientStorageAccount;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.storage.ExternalStorageNode;
import com.microsoft.tooling.msservices.serviceexplorer.azure.storage.StorageModule;

@Name("Attach external storage account...")
public class AttachExternalStorageAccountAction extends NodeActionListener {
    private final StorageModule storageModule;

    public AttachExternalStorageAccountAction(StorageModule storageModule) {
        this.storageModule = storageModule;
    }

    @Override
    public void actionPerformed(NodeActionEvent e) {
        final ExternalStorageAccountForm form = new ExternalStorageAccountForm(PluginUtil.getParentShell(), "Attach External Storage Account");

        form.setOnFinish(new Runnable() {
            @Override
            public void run() {
                DefaultLoader.getIdeHelper().invokeLater(
                        new Runnable() {
                            public void run() {
                                ClientStorageAccount storageAccount = form.getStorageAccount();
                                ClientStorageAccount fullStorageAccount = form.getFullStorageAccount();

                                for (ClientStorageAccount clientStorageAccount : ExternalStorageHelper.getList(null)) {
                                    String name = storageAccount.getName();
                                    if (clientStorageAccount.getName().equals(name)) {
                                        DefaultLoader.getUIHelper().showError(
                                                "Storage account with name '" + name + "' already exists.",
                                                "Service Explorer");
                                        return;
                                    }
                                }

                                ExternalStorageNode node = new ExternalStorageNode(storageModule, fullStorageAccount);
                                storageModule.addChildNode(node);

                                ExternalStorageHelper.add(storageAccount);
                            }
                        });
            }
        });
        form.open();
    }
}
