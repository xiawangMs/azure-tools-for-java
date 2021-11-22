/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.logstream;

import org.eclipse.core.runtime.jobs.Job;

import com.microsoft.azure.toolkit.eclipse.common.console.JobConsole;

public class EclipseAzureLogStreamingConsole extends JobConsole {

    private String resourceId;
    private EclipseAzureLogStreamingJob logjob;

    public EclipseAzureLogStreamingConsole(final String resourceId, final String title) {
        super(title, null);
        this.resourceId = resourceId;
    }

    public void setLogJob(EclipseAzureLogStreamingJob job) {
        this.logjob = job;
    }

    public EclipseAzureLogStreamingJob getLogJob() {
        return this.logjob;
    }

    @Override
    public Job getJob() {
        return this.logjob;
    }

    public String getResourceId() {
        return resourceId;
    }
}
