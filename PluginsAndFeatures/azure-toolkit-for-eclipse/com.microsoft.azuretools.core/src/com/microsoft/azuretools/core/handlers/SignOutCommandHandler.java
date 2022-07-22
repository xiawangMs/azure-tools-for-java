/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.handlers;

import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.Account;
import com.microsoft.azure.toolkit.lib.auth.AuthType;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.microsoft.azuretools.core.utils.AzureAbstractHandler;

public class SignOutCommandHandler extends AzureAbstractHandler {

	@Override
	public Object onExecute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		doSignOut(window.getShell());

		return null;
	}

	public static void doSignOut(Shell shell) {
		EventUtil.executeWithLog(TelemetryConstants.ACCOUNT, TelemetryConstants.SIGNOUT, (operation) -> {
			final AzureAccount az = Azure.az(AzureAccount.class);
			if (az.isLoggedIn()) {
				final Account account = az.account();
				final AuthType authType = account.getType();
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				final String warningMessage = String.format("Signed in as \"%s\" with %s", account.getUsername(), authType.getLabel());
				final String additionalMsg = authType == AuthType.AZURE_CLI ? "(This will not sign you out from Azure CLI)" : "";
				final String msg = String.format("%s\nDo you really want to sign out? %s", warningMessage, additionalMsg);
				messageBox.setMessage(msg);
				messageBox.setText("Azure Sign Out");
				int response = messageBox.open();
				if (response == SWT.YES) {
					az.logout();
				}
			}
		}, (ex) -> ex.printStackTrace());
	}
}
