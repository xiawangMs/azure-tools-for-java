/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.ui;

import com.azure.identity.DeviceCodeInfo;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azuretools.core.Activator;
import com.microsoft.azuretools.core.components.AzureDialogWrapper;
import com.microsoft.azuretools.core.utils.AccessibilityUtils;
import lombok.Setter;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class DeviceLoginWindow extends AzureDialogWrapper {

	private DeviceCodeInfo deviceCode;
	private Link link;
	private Runnable onCancel;

	public DeviceLoginWindow(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
		fillLayout.marginHeight = 10;
		fillLayout.marginWidth = 10;
		area.setLayout(fillLayout);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		area.setLayoutData(gridData);

		link = new Link(area, SWT.NONE);
		link.setText("");
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
							.openURL(new URL(deviceCode.getVerificationUrl()));
				} catch (PartInitException | MalformedURLException ex) {
					ex.printStackTrace();
				}
			}
		});
		AccessibilityUtils.addAccessibilityNameForUIComponent(link, deviceCode.getMessage());

		Label label = new Label(area, SWT.NONE);
		label.setText("Waiting for signing in with the code, do not close the window.");

		return area;
	}

	public void show(final DeviceCodeInfo deviceCode) {
		this.deviceCode = deviceCode;
		final String url = deviceCode.getVerificationUrl();
		final String message = "<p>"
				+ deviceCode.getMessage().replace(url, String.format("<a href=\"%s\">%s</a>", url, url))
				+ "</p><p>Waiting for signing in with the code ...</p>";
		link.setText(message);
		this.open();
	}

	@Override
	protected void okPressed() {
		final StringSelection selection = new StringSelection(deviceCode.getUserCode());
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);
		try {
			PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
					.openURL(new URL(deviceCode.getVerificationUrl()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void cancelPressed() {
        Optional.ofNullable(onCancel).ifPresent(Runnable::run);
		super.cancelPressed();
	}

    public void setDoOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Azure Device Login");
	}

	@Override
	protected Point getInitialSize() {
		Point shellSize = super.getInitialSize();
		return new Point(Math.max(this.convertHorizontalDLUsToPixels(350), shellSize.x),
				Math.max(this.convertVerticalDLUsToPixels(120), shellSize.y));
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		Button okButton = getButton(IDialogConstants.OK_ID);
		okButton.setText("Copy&&Open");
	}
}
