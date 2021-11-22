/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.logstream;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

import com.microsoft.azure.arm.resources.ResourceId;
import com.microsoft.azure.toolkit.eclipse.common.console.EclipseConsoleMessager;
import com.microsoft.azure.toolkit.eclipse.common.console.JobConsole;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;

import reactor.core.publisher.Flux;

public class EclipseAzureLogStreamingManager {

    public void showLogStreaming(final String resourceId, final Flux<String> logStreaming) {
        showLogStreaming(resourceId, getLogStreamingTitle(resourceId), logStreaming);
    }

    public void showLogStreaming(final String resourceId, final String title, final Flux<String> logStreaming) {
        EclipseAzureLogStreamingConsole console = getLogViewForResource(resourceId);
        // Get existing log streaming console
        if (console != null) {
            // Re-schedule log streaming job
            if (console.getLogJob().isDisposed()) {
                scheduleLogStreaming(console, title, logStreaming);
            }
        } else {
            console = new EclipseAzureLogStreamingConsole(resourceId, title);
            ConsolePlugin.getDefault().getConsoleManager().addConsoles(new JobConsole[] { console });
            scheduleLogStreaming(console, title, logStreaming);
        }
        // Show console
        ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
    }

    public void stopLogStreaming(final String resourceId) {
        final EclipseAzureLogStreamingJob job = Optional.ofNullable(getLogViewForResource(resourceId))
                .map(EclipseAzureLogStreamingConsole::getLogJob).orElse(null);
        if (job == null || job.isDisposed()) {
            AzureMessager.getMessager().warning(AzureString.format("Log-streaming for %s is already disconnected",
                    ResourceId.fromString(resourceId).name()));
        } else {
            job.closeLogStream();
        }
    }

    private EclipseAzureLogStreamingConsole getLogViewForResource(final String resourceId) {
        final IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
        return Arrays.stream(consoles)
                .filter(console -> console instanceof EclipseAzureLogStreamingConsole
                        && StringUtils.equals(resourceId, ((EclipseAzureLogStreamingConsole) console).getResourceId()))
                .map(console -> ((EclipseAzureLogStreamingConsole) console)).findFirst().orElse(null);
    }

    private void scheduleLogStreaming(final EclipseAzureLogStreamingConsole console, final String title,
            final Flux<String> logStreaming) {
        final EclipseAzureLogStreamingJob job = new EclipseAzureLogStreamingJob(title, logStreaming);
        job.setMessager(new EclipseConsoleMessager(console));
        console.setLogJob(job);
        console.activate();
        job.schedule();
    }

    public static String getLogStreamingTitle(final String resourceId) {
        return String.format("Log Streaming: %s", ResourceId.fromString(resourceId).name());
    }

    public static EclipseAzureLogStreamingManager getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static EclipseAzureLogStreamingManager INSTANCE = new EclipseAzureLogStreamingManager();
    }
}
