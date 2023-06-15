/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.common.genericresource;

import com.microsoft.azure.toolkit.ide.common.component.AzResourceNode;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcons;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;

import javax.annotation.Nonnull;

public class GenericResourceNode extends AzResourceNode<AzResource> {

    public GenericResourceNode(@Nonnull AzResource resource) {
        super(resource);
        this.withIcon(r -> r.getFormalStatus().isWaiting() ? AzureIcons.Common.REFRESH_ICON : AzureIcons.Resources.GENERIC_RESOURCE)
            .withLabel(AzResource::getName)
            .withDescription(r -> (r.getFormalStatus().isUnknown() ? "" : resource.getStatus() + " ") + resource.getResourceTypeName());
    }
}
