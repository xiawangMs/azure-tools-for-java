/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.springcloud.streaminglog;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.springcloud.component.SpringCloudAppInstanceSelectionDialog;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessager;
import com.microsoft.azure.toolkit.lib.common.operation.OperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudAppInstance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpringCloudStreamingLogAction {


    public static void startLogStreaming(@Nonnull SpringCloudApp app, @Nullable Project project) {
        final IAzureMessager messager = AzureMessager.getMessager();
        final AzureString title = OperationBundle.description("internal/springcloud.open_log_stream.instance", app.getName());
        showLogStreamingDialog(app, project);
    }

    private static void showLogStreamingDialog(SpringCloudApp app, Project project) {
        AzureTaskManager.getInstance().runLater(() -> {
            final SpringCloudAppInstanceSelectionDialog dialog = new SpringCloudAppInstanceSelectionDialog(project, app);
            if (dialog.showAndGet()) {
                final SpringCloudAppInstance target = dialog.getInstance();
                SpringCloudStreamingLogManager.getInstance().showStreamingLog(project, app, target.getName());
            }
        });
    }
}
