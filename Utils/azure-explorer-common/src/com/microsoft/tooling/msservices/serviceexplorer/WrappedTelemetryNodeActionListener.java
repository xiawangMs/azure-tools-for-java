/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer;

import com.microsoft.azuretools.azurecommons.helpers.AzureCmdException;

public class WrappedTelemetryNodeActionListener extends NodeActionListener {

    private final NodeActionListener listener;
    private final String serviceName;
    private final String operationName;

    public WrappedTelemetryNodeActionListener(String serviceName, String operationName, NodeActionListener listener) {
        this.serviceName = serviceName;
        this.operationName = operationName;
        this.listener = listener;
    }

    @Override
    protected void actionPerformed(NodeActionEvent e) throws AzureCmdException {
        listener.actionPerformed(e);
    }

    @Override
    protected String getServiceName(NodeActionEvent event) {
        return this.serviceName;
    }

    @Override
    protected String getOperationName(NodeActionEvent e) {
        return this.operationName;
    }
}
