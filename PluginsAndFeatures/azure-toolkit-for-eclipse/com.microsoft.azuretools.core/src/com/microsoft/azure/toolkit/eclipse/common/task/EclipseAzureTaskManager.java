/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.task;

import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class EclipseAzureTaskManager extends AzureTaskManager {

    @Override
    protected void doRead(Runnable runnable, final AzureTask<?> task) {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    protected void doWrite(final Runnable runnable, final AzureTask<?> task) {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    protected void doRunOnPooledThread(Runnable runnable, AzureTask<?> task) {
        Mono.fromRunnable(runnable).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    @Override
    protected void doRunLater(Runnable runnable, AzureTask<?> task) {
        Display.getDefault().asyncExec(runnable);
    }

    @Override
    protected void doRunAndWait(Runnable runnable, AzureTask<?> task) {
        Display.getDefault().syncExec(runnable);
    }

    @Override
    protected void doRunInBackground(Runnable runnable, AzureTask<?> task) {
        final String title = String.format("Azure: %s...", Objects.requireNonNull(task.getDescription()));
        Job.create(title, (monitor) -> {
            monitor.beginTask(title, IProgressMonitor.UNKNOWN);
            try {
                task.setBackgrounded(true);
                task.setMonitor(new EclipseTaskMonitor(monitor));
                runnable.run();
            } finally {
                monitor.done();
            }
            return Status.OK_STATUS;
        }).schedule();
    }

    @Override
    protected void doRunInModal(final Runnable runnable, final AzureTask<?> task) {
        if (task.isBackgroundable()) {
            this.doRunInBackground(runnable, task);
        } else {
            this.doRunInUnBackgroundableModal(runnable, task);
        }
    }

    protected void doRunInUnBackgroundableModal(final Runnable runnable, final AzureTask<?> task) {
        final String title = String.format("Azure: %s...", Objects.requireNonNull(task.getDescription()));
        try {
            new ProgressMonitorDialog(new Shell()).run(true, task.isCancellable(), monitor -> {
                monitor.beginTask(title, IProgressMonitor.UNKNOWN);
                try {
                    task.setBackgrounded(false);
                    task.setMonitor(new EclipseTaskMonitor(monitor));
                    runnable.run();
                } finally {
                    monitor.done();
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            String msg = String.format("failed to execute task (%s)", task.getDescription());
            throw new AzureToolkitRuntimeException(msg, e);
        }
    }

    private static class EclipseTaskMonitor implements AzureTask.Monitor {

        private final IProgressMonitor monitor;

        private EclipseTaskMonitor(IProgressMonitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public void cancel() {
            this.monitor.setCanceled(true);
        }

        @Override
        public boolean isCancelled() {
            return this.monitor.isCanceled();
        }
    }
}
