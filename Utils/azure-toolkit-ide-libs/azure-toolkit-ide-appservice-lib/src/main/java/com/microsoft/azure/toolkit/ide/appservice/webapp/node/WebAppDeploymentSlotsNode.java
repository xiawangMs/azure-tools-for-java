/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.webapp.node;

import com.microsoft.azure.toolkit.ide.appservice.webapp.WebAppActionsContributor;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.ide.common.component.NodeView;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcons;
import com.microsoft.azure.toolkit.lib.appservice.webapp.WebApp;
import com.microsoft.azure.toolkit.lib.appservice.webapp.WebAppDeploymentSlot;
import com.microsoft.azure.toolkit.lib.appservice.webapp.WebAppDeploymentSlotModule;
import com.microsoft.azure.toolkit.lib.common.event.AzureEvent;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.stream.Collectors;

public class WebAppDeploymentSlotsNode extends Node<WebAppDeploymentSlotModule> {
    private final WebApp webApp;

    public WebAppDeploymentSlotsNode(@Nonnull WebApp data) {
        super(data.getDeploymentModule());
        this.webApp = data;
        this.view(new WebAppDeploymentSlotsNodeView(data));
        this.actions(WebAppActionsContributor.DEPLOYMENT_SLOTS_ACTIONS);
        this.addChildren(ignore -> webApp.slots().list().stream().sorted(Comparator.comparing(WebAppDeploymentSlot::name)).collect(Collectors.toList()),
                (slot, slotsNode) -> new Node<>(slot).view(new AzureResourceLabelView<>(slot)).actions(WebAppActionsContributor.DEPLOYMENT_SLOT_ACTIONS));
    }

    static class WebAppDeploymentSlotsNodeView implements NodeView {

        @Nonnull
        @Getter
        private final WebApp webApp;
        private final AzureEventBus.EventListener listener;

        @Nullable
        @Setter
        @Getter
        private Refresher refresher;

        public WebAppDeploymentSlotsNodeView(@Nonnull WebApp webApp) {
            this.webApp = webApp;
            this.listener = new AzureEventBus.EventListener(this::onEvent);
            AzureEventBus.on("resource.refreshed.resource", listener);
            this.refreshView();
        }

        @Override
        public String getLabel() {
            return "Deployment Slots";
        }

        @Override
        public String getIconPath() {
            return AzureIcons.WebApp.DEPLOYMENT_SLOT.getIconPath();
        }

        @Override
        public String getDescription() {
            return "Deployment Slots";
        }

        @Override
        public void dispose() {
            AzureEventBus.off("resource.refreshed.resource", listener);
            this.refresher = null;
        }

        public void onEvent(AzureEvent event) {
            final Object source = event.getSource();
            if (source instanceof AzResource && ((AzResource<?, ?, ?>) source).id().equals(this.webApp.id())) {
                AzureTaskManager.getInstance().runLater(this::refreshChildren);
            }
        }
    }
}
