/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.vm.creation.component;

import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.compute.security.AzureNetworkSecurityGroup;
import com.microsoft.azure.toolkit.lib.compute.security.NetworkSecurityGroup;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SecurityGroupComboBox extends AzureComboBox<NetworkSecurityGroup> {
    private Region region;
    private Subscription subscription;

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
        this.refreshItems();
    }

    public void setRegion(Region region) {
        this.region = region;
        this.refreshItems();
    }

    @Override
    protected String getItemText(Object item) {
        return item instanceof NetworkSecurityGroup ? ((NetworkSecurityGroup) item).name() : super.getItemText(item);
    }

    @Nonnull
    @Override
    protected List<? extends NetworkSecurityGroup> loadItems() throws Exception {
        return subscription == null ? Collections.emptyList() : Azure.az(AzureNetworkSecurityGroup.class).subscription(subscription.getId()).list().stream()
                .filter(group -> Objects.equals(group.getRegion(), region)).collect(Collectors.toList());
    }

    public void setData(NetworkSecurityGroup networkSecurityGroup) {
        setValue(new ItemReference<>(resource -> StringUtils.equals(resource.name(), networkSecurityGroup.name()) &&
                StringUtils.equals(resource.resourceGroup(), networkSecurityGroup.resourceGroup())));
    }
}
