/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.microsoft.azuretools.azureexplorer.views.ServiceExplorerView;

public class ShowAzureExplorer  extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            ServiceExplorerView view = (ServiceExplorerView) PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().showView(ServiceExplorerView.ID);
        } catch (PartInitException e) {
        	e.printStackTrace();
        }
        return null;
    }
}