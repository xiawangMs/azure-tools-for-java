/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.serviceexplorer.azure;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcon;
import com.microsoft.azure.toolkit.intellij.monitor.AzureMonitorManager;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.monitor.AzureLogAnalyticsWorkspace;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspace;
import com.microsoft.tooling.msservices.serviceexplorer.NodeAction;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureModule;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OpenAzureMonitorAction extends NodeAction {
    public OpenAzureMonitorAction(@Nonnull AzureModule azureModule) {
        super(azureModule, "Open Azure Monitor");
        addListener(new NodeActionListener() {
            @Override
            protected void actionPerformed(NodeActionEvent nodeActionEvent) {
                Optional.ofNullable(azureModule.getProject())
                        .ifPresent(project -> {
                            final AzureAccount az = Azure.az(AzureAccount.class);
                            if (az.isLoggedIn() && az.account().getSelectedSubscriptions().size() > 0) {
                                final Subscription subscription = az.getAccount().getSelectedSubscriptions().get(0);
                                final List<LogAnalyticsWorkspace> workspaceList = Azure.az(AzureLogAnalyticsWorkspace.class)
                                        .logAnalyticsWorkspaces(subscription.getId()).list().stream().collect(Collectors.toList());
                                AzureMonitorManager.getInstance().openMonitorWindow((Project) project, subscription, workspaceList.get(0));
                            } else {
                                AzureMessager.getMessager().warning("Please log in first");
                            }
                        });
            }
        });
    }

    @Override
    public AzureIcon getIconSymbol() {
        return super.getIconSymbol();
    }
}
