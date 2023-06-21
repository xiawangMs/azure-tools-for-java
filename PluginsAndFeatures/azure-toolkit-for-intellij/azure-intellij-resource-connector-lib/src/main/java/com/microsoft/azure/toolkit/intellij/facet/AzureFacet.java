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
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.AzureModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class AzureFacet extends Facet<AzureFacetConfiguration> {

    public AzureFacet(@Nonnull FacetType facetType, @Nonnull Module module, @Nonnull String name, @Nonnull AzureFacetConfiguration configuration, Facet underlyingFacet) {
        super(facetType, module, name, configuration, underlyingFacet);
        if (configuration.getDotAzureDir() == null) {
            Optional.of(module).map(ProjectUtil::guessModuleDir).map(d -> d.findChild(".azure")).ifPresent(configuration::setDotAzureDir);
        }
    }

    public static void addTo(@Nonnull final Module module) {
        final AzureFacet facet = getInstance(module);
        if (Objects.isNull(facet)) {
            FacetManager.getInstance(module).addFacet(AzureFacetType.INSTANCE, "Azure", null);
        }
    }

    @Nonnull
    public static AzureFacet getOrAddTo(@Nonnull final Module module) {
        final AzureFacet facet = getInstance(module);
        if (Objects.isNull(facet)) {
            return FacetManager.getInstance(module).addFacet(AzureFacetType.INSTANCE, "Azure", null);
        }
        return facet;
    }

    @Nullable
    public static AzureFacet getInstance(@Nonnull final Module module) {
        return FacetManager.getInstance(module).getFacetByType(AzureFacetType.ID);
    }

    @Nullable
    public static AzureFacet getInstance(@Nonnull VirtualFile file, @Nonnull Project project) {
        final Module module = ModuleUtil.findModuleForFile(file, project);
        return Objects.isNull(module) ? null : getInstance(module);
    }

    @Nullable
    public static AzureFacet getInstance(RunConfiguration configuration) {
        return Optional.ofNullable(configuration)
            .map(AzureModule::getTargetModule)
            .map(AzureFacet::getInstance).orElse(null);
    }
}
