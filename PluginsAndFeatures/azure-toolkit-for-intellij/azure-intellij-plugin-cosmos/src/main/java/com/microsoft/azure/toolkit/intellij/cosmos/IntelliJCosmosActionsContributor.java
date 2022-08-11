/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.cosmos;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;

public class IntelliJCosmosActionsContributor implements IActionsContributor {
    @Override
    public void registerHandlers(AzureActionManager am) {
        IActionsContributor.super.registerHandlers(am);
    }
}
