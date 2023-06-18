/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.appsettings;

import com.microsoft.azure.toolkit.ide.common.component.AzResourceNode;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcons;
import com.microsoft.azure.toolkit.lib.appservice.AppServiceAppBase;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class AppSettingsNode extends AzResourceNode<AppServiceAppBase<?, ?, ?>> {
    public AppSettingsNode(@Nonnull AppServiceAppBase<?, ?, ?> app) {
        super(app);
        this.withIcon(AzureIcons.AppService.APP_SETTINGS)
            .withLabel("App Settings")
            .withTips("Variables passed as environment variables to the application code")
            .addChildren(
                a -> Optional.ofNullable(a.getAppSettings()).map(Map::entrySet).map(s -> s.stream().toList()).orElse(Collections.emptyList()),
                (e, p) -> new AppSettingNode(e));
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
                    this.refreshViewLater();
                });
        }
    }
}
