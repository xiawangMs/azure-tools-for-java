/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.common.dotnet;

import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.common.store.AzureConfigInitializer;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.utils.CommandUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.microsoft.azure.toolkit.lib.common.utils.CommandUtils.exec;

public class DotnetRuntimeHandler {

    public static final String SCRIPT_BASE_URL = "https://dot.net/v1/dotnet-install.";
    public static final String WINDOWS_INSTALL_COMMAND = "powershell.exe -NoProfile -ExecutionPolicy unrestricted -Command \"& { " +
            "[Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12 ; & %s }\"";
    public static final String VERSION = "6.0.9";

    public static String getDotnetVersion() {
        final String dotnetRuntimePath = Azure.az().config().getDotnetRuntimePath();
        try {
            return exec("dotnet --version", dotnetRuntimePath);
        } catch (IOException e) {
            // swallow exception to get version
            return null;
        }
    }

    @Nullable
    public static String getDotnetRuntimePath() {
        final List<String> paths = CommandUtils.resolveCommandPath("dotnet");
        return CollectionUtils.isEmpty(paths) ? null : Paths.get(paths.get(0)).getParent().toString();
    }

    public static void installDotnet(final String path) {
        final String rawCommand = getInstallCommand(VERSION, path);
        final String installCommand = SystemUtils.IS_OS_WINDOWS ? String.format(WINDOWS_INSTALL_COMMAND, rawCommand) : rawCommand;
        final Action<Object> openSettingsAction = AzureActionManager.getInstance().getAction(ResourceCommonActionsContributor.OPEN_AZURE_SETTINGS);
        try {
            CommandUtils.exec(installCommand);
            Azure.az().config().setDotnetRuntimePath(path);
            AzureConfigInitializer.saveAzConfig();
            final String INSTALL_SUCCEED_MESSAGE = "Download and install .NET runtime successfully. Auto configured .NET runtime path in Azure Settings";
            AzureMessager.getMessager().success(INSTALL_SUCCEED_MESSAGE, null, openSettingsAction);
        } catch (final IOException e) {
            AzureMessager.getMessager().error(e, "Failed to install .NET Runtime, please download and set the path manually",
                generateDownloadAction(), openSettingsAction);
        }
    }

    private static Action<?> generateDownloadAction() {
        return new Action<>(Action.Id.of("bicep.download_dotnet"))
            .withLabel("Download .NET Runtime")
            .withHandler(a -> AzureActionManager.getInstance().getAction(ResourceCommonActionsContributor.OPEN_URL).handle("https://dotnet.microsoft.com/en-us/download"));
    }

    private static String getInstallCommand(final String version, final String dotnetInstallDir) {
        final List<String> args = Arrays.asList(
                "-InstallDir", escapeFilePath(dotnetInstallDir),
                "-Version", version,
                "-Runtime", "dotnet"
        );
        final String scriptPath = getDotnetInstallScript();
        return String.format("%s %s", escapeFilePath(scriptPath), String.join(" ", args));
    }

    private static String getDotnetInstallScript() {
        final String suffix = SystemUtils.IS_OS_WINDOWS ? "ps1" : "sh";
        try {
            final File tempFile = Files.createTempFile("dotnet-install", "." + suffix).toFile();
            tempFile.deleteOnExit();
            FileUtils.copyURLToFile(new URL(SCRIPT_BASE_URL + suffix), tempFile);
            return tempFile.getAbsolutePath();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String escapeFilePath(@Nonnull final String path) {
        if (SystemUtils.IS_OS_WINDOWS) {
            // Surround with single quotes instead of double quotes (see https://github.com/dotnet/cli/issues/11521)
            return String.format("'%s'", path.replace("'", "''"));
        } else {
            return String.format("\"%s\"", path);
        }
    }
}
