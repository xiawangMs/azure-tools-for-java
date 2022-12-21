/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.bicep.highlight;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.PluginPathManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.lib.common.exception.SystemException;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.intellij.CommonConst;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.plugins.textmate.TextMateService;
import org.jetbrains.plugins.textmate.configuration.BundleConfigBean;
import org.jetbrains.plugins.textmate.configuration.TextMateSettings;
import org.jetbrains.plugins.textmate.configuration.TextMateSettings.TextMateSettingsState;
import org.jetbrains.plugins.textmate.language.TextMateLanguageDescriptor;
import org.jetbrains.plugins.textmate.language.syntax.highlighting.TextMateEditorHighlighterProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.locks.Lock;

public class BicepEditorHighlighterProvider extends TextMateEditorHighlighterProvider {
    @Override
    @AzureOperation("platform/bicep.get_editor_highlighter")
    public EditorHighlighter getEditorHighlighter(@Nullable Project project, @Nonnull FileType fileType, @Nullable VirtualFile virtualFile, @Nonnull EditorColorsScheme colors) {
        final TextMateLanguageDescriptor descriptor = TextMateService.getInstance().getLanguageDescriptorByExtension("bicep");
        if (Objects.isNull(descriptor)) { // register textmate if not registered
            registerBicepTextMateBundleAndReload();
        }
        return super.getEditorHighlighter(project, fileType, virtualFile, colors);
    }

    @AzureOperation("boundary/bicep.register_textmate_bundles")
    private static synchronized void registerBicepTextMateBundleAndReload() {
        final TextMateSettingsState state = TextMateSettings.getInstance().getState();
        try {
            if (Objects.nonNull(state)) {
                final Lock registrationLock = (Lock) FieldUtils.readField(TextMateService.getInstance(), "myRegistrationLock", true);
                try {
                    registrationLock.lock();
                    final Collection<BundleConfigBean> bundles = state.getBundles();
                    final ArrayList<BundleConfigBean> newBundles = new ArrayList<>(bundles);
                    newBundles.removeIf(bundle -> StringUtils.equalsAnyIgnoreCase(bundle.getName(), "bicep", "bicepparam"));
                    newBundles.add(new BundleConfigBean("bicep", Path.of(CommonConst.PLUGIN_PATH, "bicep", "textmate", "bicep").toString(), true));
                    newBundles.add(new BundleConfigBean("bicepparam", Path.of(CommonConst.PLUGIN_PATH, "bicep", "textmate", "bicepparam").toString(), true));
                    state.setBundles(newBundles);
                    TextMateService.getInstance().reloadEnabledBundles();
                } finally {
                    registrationLock.unlock();
                }
            }
        } catch (final IllegalAccessException e) {
            throw new SystemException("can not acquire lock of 'TextMateService'.", e);
        }
    }
}
