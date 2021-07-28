/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.microsoft.azure.hdinsight.serverexplore.HDInsightRootModuleImpl;
import com.microsoft.azuretools.azureexplorer.actions.AddNewClusterAction;
import com.microsoft.azuretools.azureexplorer.actions.AttachExternalStorageAccountAction;
import com.microsoft.azuretools.azureexplorer.actions.CreateArmStorageAccountAction;
import com.microsoft.azuretools.azureexplorer.actions.CreateArmVMAction;
import com.microsoft.azuretools.azureexplorer.actions.CreateQueueAction;
import com.microsoft.azuretools.azureexplorer.actions.CreateRedisCacheAction;
import com.microsoft.azuretools.azureexplorer.actions.CreateTableAction;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.rediscache.RedisCacheModule;
import com.microsoft.tooling.msservices.serviceexplorer.azure.storage.*;
import com.microsoft.tooling.msservices.serviceexplorer.azure.vmarm.VMArmModule;

public class NodeActionsMap {

    public static final Map<Class<? extends Node>, ImmutableList<Class<? extends NodeActionListener>>> node2Actions =
            new HashMap<Class<? extends Node>, ImmutableList<Class<? extends NodeActionListener>>>();

    static {
        node2Actions.put(VMArmModule.class, new ImmutableList.Builder().add(CreateArmVMAction.class).build());
        node2Actions.put(RedisCacheModule.class, new ImmutableList.Builder().add(CreateRedisCacheAction.class).build());
        node2Actions.put(TableModule.class, new ImmutableList.Builder().add(CreateTableAction.class).build());
        node2Actions.put(QueueModule.class, new ImmutableList.Builder().add(CreateQueueAction.class).build());
        node2Actions.put(StorageModule.class, new ImmutableList.Builder().add(CreateArmStorageAccountAction.class,
            AttachExternalStorageAccountAction.class).build());
        node2Actions.put(HDInsightRootModuleImpl.class, new ImmutableList.Builder().add(AddNewClusterAction.class).build());
    }
}
