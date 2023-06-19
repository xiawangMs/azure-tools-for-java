/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.appsettings;

import com.microsoft.azure.toolkit.ide.appservice.AppServiceActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.common.component.AzResourceNode;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcons;
import com.microsoft.azure.toolkit.lib.appservice.AppServiceAppBase;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.action.IActionGroup;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AppSettingsNode extends AzResourceNode<AppServiceAppBase<?, ?, ?>> {
    public AppSettingsNode(@Nonnull AppServiceAppBase<?, ?, ?> app) {
        super(app);
        this.withIcon(AzureIcons.AppService.APP_SETTINGS)
            .withLabel("App Settings")
            .withDescription("")
            .withTips("Variables passed as environment variables to the application code")
            .withActions(AppServiceActionsContributor.APP_SETTINGS_ACTIONS)
            .addChildren(
                a -> Optional.ofNullable(a.getAppSettings()).map(Map::entrySet).map(s -> s.stream().toList()).orElse(Collections.emptyList()),
                (e, p) -> new AppSettingNode(e));
    }

    private static class AppSettingNode extends Node<Map.Entry<String, String>> {
        private boolean visible = false;
        private IActionGroup actionGroup;

        public AppSettingNode(@Nonnull Map.Entry<String, String> data) {
            super(data);
            this.actionGroup = createActionGroup();
            this.withIcon(AzureIcons.AppService.APP_SETTING)
                .withLabel(data.getKey())
                .withActions(actionGroup)
                .withDescription(value -> visible ? " = " + data.getValue() : " = ***")
                .onClicked(v -> {
                    this.visible = !this.visible;
                    this.refreshViewLater(10);
                });
        }

        private IActionGroup createActionGroup() {
            final Map.Entry<String, String> value = getValue();
            final Action<?> copyAction = new Action<>(Action.Id.of("user/appservice.copy_app_setting"))
                    .withLabel("Copy")
                    .withIcon(AzureIcons.Action.COPY.getIconPath())
                    .withHandler(app -> {
                        final String str = String.format("%s=%s", value.getKey(), value.getValue());
                        AzureActionManager.getInstance().getAction(ResourceCommonActionsContributor.COPY_STRING).handle(str);
                        AzureMessager.getMessager().success(AzureString.format("Environment variables are copied into clipboard."));
                    })
                    .withAuthRequired(false);
            final Action<?> copyKeyAction = new Action<>(Action.Id.of("user/appservice.copy_app_setting_key"))
                    .withLabel("Copy Key")
                    .withIcon(AzureIcons.Action.COPY.getIconPath())
                    .withHandler(app -> {
                        final String str = value.getKey();
                        AzureActionManager.getInstance().getAction(ResourceCommonActionsContributor.COPY_STRING).handle(str);
                        AzureMessager.getMessager().success(AzureString.format("Environment variable key is copied into clipboard."));
                    })
                    .withAuthRequired(false);
            return new ActionGroup(copyAction, copyKeyAction);
        }
    }
}
