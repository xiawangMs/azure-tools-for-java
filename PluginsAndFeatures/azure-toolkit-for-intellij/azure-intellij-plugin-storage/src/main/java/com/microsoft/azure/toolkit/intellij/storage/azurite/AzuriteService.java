/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.storage.azurite;

import com.intellij.execution.CommandLineUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Version;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.intellij.common.TerminalUtils;
import com.microsoft.azure.toolkit.intellij.storage.IntellijStorageActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.utils.CommandUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class AzuriteService {

    public static final String AZURITE_DISPLAY_NAME = "Azurite";
    public static final String INSTALL_AZURITE_LINK = "https://learn.microsoft.com/en-us/azure/storage/common/storage-use-azurite?tabs=npm";
    public static final String NODE_VERSION_ERROR_MESSAGE = "Failed to get node version, or node version is too old (lower than 8) to run azurite, please visit https://learn.microsoft.com/en-us/azure/storage/common/storage-use-azurite for installation guidances.";
    public static final String AZURITE_INSTALL_COMMAND = "npm install -g azurite";
    private ProcessHandler processHandler;

    public static AzuriteService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public boolean startAzurite(@Nonnull final Project project) {
        // if process is still running, return
        final Boolean isProcessTerminated = Optional.ofNullable(processHandler).map(ProcessHandler::isProcessTerminated).orElse(true);
        if (!isProcessTerminated) {
            // show related toolwindow
            showAzuriteTerminal(project);
            return false;
        }
        try {
            final ConsoleView consoleView = getOrCreateConsoleView(project);
            final GeneralCommandLine commandLine = new GeneralCommandLine(getAzuriteCommand());
            this.processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine);
            this.processHandler.addProcessListener(new ProcessAdapter() {
                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    consoleView.print("Azurite has been terminated", ConsoleViewContentType.SYSTEM_OUTPUT);
                }
            });
            consoleView.attachToProcess(processHandler);
            this.processHandler.startNotify();
        } catch (final ExecutionException e) {
            AzureMessager.getMessager().error("Failed to run azurite, " + ExceptionUtils.getRootCauseMessage(e), getAzuriteFailureActions(project));
            return false;
        }
        return true;
    }

    private Action<?>[] getAzuriteFailureActions(@Nonnull final Project project) {
        final Action<String> openBrowserAction = AzureActionManager.getInstance().getAction(ResourceCommonActionsContributor.OPEN_URL)
                .bind(INSTALL_AZURITE_LINK).withLabel("Open Document");
        final Action<?> installAzuriteAction = AzureActionManager.getInstance().getAction(IntellijStorageActionsContributor.INSTALL_AZURITE)
                .bind(project);
        return isNodeMeetAzuriteRequirement() ? new Action[]{installAzuriteAction, openBrowserAction} : new Action[]{openBrowserAction};
    }

    public static void installAzurite(@Nonnull final Project project) {
        if (!isNodeMeetAzuriteRequirement()) {
            AzureMessager.getMessager().error(NODE_VERSION_ERROR_MESSAGE);
        }
        TerminalUtils.executeInTerminal(project, AZURITE_INSTALL_COMMAND, "Azurite installation");
    }

    private static boolean isNodeMeetAzuriteRequirement() {
        try {
            final GeneralCommandLine commandLine = new GeneralCommandLine("node", "-v");
            final Process process = commandLine.createProcess();
            process.waitFor(10, TimeUnit.SECONDS);
            final String nodeVersion = IOUtils.toString(process.getInputStream());
            return Version.parseVersion(StringUtils.removeStartIgnoreCase(nodeVersion, "v")).compareTo(8) >= 0;
        } catch (final Exception e) {
            return false;
        }
    }

    private void showAzuriteTerminal(@Nonnull final Project project) {
        final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Run");
        if (Objects.nonNull(toolWindow)) {
            Arrays.stream(toolWindow.getContentManager().getContents())
                    .filter(content -> StringUtils.equalsIgnoreCase(content.getDisplayName(), AZURITE_DISPLAY_NAME))
                    .findFirst().ifPresent(content -> toolWindow.getContentManager().setSelectedContent(content));
        }
    }

    private ConsoleView getOrCreateConsoleView(final Project project) {
        final DataContext context = dataId -> CommonDataKeys.PROJECT.getName().equals(dataId) ? project : null;
        final AnActionEvent event = AnActionEvent.createFromAnAction(new EmptyAction(), null, "azure.azurite", context);
        ActionManager.getInstance().getAction("ActivateRunToolWindow").actionPerformed(event);
        final ConsoleView console = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Run");
        if (toolWindow == null) {
            return console;
        }
        final String displayName = AZURITE_DISPLAY_NAME;
        final Content result = Arrays.stream(toolWindow.getContentManager().getContents())
                .filter(content -> StringUtils.equalsIgnoreCase(content.getDisplayName(), displayName))
                .findFirst().orElseGet(() -> {
                    final Content content = ContentFactory.getInstance().createContent(console.getComponent(), displayName, false);
                    toolWindow.getContentManager().addContent(content);
                    return content;
                });
        result.setComponent(console.getComponent());
        toolWindow.getContentManager().addContentManagerListener(new ContentManagerListener() {
            @Override
            public void contentRemoved(@Nonnull ContentManagerEvent event) {
                // terminate process once closed
                AzuriteService.getInstance().stopAzurite();
            }
        });
        return console;
    }

    private static String[] getAzuriteCommand() {
        final String fileLocation = System.getProperty("user.home");
        return new String[]{"azurite", "-l", fileLocation};
    }

    public void stopAzurite() {
        Optional.ofNullable(processHandler).ifPresent(handler -> {
            if (!handler.isProcessTerminated()) {
                handler.destroyProcess();
            }
        });
    }

    private static class SingletonHolder {
        public static final AzuriteService INSTANCE = new AzuriteService();
    }
}
