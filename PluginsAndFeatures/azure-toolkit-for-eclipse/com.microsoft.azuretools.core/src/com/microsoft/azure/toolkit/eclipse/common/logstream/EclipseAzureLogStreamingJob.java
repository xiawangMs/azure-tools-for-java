/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.logstream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.microsoft.azure.toolkit.eclipse.common.console.AzureAsyncConsoleJob;

import reactor.core.publisher.Flux;

public class EclipseAzureLogStreamingJob extends AzureAsyncConsoleJob {

    private final Flux<String> logStreaming;

    public EclipseAzureLogStreamingJob(final String title, final Flux<String> logStreaming) {
        super(title);
        this.logStreaming = logStreaming;
        this.setSupplier(this::showLogStream);
    }

    private IStatus showLogStream() {
        final Flux<String> doOnEach = logStreaming
                .doAfterTerminate(() -> EclipseAzureLogStreamingJob.this.getMessager()
                        .warning("Disconnected from log-streaming service."))
                .doOnEach((log) -> EclipseAzureLogStreamingJob.this.getMessager().info(log.get()));
        try {
            doOnEach.blockLast();
        } catch (Exception e) {
            // swallow interrupt exception
        }
        return Status.OK_STATUS;
    }

    public void closeLogStream() {
        this.cancel();
    }

    public boolean isDisposed() {
        return Job.NONE == this.getState();
    }
}
