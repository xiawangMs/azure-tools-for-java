/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common.utils;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemJdkProvider;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemJdkUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.JavaSdkVersion;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.util.lang.JavaVersion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class JdkUtils {

    @Nullable
    public static Sdk getJdk(@Nonnull Module module) {
        final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
        if (sdk != null && sdk.getSdkType() instanceof JavaSdkType) return sdk;
        return null;
    }

    /**
     * Get project jdk from project or module.
     * reference: com.intellij.openapi.externalSystem.service.execution.ExternalSystemJdkUtil#getProjectJdk
     *
     * @param project project
     * @return project jdk
     */
    @Nonnull
    @SuppressWarnings("UnstableApiUsage")
    public static Sdk getJdk(@Nullable Project project) {
        if (project != null) {
            final Sdk res = ProjectRootManager.getInstance(project).getProjectSdk();
            if (res != null) return res;

            final Module[] modules = ModuleManager.getInstance(project).getModules();
            for (final Module module : modules) {
                final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
                if (sdk != null && sdk.getSdkType() instanceof JavaSdkType) return sdk;
            }
        }

        // Workaround for projects without project Jdk
        final SdkType jdkType = ExternalSystemJdkProvider.getInstance().getJavaSdkType();
        return ProjectJdkTable.getInstance()
            .getSdksOfType(jdkType).stream()
            .filter(ExternalSystemJdkUtil::isValidJdk)
            .max(jdkType.versionComparator())
            .orElseGet(() -> ExternalSystemJdkProvider.getInstance().getInternalJdk());
    }

    @Nullable
    public static Integer getJdkLanguageLevel(@Nonnull Module module) {
        Sdk jdk = JdkUtils.getJdk(module);
        if (jdk == null) {
            jdk = getJdk(module.getProject());
        }
        return getJdkLanguageLevel(jdk);
    }

    @Nullable
    public static Integer getJdkLanguageLevel(@Nonnull Project project) {
        final Sdk jdk = JdkUtils.getJdk(project);
        return getJdkLanguageLevel(jdk);
    }

    @Nullable
    public static Integer getJdkLanguageLevel(@Nonnull Sdk jdk) {
        final JavaSdkVersion version = JavaSdk.getInstance().getVersion(jdk);
        if (Objects.nonNull(version)) {
            final LanguageLevel level = version.getMaxLanguageLevel();
            return level.toJavaVersion().feature;
        }
        final String str = jdk.getVersionString();
        if (StringUtils.isNotBlank(str)) {
            final Matcher matcher = Pattern.compile("\\d+(\\.\\d+)*").matcher(str);
            if (matcher.find()) {
                final String versionStr = matcher.group(0);
                return JavaVersion.parse(versionStr).feature;
            }
        }
        return null;
    }

    @Nullable
    public static Integer getTargetBytecodeLanguageLevel(@Nonnull Module module) {
        final CompilerConfiguration config = CompilerConfiguration.getInstance(module.getProject());
        final String level = config.getBytecodeTargetLevel(module);
        if (StringUtils.isNotBlank(level)) {
            return JavaVersion.parse(level).feature;
        }
        return null;
    }

    /**
     * copy from Utils#getArtifactCompileVersion
     */
    @Nullable
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Integer getBytecodeLanguageLevel(@Nonnull File artifact) {
        try (final JarFile jarFile = new JarFile(artifact)) {
            final Manifest manifest = jarFile.getManifest();
            final String startClass = manifest.getMainAttributes().getValue("Start-Class");
            final String mainClass = manifest.getMainAttributes().getValue("Main-Class");
            final String target = StringUtils.isNotBlank(startClass) ? getJarEntryName("BOOT-INF/classes/" + startClass) :
                StringUtils.isNotBlank(mainClass) ? getJarEntryName(mainClass) : null;
            final JarEntry jarEntry = StringUtils.isNotBlank(target) ? jarFile.getJarEntry(target) : jarFile.stream()
                .filter(entry -> StringUtils.endsWith(entry.getName(), ".class"))
                .findFirst().orElse(null);
            if (Objects.isNull(jarEntry)) {
                log.warn("Failed to parse artifact compile version, no class file founded in target artifact");
                return null;
            }
            // Read compile version from class file
            // Refers https://en.wikipedia.org/wiki/Java_class_file#General_layout
            final InputStream stream = jarFile.getInputStream(jarEntry);
            final byte[] version = new byte[2];
            stream.skip(6);
            stream.read(version);
            stream.close();
            return new BigInteger(version).intValueExact() - 44;
        } catch (final IOException e) {
            log.warn(String.format("Failed to parse artifact compile version: %s", e.getMessage()));
            return null;
        }
    }

    @Nonnull
    private static String getJarEntryName(@Nonnull final String className) {
        final String fullName = StringUtils.replace(className, ".", "/");
        return fullName + ".class";
    }
}
