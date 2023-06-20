/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Optional;

public class AzureProjectFacetConfiguration implements FacetConfiguration, PersistentStateComponent<AzureProjectFacetConfiguration.AzureProjectFacetState> {
    @Getter
    @Nonnull
    private AzureProjectFacetState state;

    public AzureProjectFacetConfiguration() {
        this.state = new AzureProjectFacetState();
    }

    public AzureProjectFacetConfiguration(@Nonnull final VirtualFile dotAzureFile) {
        this.state = new AzureProjectFacetState(dotAzureFile.toNioPath().toString());
    }

    @Nonnull
    @Override
    public AzureProjectFacetState getState() {
        return state;
    }

    @Override
    public void loadState(@Nonnull AzureProjectFacetState state) {
        this.state = state;
    }

    @Nullable
    public VirtualFile getDotAzureDir() {
        return Optional.ofNullable(this.state.dotAzurePath)
            .filter(StringUtils::isNotBlank).map(Path::of)
            .map(p -> VfsUtil.findFile(p, true)).orElse(null);
    }

    public void setDotAzureDir(final VirtualFile dotAzureDir) {
        this.state.setDotAzurePath(Optional.ofNullable(dotAzureDir).map(VirtualFile::getPath).orElse(null));
    }

    @Override
    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        return new FacetEditorTab[]{
            new AzureProjectFacetEditorTab(this.state, editorContext, validatorsManager)
        };
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AzureProjectFacetState {
        @Nullable
        private String dotAzurePath;
    }
}
