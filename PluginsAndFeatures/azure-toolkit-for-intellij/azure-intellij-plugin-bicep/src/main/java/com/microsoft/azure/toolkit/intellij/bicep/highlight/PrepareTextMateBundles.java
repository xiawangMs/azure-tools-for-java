/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.bicep.highlight;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.microsoft.azure.toolkit.lib.common.exception.SystemException;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.messager.ExceptionNotification;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.microsoft.azure.toolkit.intellij.bicep.highlight.BicepEditorHighlighterProvider.LIB_ROOT;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class PrepareTextMateBundles implements StartupActivity.DumbAware {
    public static final String TEXTMATE_ZIP = "/textmate.zip";
    protected static final Logger LOG = Logger.getInstance(PrepareTextMateBundles.class);

    @Override
    @ExceptionNotification
    @AzureOperation(name = "platform/bicep.prepare_textmate_bundles")
    public void runActivity(@Nonnull Project project) {
        final File textmateDir = new File(LIB_ROOT + File.separator + "textmate");
        if (!textmateDir.exists()) {
            final URL textmateZipUrl = PrepareTextMateBundles.class.getResource(TEXTMATE_ZIP);
            if (textmateZipUrl != null) {
                try {
                    final String textmateZipPath = this.copyTextMateBundlesFromJar(textmateZipUrl);
                    this.unzip(textmateZipPath);
                } catch (IOException e) {
                    LOG.error(e);
                    textmateDir.delete();
                    AzureMessager.getMessager().error(e);
                }
            }
            throw new SystemException("'textmate.zip' not found in jar");
        }
    }

    @AzureOperation("boundary/bicep.copy_textmate_bundle_from_jar")
    private String copyTextMateBundlesFromJar(URL textmateZipUrl) throws IOException {
        final String destZipFile = LIB_ROOT + TEXTMATE_ZIP;
        FileUtils.copyURLToFile(textmateZipUrl, new File(destZipFile));
        return destZipFile;
    }

    @AzureOperation(value = "boundary/bicep.unzip_textmate_bundle.zip", params = "zipFilePath")
    private void unzip(@Nonnull String zipFilePath) throws IOException {
        try (final java.util.zip.ZipFile zipFile = new ZipFile(zipFilePath)) {
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                final File entryDestination = new File(BicepEditorHighlighterProvider.LIB_ROOT, entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    try (final InputStream in = zipFile.getInputStream(entry);
                         final OutputStream out = new FileOutputStream(entryDestination)) {
                        IOUtils.copy(in, out);
                    }
                }
            }
        }
    }
}
