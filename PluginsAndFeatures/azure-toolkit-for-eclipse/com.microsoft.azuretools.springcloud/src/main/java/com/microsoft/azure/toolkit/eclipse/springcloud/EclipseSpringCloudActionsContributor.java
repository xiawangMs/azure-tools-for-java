/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.springcloud;

import com.microsoft.azure.toolkit.eclipse.springcloud.creation.CreateSpringCloudAppAction;
import com.microsoft.azure.toolkit.eclipse.springcloud.deployment.DeploySpringCloudAppAction;
import com.microsoft.azure.toolkit.eclipse.springcloud.streaminglog.SpringCloudLogStreamingHandler;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.springcloud.SpringCloudActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.entity.IAzureBaseResource;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudCluster;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class EclipseSpringCloudActionsContributor implements IActionsContributor {
    @Override
    public void registerHandlers(AzureActionManager am) {
        this.registerCreateAppActionHandler(am);
        this.registerDeployAppActionHandler(am);
        this.registerLogStreamingActionHandler(am);
    }

    private void registerCreateAppActionHandler(AzureActionManager am) {
        final Predicate<Object> condition = (r) -> r instanceof SpringCloudCluster;
        final Consumer<Object> handler = (c) -> CreateSpringCloudAppAction.createApp((SpringCloudCluster) c);
        am.registerHandler(ResourceCommonActionsContributor.CREATE, condition, handler);
    }

    private void registerDeployAppActionHandler(AzureActionManager am) {
        final Predicate<IAzureBaseResource<?, ?>> condition = (r) -> r instanceof SpringCloudApp;
        final Consumer<IAzureBaseResource<?, ?>> handler = (c) -> DeploySpringCloudAppAction.deployToApp((SpringCloudApp) c);
        am.registerHandler(ResourceCommonActionsContributor.DEPLOY, condition, handler);
    }

    private void registerLogStreamingActionHandler(AzureActionManager am) {
        final Predicate<SpringCloudApp> condition = (r) -> r instanceof SpringCloudApp;
        final Consumer<SpringCloudApp> handler = (c) -> SpringCloudLogStreamingHandler.startLogStreaming(c);
        am.registerHandler(SpringCloudActionsContributor.STREAM_LOG, condition, handler);
    }

    public int getOrder() {
        return 2;
    }
}
