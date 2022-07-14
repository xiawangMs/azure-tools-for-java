/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerservice;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.containerservice.ContainerServiceActionsContributor;
import com.microsoft.azure.toolkit.intellij.containerservice.actions.DownloadKubuConfigAction;
import com.microsoft.azure.toolkit.intellij.containerservice.actions.GetKubuCredentialAction;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.containerservice.KubernetesCluster;

import java.util.function.BiPredicate;

public class IntelliJContainerServiceActionsContributor implements IActionsContributor {
    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiPredicate<KubernetesCluster, AnActionEvent> condition = (r, e) -> r instanceof KubernetesCluster;
        am.registerHandler(ContainerServiceActionsContributor.GET_CREDENTIAL_USER, condition, (c, e) ->
                GetKubuCredentialAction.getKubuCredential(c, e.getProject(), false));
        am.registerHandler(ContainerServiceActionsContributor.GET_CREDENTIAL_ADMIN, condition, (c, e) ->
                GetKubuCredentialAction.getKubuCredential(c, e.getProject(), true));
        am.registerHandler(ContainerServiceActionsContributor.DOWNLOAD_CONFIG_USER, condition, (c, e) ->
                DownloadKubuConfigAction.downloadKubuConfig(c, e.getProject(), false));
        am.registerHandler(ContainerServiceActionsContributor.DOWNLOAD_CONFIG_ADMIN, condition, (c, e) ->
                DownloadKubuConfigAction.downloadKubuConfig(c, e.getProject(), true));
    }

    @Override
    public int getOrder() {
        return ContainerServiceActionsContributor.INITIALIZE_ORDER + 1;
    }

}
