/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.redis.action;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.Draft;
import com.microsoft.azure.toolkit.intellij.redis.RedisCreationDialog;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.resource.task.CreateResourceGroupTask;
import com.microsoft.azure.toolkit.redis.AzureRedis;
import com.microsoft.azure.toolkit.redis.RedisCache;
import com.microsoft.azure.toolkit.redis.RedisConfig;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.actions.AzureSignInAction;
import com.microsoft.intellij.util.AzureLoginHelper;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.Name;
import com.microsoft.tooling.msservices.serviceexplorer.AzureActionEnum;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.rediscache.RedisCacheModule;
import lombok.AllArgsConstructor;

@Name("Create")
public class CreateRedisCacheAction extends NodeActionListener {
    private static final String ERROR_CREATING_REDIS_CACHE = "Error creating Redis cache";
    private RedisCacheModule redisCacheModule;

    public CreateRedisCacheAction(RedisCacheModule redisModule) {
        this.redisCacheModule = redisModule;
    }

    @Override
    public AzureActionEnum getAction() {
        return AzureActionEnum.CREATE;
    }

    @Override
    public void actionPerformed(NodeActionEvent e) {
        Project project = (Project) redisCacheModule.getProject();
        AzureSignInAction.requireSignedIn(project, () -> this.doActionPerformed(e, true, project));
    }

    private void doActionPerformed(NodeActionEvent e, boolean isLoggedIn, Project project) {
        try {
            if (!isLoggedIn) {
                return;
            }
            if (!AzureLoginHelper.isAzureSubsAvailableOrReportError(ERROR_CREATING_REDIS_CACHE)) {
                return;
            }
            RedisCreationDialog createRedisCacheForm = new RedisCreationDialog(project);
            createRedisCacheForm.setOkActionListener(config -> {
                createRedis(config, project);
                createRedisCacheForm.close();
            });
            createRedisCacheForm.show();
        } catch (Exception ex) {
            AzurePlugin.log(ERROR_CREATING_REDIS_CACHE, ex);
            DefaultLoader.getUIHelper().showException(ERROR_CREATING_REDIS_CACHE, ex, "Error creating Redis Cache", false, true);
        }
    }

    private void createRedis(final RedisConfig config, final Project project) {
        final Runnable runnable = () -> {
            final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
            indicator.setIndeterminate(true);
            new CreateRedisTask(config).execute();
            if (redisCacheModule != null) {
                redisCacheModule.load(false);
            }
        };
        String progressMessage = Node.getProgressMessage(AzureActionEnum.CREATE.getDoingName(), RedisCacheModule.MODULE_NAME, config.getName());
        AzureTaskManager.getInstance().runInBackground(new AzureTask<>(project, progressMessage, false, runnable));
    }

    @AllArgsConstructor
    static class CreateRedisTask extends AzureTask<RedisCache> {

        private RedisConfig config;

        public RedisCache execute() {
            ResourceGroup rg = config.getResourceGroup();
            if (rg instanceof Draft) {
                new CreateResourceGroupTask(rg.getSubscriptionId(), rg.getName(), config.getRegion()).execute();
            }
            return Azure.az(AzureRedis.class).subscription(config.getSubscription()).create(config);
        }
    }
}
