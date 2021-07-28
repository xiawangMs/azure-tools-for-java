/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.serviceexplorer;

import com.google.common.collect.ImmutableList;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;

import java.util.HashMap;
import java.util.Map;

//import com.microsoft.azure.hdinsight.serverexplore.HDInsightRootModuleImpl;
//import com.microsoft.azure.hdinsight.serverexplore.action.AddNewClusterAction;
//import com.microsoft.azure.sqlbigdata.serverexplore.SqlBigDataClusterModule;
//import com.microsoft.azure.toolkit.intellij.appservice.action.ProfileFlightRecordAction;
//import com.microsoft.azure.toolkit.intellij.appservice.action.SSHIntoWebAppAction;
//import com.microsoft.azure.toolkit.intellij.appservice.action.StartStreamingLogsAction;
//import com.microsoft.azure.toolkit.intellij.appservice.action.StopStreamingLogsAction;
//import com.microsoft.azure.toolkit.intellij.arm.action.*;
//import com.microsoft.azure.toolkit.intellij.connector.mysql.ConnectToMySQLAction;
//import com.microsoft.azure.toolkit.intellij.function.action.CreateFunctionAppAction;
//import com.microsoft.azure.toolkit.intellij.function.action.DeployFunctionAppAction;
//import com.microsoft.azure.toolkit.intellij.mysql.action.CreateMySQLAction;
//import com.microsoft.azure.toolkit.intellij.mysql.action.OpenMySQLByToolsAction;
//import com.microsoft.azure.toolkit.intellij.redis.action.CreateRedisCacheAction;
//import com.microsoft.azure.toolkit.intellij.springcloud.streaminglog.SpringCloudStreamingLogAction;
//import com.microsoft.azure.toolkit.intellij.sqlserver.CreateSqlServerAction;
//import com.microsoft.azure.toolkit.intellij.sqlserver.OpenSqlServerByToolsAction;
//import com.microsoft.azure.toolkit.intellij.vm.CreateVMAction;
//import com.microsoft.azure.toolkit.intellij.webapp.action.CreateWebAppAction;
//import com.microsoft.azure.toolkit.intellij.webapp.action.DeployWebAppAction;
//import com.microsoft.azure.toolkit.intellij.webapp.docker.action.PushToContainerRegistryAction;
//import com.microsoft.intellij.serviceexplorer.azure.storage.ConfirmDialogAction;
//import com.microsoft.intellij.serviceexplorer.azure.storage.CreateQueueAction;
//import com.microsoft.intellij.serviceexplorer.azure.storage.CreateTableAction;
//import com.microsoft.intellij.serviceexplorer.azure.storage.ModifyExternalStorageAccountAction;
//import com.microsoft.intellij.serviceexplorer.azure.storagearm.CreateStorageAccountAction;
//import com.microsoft.sqlbigdata.serverexplore.action.LinkSqlServerBigDataClusterAction;

public class NodeActionsMap {
    public static final Map<Class<? extends Node>, ImmutableList<Class<? extends NodeActionListener>>> node2Actions =
            new HashMap<>();

    static {
//        node2Actions.put(VMArmModule.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                .add(CreateVMAction.class).build());
//        node2Actions.put(QueueModule.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                .add(CreateQueueAction.class).build());
//        node2Actions.put(TableModule.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                .add(CreateTableAction.class).build());
//        node2Actions.put(StorageModule.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                .add(CreateStorageAccountAction.class).build());
//        node2Actions.put(RedisCacheModule.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                .add(CreateRedisCacheAction.class).build());
//        node2Actions.put(WebAppModule.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                .add(CreateWebAppAction.class).build());
//        node2Actions.put(FunctionModule.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                .add(CreateFunctionAppAction.class).build());
//        node2Actions.put(ContainerRegistryNode.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                .add(PushToContainerRegistryAction.class).build());
//        node2Actions.put(MySQLModule.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                .add(CreateMySQLAction.class).build());
//        node2Actions.put(SqlServerModule.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                .add(CreateSqlServerAction.class).build());
//        // todo: what is ConfirmDialogAction?
//        //noinspection unchecked
//        node2Actions.put(ExternalStorageNode.class,
//                new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                        .add(ConfirmDialogAction.class, ModifyExternalStorageAccountAction.class).build());
//        //noinspection unchecked
//        node2Actions.put(HDInsightRootModuleImpl.class,
//                new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                        .add(AddNewClusterAction.class).build());
//        node2Actions.put(SqlBigDataClusterModule.class,
//                new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                        .add(LinkSqlServerBigDataClusterAction.class).build());
//
//        List<Class<? extends NodeActionListener>> deploymentNodeList = new ArrayList<>();
//        deploymentNodeList.addAll(Arrays.asList(ExportTemplateAction.class, ExportParameterAction.class,
//                UpdateDeploymentAction.class, EditDeploymentAction.class));
//
//        node2Actions.put(DeploymentNode.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//            .addAll(deploymentNodeList).build());
//
//        node2Actions.put(ResourceManagementModule.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//            .add(CreateDeploymentAction.class).build());
//
//        node2Actions.put(ResourceManagementNode.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//            .add(CreateDeploymentAction.class).build());
//
//        node2Actions.put(SpringCloudAppNode.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                .add(SpringCloudStreamingLogAction.class).build());
//
//        node2Actions.put(FunctionAppNode.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                .add(StartStreamingLogsAction.class).add(StopStreamingLogsAction.class).add(DeployFunctionAppAction.class).build());
//
//        node2Actions.put(MySQLNode.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                .add(OpenMySQLByToolsAction.class).add(ConnectToMySQLAction.class).build());
//
//        node2Actions.put(SqlServerNode.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                .add(OpenSqlServerByToolsAction.class).build());
//
//        node2Actions.put(WebAppNode.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                .add(StartStreamingLogsAction.class).add(StopStreamingLogsAction.class).add(SSHIntoWebAppAction.class)
//                .add(DeployWebAppAction.class)
//                .add(ProfileFlightRecordAction.class).build());
//
//        node2Actions.put(DeploymentSlotNode.class, new ImmutableList.Builder<Class<? extends NodeActionListener>>()
//                .add(StartStreamingLogsAction.class).add(StopStreamingLogsAction.class).build());
    }
}
