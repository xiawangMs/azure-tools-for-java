/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.ide.common.icon;

public final class AzureIcons {
    public static final class Common {
        public static final AzureIcon REFRESH_ICON = AzureIcon.builder().iconPath("/icons/refresh").build();
        public static final AzureIcon UNKNOWN_ICON = AzureIcon.builder().iconPath("/icons/unknown").build();
        public static final AzureIcon AZURE = AzureIcon.builder().iconPath("/icons/Common/Azure.svg").build();
        public static final AzureIcon SIGN_IN = AzureIcon.builder().iconPath("/icons/Common/sign_in.svg").build();
        public static final AzureIcon SIGN_OUT = AzureIcon.builder().iconPath("/icons/Common/sign_out.svg").build();
        public static final AzureIcon GET_START = AzureIcon.builder().iconPath("/icons/Common/get_start.svg").build();
        public static final AzureIcon SELECT_SUBSCRIPTIONS = AzureIcon.builder().iconPath("/icons/Common/SelectSubscriptions.svg").build();
        public static final AzureIcon AZURE_ACTIVE_LOG = AzureIcon.builder().iconPath("/icons/Common/AzureActiveLog.svg").build();
        public static final AzureIcon AZURE_RESOURCE_CONNECTOR = AzureIcon.builder().iconPath("/icons/Common/AzureResourceConnector.svg").build();
        public static final AzureIcon REFRESH = AzureIcon.builder().iconPath("/icons/Common/Refresh.svg").build();
        public static final AzureIcon CREATE = AzureIcon.builder().iconPath("/icons/Common/Create.svg").build();
        public static final AzureIcon DELETE = AzureIcon.builder().iconPath("/icons/Common/Delete.svg").build();
        public static final AzureIcon START = AzureIcon.builder().iconPath("/icons/Common/Start.svg").build();
        public static final AzureIcon STOP = AzureIcon.builder().iconPath("/icons/Common/Stop.svg").build();
        public static final AzureIcon RESTART = AzureIcon.builder().iconPath("/icons/Common/Restart.svg").build();
        public static final AzureIcon OPEN_IN_PORTAL = AzureIcon.builder().iconPath("/icons/Common/OpenInPortal.svg").build();
        public static final AzureIcon SHOW_PROPERTIES = AzureIcon.builder().iconPath("/icons/Common/ShowProperties.svg").build();
        public static final AzureIcon APACHE_SPARK_FAILURE_DEBUG = AzureIcon.builder().iconPath("/icons/Common/ApacheSparkFailureDebug.svg").build();
        public static final AzureIcon FAVORITE = AzureIcon.builder().iconPath("/icons/Common/favorite.svg").build();
    }

