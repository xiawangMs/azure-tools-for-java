/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.function.coretools;

import com.microsoft.azure.toolkit.ide.common.store.AzureConfigInitializer;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FunctionsCoreToolsManager {
    private ReleaseInfo releaseInfoCache;
    private final String AZURE_FUNCTIONS = "AzureFunctions";
    private final String INSTALL_FAILED_MESSAGE = "failed to download and install functions core tools.";
    private final String RELEASE_TAG = "v4";
    public final static String DEFAULT_FUNCTIONS_CORE_TOOLS_DOWNLOAD_PATH = Paths.get(System.getProperty("user.home"), ".azure-functions-core-tools").toString();
    private static final FunctionsCoreToolsManager instance = new FunctionsCoreToolsManager();
    private FuncCoreToolsDownloadListener listener;
    public static FunctionsCoreToolsManager getInstance() {
        return instance;
    }

    public void downloadReleaseWithFilter(String downloadDirPath, FuncCoreToolsDownloadListener listener) {
        this.listener = listener;
        if (Objects.isNull(releaseInfoCache)) {
            cacheReleaseInfoFromFeed();
        }
        doDownloadRelease(releaseInfoCache, downloadDirPath);
    }

    /**
     * refer to https://github.com/JetBrains/azure-tools-for-intellij
     * */
    private void cacheReleaseInfoFromFeed() {
        final ReleaseFilter releaseFilter = generateFilter();
        final ReleaseFeedData releaseFeedData = ReleaseService.getInstance().getReleaseFeedData();
        Optional.ofNullable(releaseFeedData).ifPresent(data -> {
            if (!data.getTags().containsKey(RELEASE_TAG)) {
                return;
            }
            final String releaseVersion = data.getTags().get(RELEASE_TAG).getRelease();
            final ReleaseFeedData.ReleaseData releaseData = releaseFeedData.getReleases().get(releaseVersion);
            releaseData.getCoreTools().stream()
                    // Match OS and ensure a download link is present
                    .filter(it -> Objects.equals(it.getOs(), releaseFilter.os) && StringUtils.isNotBlank(it.getDownloadLink()))
                    // Sort by architecture. Use 9999 when no match is found.
                    .sorted((o1, o2) -> {
                        final int rank1 = IntStream.range(0, releaseFilter.architectures.size())
                                .filter(i -> releaseFilter.architectures.get(i).equalsIgnoreCase(o1.getArchitecture()))
                                .findFirst().orElse(9999);
                        final int rank2 = IntStream.range(0, releaseFilter.architectures.size())
                                .filter(i -> releaseFilter.architectures.get(i).equalsIgnoreCase(o2.getArchitecture()))
                                .findFirst().orElse(9999);
                        return rank1 - rank2;
                    }).min((o1, o2) -> {    // Sort by size. Use 9999 when no match is found.
                        final int rank1 = IntStream.range(0, releaseFilter.sizes.size())
                                .filter(i -> releaseFilter.sizes.get(i).equalsIgnoreCase(o1.getSize()))
                                .findFirst().orElse(9999);
                        final int rank2 = IntStream.range(0, releaseFilter.sizes.size())
                                .filter(i -> releaseFilter.sizes.get(i).equalsIgnoreCase(o2.getSize()))
                                .findFirst().orElse(9999);
                        return rank1 - rank2;
                    }).ifPresent(releaseCoreTool -> this.releaseInfoCache = new ReleaseInfo(releaseVersion.toLowerCase(), releaseCoreTool.getDownloadLink()));
        });
    }

    private void doDownloadRelease(@Nullable ReleaseInfo releaseInfo, String downloadDirPath) {
        if (Objects.isNull(releaseInfo)) {
            return;
        }
        try {
            final File tempFile = File.createTempFile(
                    String.format("%s-%s", AZURE_FUNCTIONS, releaseInfo.releaseVersion),
                    ".zip", Files.createTempDirectory(AZURE_FUNCTIONS).toFile());
            ReleaseService.getInstance().downloadZip(releaseInfo.downloadLink, tempFile);
            unzip(tempFile, Paths.get(downloadDirPath, releaseInfo.releaseVersion).toString());
        } catch (final Exception e) {
            AzureMessager.getMessager().error(e, INSTALL_FAILED_MESSAGE);
            Optional.ofNullable(listener).ifPresent(FuncCoreToolsDownloadListener::onFail);
        }
        Azure.az().config().setFunctionCoreToolsPath(Paths.get(downloadDirPath, releaseInfo.releaseVersion, "func.exe").toString());
        AzureConfigInitializer.saveAzConfig();
        Optional.ofNullable(listener).ifPresent(FuncCoreToolsDownloadListener::onSuccess);
    }

    private void unzip(File zipFile, String destDirPath) throws Exception {
        createIfNotExist(destDirPath);
        final FileInputStream fileInputStream = new FileInputStream(zipFile);
        final ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
        final byte[] buffer = new byte[1024];
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        while (zipEntry != null) {
            final File zipEntryFile = new File(destDirPath + File.separator + zipEntry.getName());
            createIfNotExist(zipEntryFile.getParent());
            final FileOutputStream fileOutputStream = new FileOutputStream(zipEntryFile);
            int len = zipInputStream.read(buffer);
            while (len > 0) {
                fileOutputStream.write(buffer, 0, len);
                len = zipInputStream.read(buffer);
            }
            fileOutputStream.close();
            zipInputStream.closeEntry();
            zipEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.closeEntry();
        zipInputStream.close();
        fileInputStream.close();
    }

    private void createIfNotExist(String dirPath) {
        final File dstDir = new File(dirPath);
        dstDir.mkdirs();
    }

    /**
     * refer to https://github.com/JetBrains/azure-tools-for-intellij
     * */
    private ReleaseFilter generateFilter() {
        final String osName = System.getProperty("os.name");
        final String architectureName = System.getProperty("os.arch");
        final boolean isIntel64 = "x86_64".equals(architectureName) || "amd64".equals(architectureName);
        final boolean isArm64 = "aarch64".equals(architectureName) || "arm64".equals(architectureName);
        if (osName.startsWith("windows") && isIntel64) {
            return new ReleaseFilter("Windows", List.of("x64"), List.of("minified", "full"));
        } else if (osName.startsWith("windows")) {
            return new FunctionsCoreToolsManager.ReleaseFilter("Windows", List.of("x86"), List.of("minified", "full"));
        } else if (osName.startsWith("mac") && isArm64) {
            return new FunctionsCoreToolsManager.ReleaseFilter("MacOS", List.of("arm64", "x64"), List.of("full"));
        } else if (osName.startsWith("mac")) {
            return new FunctionsCoreToolsManager.ReleaseFilter("MacOS", List.of("x64"), List.of("full"));
        } else if (osName.startsWith("linux")) {
            return new FunctionsCoreToolsManager.ReleaseFilter("Linux", List.of("x64"), List.of("full"));
        }
        return new FunctionsCoreToolsManager.ReleaseFilter("Unknown", List.of("x64"), List.of("full"));
    }

    private static class ReleaseInfo {
        private final String releaseVersion;
        private final String downloadLink;
        ReleaseInfo(String releaseVersion, String downloadLink) {
            this.releaseVersion = releaseVersion;
            this.downloadLink = downloadLink;
        }
    }

    public static class ReleaseFilter {
        private final String os;
        private final List<String> architectures;
        private final List<String> sizes;
        public ReleaseFilter(String os, List<String> architectures, List<String> sizes) {
            this.os = os;
            this.architectures = architectures;
            this.sizes = sizes;
        }
    }

    public static interface FuncCoreToolsDownloadListener {
        public void onSuccess();
        public void onFail();
    }

}


