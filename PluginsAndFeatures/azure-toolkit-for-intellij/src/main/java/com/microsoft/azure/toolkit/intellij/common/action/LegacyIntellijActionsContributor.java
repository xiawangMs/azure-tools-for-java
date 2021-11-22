/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle;
import com.microsoft.intellij.actions.AzureSignInAction;

import java.util.Optional;
import java.util.function.BiConsumer;

public class LegacyIntellijActionsContributor implements IActionsContributor {
    @Override
    public void registerActions(AzureActionManager am) {
        final AzureString title = AzureOperationBundle.title("account.authorize");
        final ActionView.Builder authView = new ActionView.Builder("Authorize").title((s) -> title);
        final BiConsumer<Runnable, AnActionEvent> handler = (Runnable r, AnActionEvent e) ->
            AzureSignInAction.requireSignedIn(Optional.ofNullable(e).map(AnActionEvent::getProject).orElse(null), r);
        am.registerAction(Action.REQUIRE_AUTH, new Action<>(handler, authView).authRequired(false));
    }
}