    public static final class Action {
        public static final AzureIcon SELECT_SUBSCRIPTION = AzureIcon.builder().iconPath("/icons/action/select_subscription.svg").build();
        public static final AzureIcon OPEN_DATABASE_TOOL = AzureIcon.builder().iconPath("/icons/action/open_database_tool.svg").build();
        public static final AzureIcon PIN = AzureIcon.builder().iconPath("/icons/action/pin").build();
        public static final AzureIcon UNPIN = AzureIcon.builder().iconPath("/icons/action/unpin").build();
        public static final AzureIcon START = AzureIcon.builder().iconPath("/icons/action/start").build();
        public static final AzureIcon STOP = AzureIcon.builder().iconPath("/icons/action/stop").build();
        public static final AzureIcon RESTART = AzureIcon.builder().iconPath("/icons/action/restart").build();
        public static final AzureIcon REFRESH = AzureIcon.builder().iconPath("/icons/action/refresh").build();
        public static final AzureIcon CREATE = AzureIcon.builder().iconPath("/icons/action/create").build();
        public static final AzureIcon DELETE = AzureIcon.builder().iconPath("/icons/action/delete").build();
        public static final AzureIcon DEPLOY = AzureIcon.builder().iconPath("/icons/action/deploy").build();
        public static final AzureIcon EDIT = AzureIcon.builder().iconPath("/icons/action/edit").build();
        public static final AzureIcon EXPORT = AzureIcon.builder().iconPath("/icons/action/export").build();
        public static final AzureIcon UPDATE = AzureIcon.builder().iconPath("/icons/action/update").build();
        public static final AzureIcon ADD = AzureIcon.builder().iconPath("/icons/action/add").build();
        public static final AzureIcon REMOVE = AzureIcon.builder().iconPath("/icons/action/remove").build();
        public static final AzureIcon PORTAL = AzureIcon.builder().iconPath("/icons/action/portal").build();
        public static final AzureIcon BROWSER = AzureIcon.builder().iconPath("/icons/action/browser").build();
        public static final AzureIcon PROPERTIES = AzureIcon.builder().iconPath("/icons/action/properties").build();
        public static final AzureIcon LOG = AzureIcon.builder().iconPath("/icons/action/log").build();
        public static final AzureIcon CONSOLE = AzureIcon.builder().iconPath("/icons/action/console").build();
        public static final AzureIcon SFTP = AzureIcon.builder().iconPath("/icons/action/sftp").build();
        public static final AzureIcon DEBUG = AzureIcon.builder().iconPath("icons/action/debug").build();
        public static final AzureIcon REMOTE = AzureIcon.builder().iconPath("icons/action/remote").build();
    }

    public static final class MySQL {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/Microsoft.DBforMySQL/default.svg").build();
        public static final AzureIcon RUNNING = AzureIcon.builder().iconPath("/icons/MySQL/MySQLRunning.svg").build();
        public static final AzureIcon STOPPED = AzureIcon.builder().iconPath("/icons/MySQL/MySQLStopped.svg").build();
        public static final AzureIcon UPDATING = AzureIcon.builder().iconPath("/icons/MySQL/MySQLUpdating.svg").build();
        public static final AzureIcon CONNECT_TO_SERVER = AzureIcon.builder().iconPath("/icons/MySQL/ConnectToServer.svg").build();
        public static final AzureIcon BIND_INTO = AzureIcon.builder().iconPath("/icons/MySQL/BindInto.svg").build();
    }

    public static final class SqlServer {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/Microsoft.Sql/default.svg").build();
        public static final AzureIcon RUNNING = AzureIcon.builder().iconPath("/icons/SqlServer/SqlServerRunning.svg").build();
        public static final AzureIcon STOPPED = AzureIcon.builder().iconPath("/icons/SqlServer/SqlServerStopped.svg").build();
        public static final AzureIcon UPDATING = AzureIcon.builder().iconPath("/icons/SqlServer/SqlServerUpdating.svg").build();
        public static final AzureIcon CONNECT_TO_SERVER = AzureIcon.builder().iconPath("/icons/SqlServer/ConnectToServer.svg").build();
        public static final AzureIcon BIND_INTO = AzureIcon.builder().iconPath("/icons/SqlServer/BindInto.svg").build();
    }

    public static final class Postgre {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/Microsoft.DBforPostgreSQL/default.svg").build();
    }

    public static final class WebApp {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/Microsoft.Web/webapps.svg").build();
        public static final AzureIcon RUNNING = AzureIcon.builder().iconPath("/icons/WebApp/WebAppRunning.svg").build();
        public static final AzureIcon STOPPED = AzureIcon.builder().iconPath("/icons/WebApp/WebAppStopped.svg").build();
        public static final AzureIcon UPDATING = AzureIcon.builder().iconPath("/icons/WebApp/WebAppUpdating.svg").build();
        public static final AzureIcon RUNNING_ON_LINUX = AzureIcon.builder().iconPath("/icons/WebApp/WebAppRunningOnLinux.svg").build();
        public static final AzureIcon STOPPED_ON_LINUX = AzureIcon.builder().iconPath("/icons/WebApp/WebAppStoppedOnLinux.svg").build();
        public static final AzureIcon UPDATING_ON_LINUX = AzureIcon.builder().iconPath("/icons/WebApp/WebAppUpdatingOnLinux.svg").build();
        public static final AzureIcon DEPLOY = AzureIcon.builder().iconPath("/icons/WebApp/Deploy.svg").build();
        public static final AzureIcon DEPLOYMENT_SLOT = AzureIcon.builder().iconPath("/icons/Microsoft.Web/sites/slots/default.svg").build();
    }

