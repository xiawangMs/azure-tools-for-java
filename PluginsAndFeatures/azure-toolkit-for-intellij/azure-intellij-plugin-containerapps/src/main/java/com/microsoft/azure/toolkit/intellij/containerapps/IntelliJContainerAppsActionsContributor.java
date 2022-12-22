/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerapps;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.containerapps.ContainerAppsActionsContributor;
import com.microsoft.azure.toolkit.intellij.containerapps.updateimage.UpdateContainerImageAction;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerApp;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class IntelliJContainerAppsActionsContributor implements IActionsContributor {
    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiPredicate<ContainerApp, AnActionEvent> serviceCondition = (r, e) -> r != null;
        am.registerHandler(ContainerAppsActionsContributor.UPDATE_IMAGE, UpdateContainerImageAction::openUpdateDialog);
    }

    @Override
    public int getOrder() {
        return ContainerAppsActionsContributor.INITIALIZE_ORDER + 1;
    }
}
