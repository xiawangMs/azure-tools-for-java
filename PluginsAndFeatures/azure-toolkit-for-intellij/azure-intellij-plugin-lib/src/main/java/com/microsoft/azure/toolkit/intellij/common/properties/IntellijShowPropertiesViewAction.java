/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common.properties;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.IntelliJAzureIcons;
import com.microsoft.azure.toolkit.intellij.common.properties.AzureResourceEditorViewManager.AzureResourceFileType;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.model.AzResourceBase;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class IntellijShowPropertiesViewAction {
    private static final AzureResourceEditorViewManager manager = new AzureResourceEditorViewManager((resource) -> {
        final Icon icon = getFileTypeIcon(resource);
        final String name = getFileTypeName(resource);
        return new AzureResourceFileType(name, icon);
    });

    public static void showPropertyView(@Nonnull AzResourceBase resource, @Nonnull Project project) {
        manager.showEditor(resource, project);
    }

    public static void closePropertiesView(@Nonnull AzResourceBase resource, @Nonnull Project project) {
        manager.closeEditor(resource, project);
    }

    private static String getFileTypeName(@Nonnull AzResourceBase resource) {
        if (resource instanceof AzResource) {
            return getNewFileTypeName((AzResource<?, ?>) resource);
        }
        return String.format("%s_FILE_TYPE", resource.getClass().getSimpleName().toUpperCase());
    }

    private static Icon getFileTypeIcon(@Nonnull AzResourceBase resource) {
        if (resource instanceof AzResource) {
            return IntelliJAzureIcons.getIcon(getNewFileTypeIcon((AzResource<?, ?>) resource));
        }
        return IntelliJAzureIcons.getIcon(String.format("/icons/%s.svg", resource.getClass().getSimpleName().toLowerCase()));
    }

    @Nonnull
    private static String getNewFileTypeIcon(AzResource<?, ?> resource) {
        final String status = resource.getStatus();
        AzResource<?, ?> current = resource;
        final StringBuilder modulePath = new StringBuilder();
        while (!(current instanceof AzResource.None)) {
            modulePath.insert(0, "/" + current.getModule().getName());
            current = current.getParent();
        }
        return String.format("/icons%s/default.svg", modulePath);
    }

    @Nonnull
    private static String getNewFileTypeName(AzResource<?, ?> resource) {
        AzResource<?, ?> current = resource;
        final List<String> modules = new ArrayList<>();
        while (!(current instanceof AzResource.None)) {
            modules.add(0, current.getModule().getName());
            current = current.getParent();
        }
        return String.join(".", modules);
    }
}
