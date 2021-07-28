/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.microsoft.azuretools.azureexplorer.views.ServiceExplorerView;
import com.microsoft.azuretools.core.ui.commoncontrols.Messages;
import com.microsoft.azuretools.core.utils.AzureAbstractHandler;
import com.microsoft.azuretools.core.utils.PluginUtil;

public class ShowAzureExplorer extends AzureAbstractHandler {

    public Object onExecute(ExecutionEvent event) throws ExecutionException {
        try {
            ServiceExplorerView view = (ServiceExplorerView) PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().showView(ServiceExplorerView.ID);
        } catch (PartInitException e) {
            PluginUtil.displayErrorDialogAndLog(PluginUtil.getParentShell(), Messages.error,
                    "Error opening Azure Explorer view", e);
        }
        return null;
    }
}
