/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.actions;

import com.microsoft.azuretools.azureexplorer.forms.CreateArmStorageAccountForm;
import com.microsoft.azuretools.core.handlers.SignInCommandHandler;
import com.microsoft.azuretools.core.utils.PluginUtil;
import com.microsoft.tooling.msservices.helpers.Name;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.storage.StorageModule;

@Name("Create Storage Account...")
public class CreateArmStorageAccountAction extends NodeActionListener {

    private StorageModule storageModule;

    public CreateArmStorageAccountAction(StorageModule storageModule) {
        this.storageModule = storageModule;
    }

    @Override
    public void actionPerformed(NodeActionEvent e) {
        if (!SignInCommandHandler.doSignIn(PluginUtil.getParentShell())) return;
        CreateArmStorageAccountForm createStorageAccountForm = new CreateArmStorageAccountForm(PluginUtil.getParentShell(), null, null);

        createStorageAccountForm.setOnCreate(new Runnable() {
            @Override
            public void run() {
                storageModule.load(false);
            }
        });
        createStorageAccountForm.open();
    }
}
