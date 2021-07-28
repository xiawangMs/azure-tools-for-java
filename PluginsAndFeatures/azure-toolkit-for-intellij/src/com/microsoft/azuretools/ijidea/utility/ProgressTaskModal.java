/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.ijidea.utility;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.microsoft.azuretools.utils.IProgressTaskImpl;
import com.microsoft.azuretools.utils.IWorker;

/**
 * Created by vlashch on 1/23/17.
 */
public class ProgressTaskModal implements IProgressTaskImpl {

    private Project project;

    public ProgressTaskModal(Project project) {
        this.project = project;
    }

    @Override
    public void doWork(IWorker worker) {
        ProgressManager.getInstance().run(new Task.Modal(project, worker.getName(), true) {
            @Override
            public void run(ProgressIndicator progressIndicator) {
                try {
                    progressIndicator.setIndeterminate(true);
                    worker.work(new UpdateProgressIndicator(progressIndicator));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }
}
