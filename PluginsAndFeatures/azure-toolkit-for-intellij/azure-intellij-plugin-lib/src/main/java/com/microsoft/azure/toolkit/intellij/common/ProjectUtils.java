/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common;

import com.intellij.openapi.externalSystem.service.execution.ExternalSystemJdkProvider;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemJdkUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.openapi.wm.impl.ProjectFrameHelper;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectUtils {

    @SuppressWarnings("UnstableApiUsage")
    public static Window getWindow(@Nullable Project project) {
        final WindowManagerEx manager = WindowManagerEx.getInstanceEx();
        Window window = manager.suggestParentWindow(project);
        if (window == null) {
            window = manager.getMostRecentFocusedWindow();
        }
        if (window == null) {
            for (final ProjectFrameHelper frameHelper : manager.getProjectFrameHelpers()) {
                final IdeFrameImpl frame = frameHelper.getFrame();
                if (frame != null && frame.isActive()) {
                    window = frameHelper.getFrame();
                    break;
                }
            }
        }
        if (window == null) {
            window = JOptionPane.getRootFrame();
        }
        return window;
    }

    @Nonnull
    @SuppressWarnings("UnstableApiUsage")
    public static Project getProject() {
        Project project = null;
        final WindowManagerEx manager = WindowManagerEx.getInstanceEx();
        for (final ProjectFrameHelper frameHelper : manager.getProjectFrameHelpers()) {
            final IdeFrameImpl frame = frameHelper.getFrame();
            if (frame != null && frame.isActive()) {
                project = frameHelper.getProject();
            }
        }
        if (project == null) {
            project = ProjectManager.getInstance().getDefaultProject();
        }
        return project;
    }

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

    public static boolean isSameModule(@Nullable final Module first, @Nullable final Module second) {
        return Objects.equals(first, second) || StringUtils.equals(getFormalModuleId(first), getFormalModuleId(second));
    }

    private static String getFormalModuleId(@Nullable Module module) {
        return Optional.ofNullable(module).map(Module::getName)
            .map(name -> StringUtils.removeEnd(name, ".main"))
            .map(name -> StringUtils.removeEnd(name, ".test")).orElse(null);
    }

    public static @Nullable Integer getJavaVersion(@Nonnull Module module) {
        Sdk jdk = ProjectUtils.getJdk(module);
        if (jdk == null) {
            jdk = getJdk(module.getProject());
        }
        return getJavaVersion(jdk);
    }

    public static @Nullable Integer getJavaVersion(@Nonnull Project project) {
        final Sdk jdk = ProjectUtils.getJdk(project);
        return getJavaVersion(jdk);
    }

    @Nullable
    public static Integer getJavaVersion(@Nonnull Sdk jdk) {
        final String str = jdk.getVersionString();
        if (StringUtils.isNotBlank(str)) {
            final Matcher matcher = Pattern.compile("\\d+\\.\\d+(\\.\\d+)*").matcher(str);
            if (matcher.find()) {
                final String versionStr = matcher.group(0);
                final String[] parts = versionStr.split("\\.");
                final int majorVersion = Integer.parseInt(parts[0]);
                return majorVersion == 1 ? Integer.parseInt(parts[1]) : majorVersion;
            }
        }
        return null;
    }
}
