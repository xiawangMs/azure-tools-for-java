/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerregistry;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.containerregistry.ContainerRegistryActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;

public class IntelliJContainerRegistryActionsContributor implements IActionsContributor {
    @Override
    public void registerHandlers(AzureActionManager am) {

    }

    @Override
    public int getOrder() {
        return ContainerRegistryActionsContributor.INITIALIZE_ORDER + 1;
    }
}
