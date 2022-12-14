/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerapps.component;

import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ContainerRegistryTypeComboBox extends AzureComboBox<String> {

    public static final String ACR = "Azure Container Registries";
    public static final String DOCKER_HUB = "Docker Hub Registry";
    public static final String OTHER = "Other public Registry";

    @Override
    public String getLabel() {
        return "Registry Type";
    }

    @Nonnull
    @Override
    protected List<? extends String> loadItems() {
        return Arrays.asList(ACR, DOCKER_HUB, OTHER);
    }

    @Nonnull
    @Override
    protected List<ExtendableTextComponent.Extension> getExtensions() {
        return Collections.emptyList();
    }
}
