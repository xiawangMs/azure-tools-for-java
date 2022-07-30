/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.externalSystem.model.project.ExternalProjectPojo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packaging.artifacts.Artifact;
import com.microsoft.intellij.util.MavenUtils;
import icons.GradleIcons;
import icons.MavenIcons;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;

import javax.annotation.Nullable;
import javax.swing.*;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

public class AzureArtifact {
    private final String name;
    private final AzureArtifactType type;
    private final Object referencedObject;

    private AzureArtifact(final AzureArtifactType type, final String name, Object obj) {
        this.type = type;
        this.name = name;
        this.referencedObject = obj;
    }

    @Nullable
    public static AzureArtifact createFromFile(@Nullable String path) {
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        return Optional.ofNullable(LocalFileSystem.getInstance().findFileByPath(path))
                .map(AzureArtifact::createFromFile).orElse(null);
    }

    public static AzureArtifact createFromFile(@NotNull VirtualFile virtualFile) {
        return new AzureArtifact(AzureArtifactType.File, virtualFile.getName(), virtualFile);
    }

    public static AzureArtifact createFromArtifact(@NotNull Artifact artifact) {
        return new AzureArtifact(AzureArtifactType.Artifact,
                                 artifact.getName(),
                                 artifact);
    }

    public static AzureArtifact createFromMavenProject(MavenProject mavenProject) {
        return new AzureArtifact(AzureArtifactType.Maven,
                                 mavenProject.toString(),
                                 mavenProject);
    }

    public static AzureArtifact createFromGradleProject(ExternalProjectPojo projectPojo) {
        return new AzureArtifact(AzureArtifactType.Gradle, projectPojo.getName(), projectPojo);
    }

    public Icon getIcon() {
        switch (type) {
            case Gradle:
                return GradleIcons.Gradle;
            case Maven:
                return MavenIcons.MavenProject;
            case Artifact:
                return ((Artifact) referencedObject).getArtifactType().getIcon();
            case File:
                return AllIcons.FileTypes.Archive;
            default:
                return null;
        }
    }

    public String getName() {
        return name;
    }

    public AzureArtifactType getType() {
        return type;
    }

    public Object getReferencedObject() {
        return referencedObject;
    }

    @Override
    public String toString() {
        return Objects.toString(name);
    }

    public String getTargetPath() {
        switch (type) {
            case Gradle:
                // it is not required to get a final output jar in getTargetPath
                return Paths.get(((ExternalProjectPojo) referencedObject).getPath(),
                                 String.format("build/lib/%s.jar", getName())).toString();
            case Maven:
                return MavenUtils.getTargetFile((MavenProject) referencedObject);
            case Artifact:
                return ((Artifact) referencedObject).getOutputFilePath();
            case File:
                return ((VirtualFile) referencedObject).getPath();
            default:
                throw new RuntimeException(String.format("Invalid type '%s' for AzureArtifact", type));
        }
    }
}
