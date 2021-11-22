/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.springcloud.streaminglog;

import org.eclipse.swt.widgets.Display;

import com.microsoft.azure.toolkit.eclipse.common.logstream.EclipseAzureLogStreamingManager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;

public class SpringCloudLogStreamingHandler {
    public static void startLogStreaming(final SpringCloudApp app) {
        AzureTaskManager.getInstance().runLater(() -> {
            final SpringCloudStreamingLogDialog dialog = new SpringCloudStreamingLogDialog(
                    Display.getCurrent().getActiveShell(), app);
            dialog.setOkActionListener(instance -> {
                dialog.close();
                AzureTaskManager.getInstance().runLater(
                        () -> EclipseAzureLogStreamingManager.getInstance().showLogStreaming(instance.getName(),
                                instance.getName(), app.activeDeployment().streamLogs(instance.getName())));
            });
            dialog.open();
        });
    }
}
