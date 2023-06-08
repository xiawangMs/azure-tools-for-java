/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.facet.projectexplorer;

import com.intellij.openapi.actionSystem.DataProvider;
import com.microsoft.azure.toolkit.lib.common.action.IActionGroup;

import javax.annotation.Nullable;

public interface IAzureFacetNode extends DataProvider {
    @Nullable
    default IActionGroup getActionGroup() {
        return null;
    }

    default void onClicked(Object event) {

    }

    default void onDoubleClicked(Object event) {

    }
}
