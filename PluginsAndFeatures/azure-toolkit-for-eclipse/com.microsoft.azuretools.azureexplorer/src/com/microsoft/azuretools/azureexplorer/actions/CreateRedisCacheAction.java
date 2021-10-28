/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.actions;

import com.microsoft.azuretools.azureexplorer.forms.createrediscache.CreateRedisCacheForm;
import com.microsoft.azuretools.core.handlers.SignInCommandHandler;
import com.microsoft.azuretools.core.utils.PluginUtil;
import com.microsoft.tooling.msservices.helpers.Name;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.rediscache.RedisCacheModule;

import java.io.IOException;


@Name("Create Redis Cache")
public class CreateRedisCacheAction extends NodeActionListener {

    private RedisCacheModule redisCacheModule;

    public CreateRedisCacheAction(RedisCacheModule redisCacheModule) {
        this.redisCacheModule = redisCacheModule;
    }

    @Override
    public void actionPerformed(NodeActionEvent e) {
        SignInCommandHandler.requireSignedIn(PluginUtil.getParentShell(), () -> {
            try {
                CreateRedisCacheForm createRedisCacheForm = new CreateRedisCacheForm(PluginUtil.getParentShell());
                createRedisCacheForm.create();
                createRedisCacheForm.setOnCreate(() -> {
                    if (redisCacheModule != null) {
                        redisCacheModule.load(false);
                    }
                });
                createRedisCacheForm.open();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        );
    }
}
