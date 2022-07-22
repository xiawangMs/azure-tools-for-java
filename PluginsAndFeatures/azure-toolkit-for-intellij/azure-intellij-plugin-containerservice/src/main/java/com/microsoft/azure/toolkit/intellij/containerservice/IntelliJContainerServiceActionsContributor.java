/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerservice;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.containerservice.ContainerServiceActionsContributor;
import com.microsoft.azure.toolkit.intellij.containerservice.actions.DownloadKubuConfigAction;
import com.microsoft.azure.toolkit.intellij.containerservice.actions.GetKubuCredentialAction;
import com.microsoft.azure.toolkit.intellij.containerservice.creation.CreateKubernetesServiceAction;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.containerservice.AzureContainerService;
import com.microsoft.azure.toolkit.lib.containerservice.KubernetesCluster;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import static com.microsoft.azure.toolkit.intellij.containerservice.creation.CreateKubernetesServiceAction.getDefaultConfig;

public class IntelliJContainerServiceActionsContributor implements IActionsContributor {
    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiPredicate<Object, AnActionEvent> serviceCondition = (r, e) -> r instanceof AzureContainerService;
        final BiConsumer<Object, AnActionEvent> handler = (c, e) -> CreateKubernetesServiceAction.create(e.getProject(), getDefaultConfig(null));
        am.registerHandler(ResourceCommonActionsContributor.CREATE, serviceCondition, handler);

        final BiConsumer<ResourceGroup, AnActionEvent> groupCreateHandler = (r, e) ->
                CreateKubernetesServiceAction.create(e.getProject(), getDefaultConfig(r));
        am.registerHandler(ContainerServiceActionsContributor.GROUP_CREATE_KUBERNETES_SERVICE, (r, e) -> true, groupCreateHandler);

        final BiPredicate<KubernetesCluster, AnActionEvent> clusterCondition = (r, e) -> r instanceof KubernetesCluster;
        am.registerHandler(ContainerServiceActionsContributor.GET_CREDENTIAL_USER, clusterCondition, (c, e) ->
                GetKubuCredentialAction.getKubuCredential(c, e.getProject(), false));
        am.registerHandler(ContainerServiceActionsContributor.GET_CREDENTIAL_ADMIN, clusterCondition, (c, e) ->
                GetKubuCredentialAction.getKubuCredential(c, e.getProject(), true));
        am.registerHandler(ContainerServiceActionsContributor.DOWNLOAD_CONFIG_USER, clusterCondition, (c, e) ->
                DownloadKubuConfigAction.downloadKubuConfig(c, e.getProject(), false));
        am.registerHandler(ContainerServiceActionsContributor.DOWNLOAD_CONFIG_ADMIN, clusterCondition, (c, e) ->
                DownloadKubuConfigAction.downloadKubuConfig(c, e.getProject(), true));
    }

    @Override
    public int getOrder() {
        return ContainerServiceActionsContributor.INITIALIZE_ORDER + 1;
    }

}
