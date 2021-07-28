/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.serviceexplorer.azure.rediscache;

import com.intellij.openapi.project.Project;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.ijidea.actions.AzureSignInAction;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.forms.CreateRedisCacheForm;
import com.microsoft.intellij.util.AzureLoginHelper;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.Name;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.rediscache.RedisCacheModule;

@Name("Create Redis Cache")
public class CreateRedisCacheAction extends NodeActionListener {
    private static final String ERROR_CREATING_REDIS_CACHE = "Error creating Redis cache";
    private RedisCacheModule redisCacheModule;

    public CreateRedisCacheAction(RedisCacheModule redisModule) {
        this.redisCacheModule = redisModule;
    }

    @Override
    public void actionPerformed(NodeActionEvent e) {
        Project project = (Project) redisCacheModule.getProject();
        try {
            if (!AzureSignInAction.doSignIn(AuthMethodManager.getInstance(), project)) {
                return;
            }
            if (!AzureLoginHelper.isAzureSubsAvailableOrReportError(ERROR_CREATING_REDIS_CACHE)) {
                return;
            }
            CreateRedisCacheForm createRedisCacheForm = new CreateRedisCacheForm(project);
            createRedisCacheForm.setOnCreate(new Runnable() {
                @Override
                public void run() {
                    if (redisCacheModule != null) {
                        redisCacheModule.load(false);
                    }
                }
            });
            createRedisCacheForm.show();
        } catch (Exception ex) {
            AzurePlugin.log(ERROR_CREATING_REDIS_CACHE, ex);
            DefaultLoader.getUIHelper().showException(ERROR_CREATING_REDIS_CACHE, ex, "Error creating Redis Cache", false, true);
        }
    }
}
