/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.handlers;

import com.microsoft.azure.toolkit.lib.auth.model.AuthType;
import com.microsoft.azuretools.authmanage.models.AuthMethodDetails;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.authmanage.AuthMethod;
import com.microsoft.azuretools.core.utils.AzureAbstractHandler;

import javax.annotation.Nonnull;

public class SignOutCommandHandler extends AzureAbstractHandler {

    @Override
    public Object onExecute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        doSignOut(window.getShell());

        return null;
    }

    public static void doSignOut(Shell shell) {
        EventUtil.executeWithLog(TelemetryConstants.ACCOUNT, TelemetryConstants.SIGNOUT, (operation) -> {
            AuthMethodManager authMethodManager = AuthMethodManager.getInstance();
            MessageBox messageBox = new MessageBox(
                    shell,
                    SWT.ICON_QUESTION | SWT.YES | SWT.NO);
            messageBox.setMessage(getSignOutWarningMessage(authMethodManager));
            messageBox.setText("Azure Sign Out");
            int response = messageBox.open();
            if (response == SWT.YES) {
                if (authMethodManager.isSignedIn()) {
                    authMethodManager.signOut();
                }
            }
        }, (ex) -> ex.printStackTrace());
    }

    public static String getSignOutWarningMessage(@Nonnull AuthMethodManager authMethodManager) {
        final AuthMethodDetails authMethodDetails = authMethodManager.getAuthMethodDetails();
        if (authMethodDetails == null || authMethodDetails.getAuthType() == null) {
            return "Do you really want to sign out?";
        }
        final AuthType authType = authMethodDetails.getAuthType();
        final String warningMessage;
        switch (authType) {
            case SERVICE_PRINCIPAL:
                warningMessage = String.format("Signed in using service principal \"%s\"", authMethodDetails.getClientId());
                break;
            case OAUTH2:
            case DEVICE_CODE:
                warningMessage = String.format("Signed in as %s(%s)", authMethodDetails.getAccountEmail(), authType.toString());
                break;
            case AZURE_CLI:
                warningMessage = "Signed in with Azure CLI";
                break;
            default:
                warningMessage = "Signed in by unknown authentication method.";
                break;
        }
        return String.format("%s\nDo you really want to sign out? %s",
                warningMessage, authType == AuthType.AZURE_CLI ? "(This will not sign you out from Azure CLI)" : "");
    }
}
