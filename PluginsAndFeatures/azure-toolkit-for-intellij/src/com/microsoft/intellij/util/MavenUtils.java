/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.util;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.common.exceptions.AzureExecutionException;
import com.microsoft.intellij.maven.SpringCloudDependencyManager;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenExplicitProfiles;
import org.jetbrains.idea.maven.project.*;
import org.jetbrains.idea.maven.server.MavenEmbedderWrapper;
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException;

import java.nio.file.Paths;
import java.util.concurrent.*;

public class MavenUtils {
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    public static String getSpringBootFinalJarPath(@NotNull Project ideaProject, @NotNull MavenProject mavenProject)
            throws AzureExecutionException, DocumentException, MavenProcessCanceledException {
        String xml = evaluateEffectivePom(ideaProject, mavenProject);
        if (StringUtils.isEmpty(xml)) {
            throw new AzureExecutionException("Failed to evaluate effective pom for project: " + ideaProject.getName());
        }
        SpringCloudDependencyManager manager = new SpringCloudDependencyManager(xml);
        String finalName = manager.getPluginConfiguration("org.springframework.boot", "spring-boot-maven"
                + "-plugin", "finalName");
        if (StringUtils.isEmpty(finalName)) {
            finalName = mavenProject.getFinalName();
        }

        if (StringUtils.isEmpty(finalName)) {
            throw new AzureExecutionException("Failed to evaluate <finalName> for project: " + ideaProject.getName());
        }
        return Paths.get(mavenProject.getBuildDirectory(), finalName + "." + mavenProject.getPackaging()).toString();
    }

    public static String evaluateEffectivePom(@NotNull Project ideaProject,
                                              @NotNull MavenProject mavenProject) throws MavenProcessCanceledException {
        final MavenProjectsManager projectsManager = MavenProjectsManager.getInstance(ideaProject);

        MavenEmbeddersManager embeddersManager = projectsManager.getEmbeddersManager();
        MavenExplicitProfiles profiles = mavenProject.getActivatedProfilesIds();
        MavenEmbedderWrapper embedder = embeddersManager.getEmbedder(mavenProject,
                                                                     MavenEmbeddersManager.FOR_DEPENDENCIES_RESOLVE);
        embedder.clearCachesFor(mavenProject.getMavenId());
        return embedder.evaluateEffectivePom(mavenProject.getFile(), profiles.getEnabledProfiles(), profiles.getDisabledProfiles());
    }
}
