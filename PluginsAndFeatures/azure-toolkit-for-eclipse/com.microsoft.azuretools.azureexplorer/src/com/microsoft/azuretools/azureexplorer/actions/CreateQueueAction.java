/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.actions;

import com.microsoft.azuretools.azureexplorer.forms.CreateQueueForm;
import com.microsoft.azuretools.core.utils.PluginUtil;
import com.microsoft.tooling.msservices.helpers.Name;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.storage.ClientStorageNode;
import com.microsoft.tooling.msservices.serviceexplorer.azure.storage.QueueModule;

@Name("Create new queue")
public class CreateQueueAction  extends NodeActionListener {
    private QueueModule queueModule;

    public CreateQueueAction(QueueModule queuModule) {
        this.queueModule = queuModule;
    }

    @Override
    public void actionPerformed(NodeActionEvent e) {
        /*CreateQueueForm form = new CreateQueueForm(PluginUtil.getParentShell(), queueModule.getStorageAccount());

        form.setOnCreate(new Runnable() {
            @Override
            public void run() {
                queueModule.getParent().removeAllChildNodes();
                ((ClientStorageNode) queueModule.getParent()).load();
            }
        });
        form.open();*/
    }
}
