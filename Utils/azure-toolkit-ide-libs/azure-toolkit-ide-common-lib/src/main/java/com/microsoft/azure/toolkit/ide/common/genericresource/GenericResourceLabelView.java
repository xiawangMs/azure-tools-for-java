/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.common.genericresource;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceLabelView;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcons;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;

import javax.annotation.Nonnull;

public class GenericResourceLabelView<T extends AbstractAzResource<?, ?>> extends AzureResourceLabelView<T> {

    private final ResourceId resourceId;

    public GenericResourceLabelView(@Nonnull T resource) {
        super(resource,
            r -> (r.getFormalStatus().isUnknown() ? "" : resource.getStatus() + " ") + resource.getResourceTypeName(),
            r -> r.getFormalStatus().isWaiting() ? AzureIcons.Common.REFRESH_ICON : AzureIcons.Resources.GENERIC_RESOURCE);
        this.resourceId = ResourceId.fromString(resource.getId());
    }

    @Override
    public String getLabel() {
        return this.resourceId.name();
    }
}
