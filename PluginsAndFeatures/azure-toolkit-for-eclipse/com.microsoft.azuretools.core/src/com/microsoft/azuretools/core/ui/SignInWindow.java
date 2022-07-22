/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.ui;

import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.auth.AuthConfiguration;
import com.microsoft.azure.toolkit.lib.auth.AuthType;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azuretools.core.components.AzureTitleAreaDialogWrapper;
import com.microsoft.azuretools.core.utils.AccessibilityUtils;

import lombok.SneakyThrows;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

public class SignInWindow extends AzureTitleAreaDialogWrapper {
    private static final String DESC = "desc_label";
    private static final String AZURE_SIGN_IN = "Azure Sign In";
    private Button cliBtn;
    private Button oauthBtn;
    private Button deviceBtn;
    private Button spBtn;
    private Label cliDesc;
    private Label oauthDesc;
    private Label deviceDesc;
    private Label spDesc;

    private AuthType type = null;
	private Button okButton;
	private Group authTypeGroup;

    /**
     * Create the dialog.
     * @param parentShell
     *
     */
    public SignInWindow(Shell parentShell) {
        super(parentShell);
        setHelpAvailable(false);
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        this.okButton = getButton(IDialogConstants.OK_ID);
        okButton.setText("Sign in");
    }

    /**
     * Create contents of the dialog.
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        setMessage(AZURE_SIGN_IN);
        setTitle(AZURE_SIGN_IN);
        getShell().setText(AZURE_SIGN_IN);
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayout(new FillLayout(SWT.HORIZONTAL));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite composite = new Composite(container, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        this.authTypeGroup = new Group(composite, SWT.NONE);
        authTypeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        authTypeGroup.setText("Authentication Method");
        authTypeGroup.setLayout(new GridLayout(1, false));
        cliBtn = createRadioButton(authTypeGroup, "Azure CLI (checking...)", AuthType.AZURE_CLI);
        cliDesc = createDescriptionLabel(authTypeGroup, cliBtn, "Consume your existing Azure CLI credential.");

        oauthBtn = createRadioButton(authTypeGroup, "OAuth2", AuthType.OAUTH2);
        oauthDesc = createDescriptionLabel(authTypeGroup, oauthBtn, "You will need to open an external browser and sign in.");

        deviceBtn = createRadioButton(authTypeGroup, "Device Login", AuthType.DEVICE_CODE);
        deviceDesc = createDescriptionLabel(authTypeGroup, deviceBtn, "You will need to open an external browser and sign in with a generated device code.");

        spBtn = createRadioButton(authTypeGroup, "Service Principal", AuthType.SERVICE_PRINCIPAL);
        spDesc = createDescriptionLabel(authTypeGroup, spBtn, "Use Azure Active Directory service principal for sign in.");

        this.updateSelection();
        checkAccountAvailability();
        return area;
    }

    private Button createRadioButton(Composite parent, String label, AuthType type) {
        final Button radioButton = new Button(parent, SWT.RADIO);
        radioButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        radioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                SignInWindow.this.updateSelection();
            }
        });
        radioButton.setText(label);
        return radioButton;
    }

    private Label createDescriptionLabel(Composite parent, Button button, String description) {
        Composite compositeDevice = new Composite(parent, SWT.NONE);
        GridData gdCompositeDevice = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdCompositeDevice.heightHint = 38;
        gdCompositeDevice.widthHint = 66;
        compositeDevice.setLayoutData(gdCompositeDevice);
        compositeDevice.setLayout(new GridLayout(1, false)          );
        Label label = new Label(compositeDevice, SWT.WRAP); 
        GridData gdLblDeviceInfo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdLblDeviceInfo.horizontalIndent = 11;
        label.setLayoutData(gdLblDeviceInfo);
        label.setText(description);
        AccessibilityUtils.addAccessibilityNameForUIComponent(button, button.getText() + " "+ description);
        return label;
        //
    }

    private void checkAccountAvailability() {
        // only azure cli need availability check.
        this.oauthBtn.setEnabled(true);
        this.deviceBtn.setEnabled(true);
        this.spBtn.setEnabled(true);
        this.cliBtn.setText("Azure CLI (checking...)");
        this.cliBtn.setEnabled(false);
        AzureTaskManager.getInstance().runOnPooledThread(() -> {
            final boolean available = AuthType.AZURE_CLI.checkAvailable();
            cliBtn.setEnabled(available);
            cliBtn.setText(available ? "Azure CLI" : "Azure CLI (Not logged in)");
            if (cliBtn.getSelection() && !available) {
                oauthBtn.setSelection(true);
            }
            updateSelection();
        });
    }

    private void updateSelection() {
        boolean selectionAvailable = false;
        for (final Control control : authTypeGroup.getChildren()) {
        	final Button button = (Button) control;
            ((Label) button.getData(DESC)).setEnabled(button.getSelection() && button.isEnabled());
            selectionAvailable = selectionAvailable || (button.getSelection() && button.isEnabled());
        }
        this.okButton.setEnabled(selectionAvailable);
    }

    public AuthType getData() {
    	if(this.cliBtn.getSelection()) {
    		return AuthType.AZURE_CLI;
    	}
    	if(this.oauthBtn.getSelection()) {
    		return AuthType.OAUTH2;
    	}
    	if(this.deviceBtn.getSelection()) {
    		return AuthType.DEVICE_CODE;
    	}
    	if(this.spBtn.getSelection()) {
    		return AuthType.SERVICE_PRINCIPAL;
    	}
        throw new AzureToolkitRuntimeException("No auth type is selected");
    }
}