    public static final class DeploymentSlot {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/DeploymentSlot/DeploymentSlot.svg").build();
        public static final AzureIcon MODULE_ON_LINUX = AzureIcon.builder().iconPath("/icons/DeploymentSlot/DeploymentSlotOnLinux.svg").build();
        public static final AzureIcon RUNNING = AzureIcon.builder().iconPath("/icons/DeploymentSlot/DeploymentSlotRunning.svg").build();
        public static final AzureIcon STOPPED = AzureIcon.builder().iconPath("/icons/DeploymentSlot/DeploymentSlotStopped.svg").build();
        public static final AzureIcon UPDATING = AzureIcon.builder().iconPath("/icons/DeploymentSlot/DeploymentSlotUpdating.svg").build();
        public static final AzureIcon RUNNING_ON_LINUX = AzureIcon.builder().iconPath("/icons/DeploymentSlot/DeploymentSlotRunningOnLinux.svg").build();
        public static final AzureIcon STOPPED_ON_LINUX = AzureIcon.builder().iconPath("/icons/DeploymentSlot/DeploymentSlotStoppedOnLinux.svg").build();
        public static final AzureIcon UPDATING_ON_LINUX = AzureIcon.builder().iconPath("/icons/DeploymentSlot/DeploymentSlotUpdatingOnLinux.svg").build();
    }

    public static final class FunctionApp {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/Microsoft.Web/functions.svg").build();
        public static final AzureIcon RUNNING = AzureIcon.builder().iconPath("/icons/FunctionApp/FunctionAppRunning.svg").build();
        public static final AzureIcon STOPPED = AzureIcon.builder().iconPath("/icons/FunctionApp/FunctionAppStopped.svg").build();
        public static final AzureIcon UPDATING = AzureIcon.builder().iconPath("/icons/FunctionApp/FunctionAppUpdating.svg").build();
        public static final AzureIcon RUN = AzureIcon.builder().iconPath("/icons/FunctionApp/Run.svg").build();
        public static final AzureIcon DEPLOY = AzureIcon.builder().iconPath("/icons/FunctionApp/Deploy.svg").build();
    }

    public static final class SpringCloud {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/Microsoft.AppPlatform/default.svg").build();
        public static final AzureIcon CLUSTER = AzureIcon.builder().iconPath("/icons/SpringCloud/SpringCloudApp.svg").build();
        public static final AzureIcon RUNNING = AzureIcon.builder().iconPath("/icons/SpringCloud/SpringCloudAppRunning.svg").build();
        public static final AzureIcon STOPPED = AzureIcon.builder().iconPath("/icons/SpringCloud/SpringCloudAppStopped.svg").build();
        public static final AzureIcon UPDATING = AzureIcon.builder().iconPath("/icons/SpringCloud/SpringCloudAppUpdating.svg").build();
        public static final AzureIcon FAILED = AzureIcon.builder().iconPath("/icons/SpringCloud/SpringCloudAppFailed.svg").build();
        public static final AzureIcon PENDING = AzureIcon.builder().iconPath("/icons/SpringCloud/SpringCloudAppPending.svg").build();
        public static final AzureIcon UNKNOWN = AzureIcon.builder().iconPath("/icons/SpringCloud/SpringCloudAppUnknown.svg").build();
        public static final AzureIcon DEPLOY = AzureIcon.builder().iconPath("/icons/SpringCloud/Deploy.svg").build();
        public static final AzureIcon ADD_DEPENDENCY = AzureIcon.builder().iconPath("/icons/SpringCloud/AddDependency.svg").build();
    }

