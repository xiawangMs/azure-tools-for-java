/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.facet;

import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.AzureModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class AzureProjectFacet extends Facet<AzureProjectFacetConfiguration> {

    public AzureProjectFacet(@Nonnull FacetType facetType, @Nonnull Module module, @Nonnull String name, @Nonnull AzureProjectFacetConfiguration configuration, Facet underlyingFacet) {
        super(facetType, module, name, configuration, underlyingFacet);
    }

    public static void addTo(@Nonnull final Module module) {
        final AzureProjectFacet facet = getInstance(module);
        if (Objects.isNull(facet)) {
            FacetManager.getInstance(module).addFacet(AzureProjectFacetType.INSTANCE, "Azure Resource Connections", null);
        }
    }

    @Nonnull
    public static AzureProjectFacet getOrAddTo(@Nonnull final Module module) {
        final AzureProjectFacet facet = getInstance(module);
        if (Objects.isNull(facet)) {
            return FacetManager.getInstance(module).addFacet(AzureProjectFacetType.INSTANCE, "Azure Resource Connections", null);
        }
        return facet;
    }

    @Nullable
    public static AzureProjectFacet getInstance(@Nonnull final Module module) {
        return FacetManager.getInstance(module).getFacetByType(AzureProjectFacetType.ID);
    }

    @Nullable
    public static AzureProjectFacet getInstance(@Nonnull VirtualFile file, @Nonnull Project project) {
        final Module module = ModuleUtil.findModuleForFile(file, project);
        return Objects.isNull(module) ? null : getInstance(module);
    }

    @Nullable
    public static AzureProjectFacet getInstance(RunConfiguration configuration) {
        return Optional.ofNullable(configuration)
            .map(AzureModule::getTargetModule)
            .map(AzureProjectFacet::getInstance).orElse(null);
    }
}
