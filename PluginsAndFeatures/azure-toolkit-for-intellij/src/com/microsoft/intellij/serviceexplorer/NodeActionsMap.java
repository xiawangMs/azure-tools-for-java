/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.serviceexplorer;

import com.google.common.collect.ImmutableList;
import com.microsoft.azure.hdinsight.serverexplore.HDInsightRootModuleImpl;
import com.microsoft.azure.hdinsight.serverexplore.action.AddNewClusterAction;
import com.microsoft.azure.sqlbigdata.serverexplore.SqlBigDataClusterModule;
import com.microsoft.intellij.serviceexplorer.azure.appservice.StartStreamingLogsAction;
import com.microsoft.intellij.serviceexplorer.azure.appservice.StopStreamingLogsAction;
import com.microsoft.intellij.serviceexplorer.azure.arm.*;
import com.microsoft.intellij.serviceexplorer.azure.container.PushToContainerRegistryAction;
import com.microsoft.intellij.serviceexplorer.azure.rediscache.CreateRedisCacheAction;
import com.microsoft.intellij.serviceexplorer.azure.springcloud.SpringCloudStreamingLogsAction;
import com.microsoft.intellij.serviceexplorer.azure.storage.ConfirmDialogAction;
import com.microsoft.intellij.serviceexplorer.azure.storage.CreateQueueAction;
import com.microsoft.intellij.serviceexplorer.azure.storage.CreateTableAction;
import com.microsoft.intellij.serviceexplorer.azure.storage.ModifyExternalStorageAccountAction;
import com.microsoft.intellij.serviceexplorer.azure.storagearm.CreateStorageAccountAction;
import com.microsoft.intellij.serviceexplorer.azure.vmarm.CreateVMAction;
import com.microsoft.sqlbigdata.serverexplore.action.LinkSqlServerBigDataClusterAction;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.arm.ResourceManagementModule;
import com.microsoft.tooling.msservices.serviceexplorer.azure.arm.ResourceManagementNode;
import com.microsoft.tooling.msservices.serviceexplorer.azure.arm.deployments.DeploymentNode;
import com.microsoft.tooling.msservices.serviceexplorer.azure.container.ContainerRegistryNode;
import com.microsoft.tooling.msservices.serviceexplorer.azure.function.FunctionNode;
import com.microsoft.tooling.msservices.serviceexplorer.azure.rediscache.RedisCacheModule;
import com.microsoft.tooling.msservices.serviceexplorer.azure.springcloud.SpringCloudAppNode;
import com.microsoft.tooling.msservices.serviceexplorer.azure.storage.ExternalStorageNode;
import com.microsoft.tooling.msservices.serviceexplorer.azure.storage.QueueModule;
import com.microsoft.tooling.msservices.serviceexplorer.azure.storage.StorageModule;
import com.microsoft.tooling.msservices.serviceexplorer.azure.storage.TableModule;
import com.microsoft.tooling.msservices.serviceexplorer.azure.vmarm.VMArmModule;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.WebAppNode;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.deploymentslot.DeploymentSlotNode;

import java.util.*;

public class NodeActionsMap {
    public static final Map<Class<? extends Node>, ImmutableList<Class<? extends NodeActionListener>>> node2Actions =
            new HashMap<>();

    static {
        node2Actions.put(VMArmModule.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
                .add(CreateVMAction.class).build());
        node2Actions.put(QueueModule.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
                .add(CreateQueueAction.class).build());
        node2Actions.put(TableModule.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
                .add(CreateTableAction.class).build());
        node2Actions.put(StorageModule.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
                .add(CreateStorageAccountAction.class).build());
        node2Actions.put(RedisCacheModule.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
                .add(CreateRedisCacheAction.class).build());
        node2Actions.put(ContainerRegistryNode.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
                .add(PushToContainerRegistryAction.class).build());
        // todo: what is ConfirmDialogAction?
        //noinspection unchecked
        node2Actions.put(ExternalStorageNode.class,
                new ImmutableList.Builder<Class<? extends NodeActionListener>>()
                        .add(ConfirmDialogAction.class, ModifyExternalStorageAccountAction.class).build());
        //noinspection unchecked
        node2Actions.put(HDInsightRootModuleImpl.class,
                new ImmutableList.Builder<Class<? extends NodeActionListener>>()
                        .add(AddNewClusterAction.class).build());
        node2Actions.put(SqlBigDataClusterModule.class,
                new ImmutableList.Builder<Class<? extends NodeActionListener>>()
                        .add(LinkSqlServerBigDataClusterAction.class).build());

        List<Class<? extends NodeActionListener>> deploymentNodeList = new ArrayList<>();
        deploymentNodeList.addAll(Arrays.asList(ExportTemplateAction.class, ExportParameterAction.class,
                UpdateDeploymentAction.class, EditDeploymentAction.class));

        node2Actions.put(DeploymentNode.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
            .addAll(deploymentNodeList).build());

        node2Actions.put(ResourceManagementModule.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
            .add(CreateDeploymentAction.class).build());

        node2Actions.put(ResourceManagementNode.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
            .add(CreateDeploymentAction.class).build());

        node2Actions.put(SpringCloudAppNode.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
                .add(SpringCloudStreamingLogsAction.class).build());

        node2Actions.put(FunctionNode.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
                .add(StartStreamingLogsAction.class).add(StopStreamingLogsAction.class).build());

        node2Actions.put(WebAppNode.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
                .add(StartStreamingLogsAction.class).add(StopStreamingLogsAction.class).build());

        node2Actions.put(DeploymentSlotNode.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
                .add(StartStreamingLogsAction.class).add(StopStreamingLogsAction.class).build());
    }
}