    public static final class ApacheSparkOnAzureSynapse {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/ApacheSparkOnAzureSynapse/ApacheSparkOnAzureSynapse.svg").build();
    }

    public static final class ApacheSparkOnCosmos {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/ApacheSparkOnCosmos/ApacheSparkOnCosmos.svg").build();
    }

    public static final class ContainerRegistry {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/ContainerRegistry/ContainerRegistry.svg").build();
    }

    public static final class HDInsight {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/HDInsight/HDInsight.svg").build();
    }

    public static final class RedisCache {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/Microsoft.Cache/default.svg").build();
        public static final AzureIcon RUNNING = AzureIcon.builder().iconPath("/icons/RedisCache/RedisCacheRunning.svg").build();
        public static final AzureIcon STOPPED = AzureIcon.builder().iconPath("/icons/RedisCache/RedisCacheStopped.svg").build();
        public static final AzureIcon UPDATING = AzureIcon.builder().iconPath("/icons/RedisCache/RedisCacheUpdating.svg").build();
    }

    public static final class ResourceManagement {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/ResourceManagement/ResourceManagement.svg").build();
    }

    public static final class StorageAccount {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/Microsoft.Storage/default.svg").build();
    }

    public static final class VirtualMachine {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/Microsoft.Compute/default.svg").build();
        public static final AzureIcon RUNNING = AzureIcon.builder().iconPath("/icons/VirtualMachine/VirtualMachineRunning.svg").build();
        public static final AzureIcon STOPPED = AzureIcon.builder().iconPath("/icons/VirtualMachine/VirtualMachineStopped.svg").build();
        public static final AzureIcon UPDATING = AzureIcon.builder().iconPath("/icons/VirtualMachine/VirtualMachineUpdating.svg").build();
    }

    public static final class SQLServerBigDataCluster {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/SQLServerBigDataCluster/SqlServerBigDataCluster.svg").build();
    }

    public static final class DockerSupport {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/DockerSupport/DockerSupport.svg").build();
        public static final AzureIcon RUN = AzureIcon.builder().iconPath("/icons/DockerSupport/Run.svg").build();
        public static final AzureIcon PUSH_IMAGE = AzureIcon.builder().iconPath("/icons/DockerSupport/PushImage.svg").build();
        public static final AzureIcon RUN_ON_WEB_APP = AzureIcon.builder().iconPath("/icons/DockerSupport/RunOnWebApp.svg").build();
    }

    public static final class Resources {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/Microsoft.Resources/default.svg").build();
        public static final AzureIcon DEPLOYMENT = AzureIcon.builder().iconPath("/icons/Microsoft.Resources/resourceGroups/deployments/default.svg").build();
        public static final AzureIcon DEPLOYMENT_MODULE = AzureIcon.builder().iconPath("/icons/Microsoft.Resources/resourceGroups/deployments/folder.svg").build();
        public static final AzureIcon GENERIC_RESOURCE = AzureIcon.builder().iconPath("/icons/Microsoft.Resources/resourceGroups/genericResources/default.svg").build();
    }

    public static final class ApplicationInsights {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/Microsoft.Insights/default.svg").build();
    }

    public static final class Kubernetes {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/Microsoft.ContainerService/default.svg").build();
    }

    public static final class Cosmos {
        public static final AzureIcon MODULE = AzureIcon.builder().iconPath("/icons/Microsoft.DocumentDB/default.svg").build();
    }

    public static final class Connector {
        public static final AzureIcon CONNECT = AzureIcon.builder().iconPath("/icons/connector/connect.svg").build();
    }
}
