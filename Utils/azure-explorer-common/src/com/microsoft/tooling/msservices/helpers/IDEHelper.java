/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.helpers;

import java.io.IOException;
import java.util.List;

import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azuretools.azurecommons.helpers.AzureCmdException;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.azuretools.azurecommons.tasks.CancellableTask;

public interface IDEHelper {
    class ProjectDescriptor {
        @NotNull
        private final String name;
        @NotNull
        private final String path;

        public ProjectDescriptor(@NotNull String name, @NotNull String path) {
            this.name = name;
            this.path = path;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @NotNull
        public String getPath() {
            return path;
        }
    }

    class ArtifactDescriptor {
        @NotNull
        private String name;
        @NotNull
        private String artifactType;

        public ArtifactDescriptor(@NotNull String name, @NotNull String artifactType) {
            this.name = name;
            this.artifactType = artifactType;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @NotNull
        public String getArtifactType() {
            return artifactType;
        }
    }

    String getProjectSettingsPath();

    void closeFile(@NotNull Object projectObject, @NotNull Object openedFile);

    void invokeLater(@NotNull Runnable runnable);

    void invokeAndWait(@NotNull Runnable runnable);

    void executeOnPooledThread(@NotNull Runnable runnable);

    void runInBackground(@Nullable Object project, @NotNull String name, boolean canBeCancelled,
                         boolean isIndeterminate, @Nullable String indicatorText,
                         Runnable runnable);

    @NotNull
    CancellableTask.CancellableTaskHandle runInBackground(@NotNull ProjectDescriptor projectDescriptor,
                                                          @NotNull String name,
                                                          @Nullable String indicatorText,
                                                          @NotNull CancellableTask cancellableTask) throws AzureCmdException;

    @Nullable
    String getProperty(@NotNull String name);

    @NotNull
    String getProperty(@NotNull String name, Object projectObject);

    @NotNull
    String getPropertyWithDefault(@NotNull String name, @NotNull String defaultValue);

    void setProperty(@NotNull String name, @NotNull String value);

    void setProperty(@NotNull String name, @NotNull String value, Object projectObject);

    void unsetProperty(@NotNull String name);

    boolean isPropertySet(@NotNull String name);

    void unsetProperty(@NotNull String name, Object projectObject);

    @Nullable
    String[] getProperties(@NotNull String name);

    @Nullable
    String[] getProperties(@NotNull String name, Object projectObject);

    void setProperties(@NotNull String name, @NotNull String[] value);

    @NotNull
    List<ArtifactDescriptor> getArtifacts(@NotNull ProjectDescriptor projectDescriptor)
            throws AzureCmdException;

    @NotNull
    ListenableFuture<String> buildArtifact(@NotNull ProjectDescriptor projectDescriptor,
                                           @NotNull ArtifactDescriptor artifactDescriptor);

    @NotNull
    Object getCurrentProject();

    void setApplicationProperty(@NotNull String name, @NotNull String value);

    void unsetApplicationProperty(@NotNull String name);

    @Nullable
    String getApplicationProperty(@NotNull String name);

    void setApplicationProperties(@NotNull String name, @NotNull String[] value);

    void unsetApplicatonProperties(@NotNull String name);

    @Nullable
    String[] getApplicationProperties(@NotNull String name);

    boolean isApplicationPropertySet(@NotNull String name);

    void openLinkInBrowser(@NotNull String url);

}
