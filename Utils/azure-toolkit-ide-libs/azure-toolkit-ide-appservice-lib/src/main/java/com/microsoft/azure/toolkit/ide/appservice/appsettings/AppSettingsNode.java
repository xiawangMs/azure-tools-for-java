/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.appsettings;

import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcons;
import com.microsoft.azure.toolkit.lib.appservice.AppServiceAppBase;
import com.microsoft.azure.toolkit.lib.common.event.AzureEvent;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AppSettingsNode extends Node<Map<String, String>> {
    @Nonnull
    @Getter
    private final AppServiceAppBase<?, ?, ?> app;
    private final AzureEventBus.EventListener listener;

    public AppSettingsNode(@Nonnull AppServiceAppBase<?, ?, ?> app) {
        super(Optional.ofNullable(app.getAppSettings()).orElse(Collections.emptyMap()));
        this.app = app;
        this.listener = new AzureEventBus.EventListener(this::onEvent);
        this.withIcon(AzureIcons.AppService.APP_SETTINGS)
            .withLabel("App Settings")
            .withTips("Variables passed as environment variables to the application code")
            .addChildren(settings -> settings.entrySet().stream().map(AppSettingNode::new).collect(Collectors.toList()));

        AzureEventBus.on("resource.refreshed.resource", listener);
        AzureEventBus.on("resource.status_changed.resource", listener);
    }

    public void onEvent(AzureEvent event) {
        final Object source = event.getSource();
        if (source instanceof AzResource && ((AzResource) source).getId().equals(this.getValue().getId())) {
            this.onChildrenChanged();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        AzureEventBus.off("resource.refreshed.resource", listener);
        AzureEventBus.off("resource.status_changed.resource", listener);
    }

    private static class AppSettingNode extends Node<Map.Entry<String, String>> {
        private boolean visible = false;

        public AppSettingNode(@Nonnull Map.Entry<String, String> data) {
            super(data);
            this.withIcon(AzureIcons.AppService.APP_SETTING)
                .withLabel(data.getKey())
                .withDescription(value -> visible ? " = " + data.getValue() : " = ***")
                .onClicked(v -> {
                    this.visible = !this.visible;
                    this.onViewChanged();
                });
        }
    }
}
