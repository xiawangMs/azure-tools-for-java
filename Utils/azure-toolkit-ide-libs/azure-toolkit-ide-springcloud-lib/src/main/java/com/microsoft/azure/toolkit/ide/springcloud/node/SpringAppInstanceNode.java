package com.microsoft.azure.toolkit.ide.springcloud.node;

import com.azure.resourcemanager.appplatform.models.DeploymentInstance;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.ide.common.component.NodeView;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcons;
import com.microsoft.azure.toolkit.ide.springcloud.SpringCloudActionsContributor;
import com.microsoft.azure.toolkit.lib.common.event.AzureEvent;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpringAppInstanceNode extends Node<DeploymentInstance> {
    public SpringAppInstanceNode(@Nonnull DeploymentInstance data, SpringCloudApp app) {
        super(data);
        this.view(new SpringAppInstanceNodeView(data, app));
        this.actions(SpringCloudActionsContributor.APP_INSTANCE_ACTIONS);
    }

    static class SpringAppInstanceNodeView implements NodeView {
        private final AzureEventBus.EventListener listener;
        private final SpringCloudApp springCloudApp;
        private final DeploymentInstance appInstance;
        @Nullable
        @Setter
        @Getter
        private Refresher refresher;

        public SpringAppInstanceNodeView(DeploymentInstance appInstance, SpringCloudApp app) {
            this.listener = new AzureEventBus.EventListener(this::onEvent);
            this.springCloudApp = app;
            this.appInstance = appInstance;
            AzureEventBus.on("resource.refreshed.resource", listener);
            this.refreshView();
        }

        @Override
        public String getLabel() {
            return this.appInstance.name();
        }

        @Override
        public String getIconPath() {
            return AzureIcons.SpringCloud.MODULE.getIconPath();
        }

        @Override
        public String getDescription() {
            // todo show debugging port/debugging status
            return "";
        }

        @Override
        public void dispose() {
            AzureEventBus.off("resource.refreshed.resource", listener);
            this.refresher = null;
        }
        public void onEvent(AzureEvent event) {
            final Object source = event.getSource();
            if (source instanceof AzResource && ((AzResource<?, ?, ?>) source).getId().equals(this.springCloudApp.getId())) {
                AzureTaskManager.getInstance().runLater(this::refreshChildren);
            }
        }

    }
}
