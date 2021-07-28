/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.forms;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.microsoft.azuretools.core.components.AzureDialogWrapper;

public class ViewMessageForm extends AzureDialogWrapper {
    private String content;
    private Text messageTextArea;

    public ViewMessageForm(Shell parentShell, String content) {
        super(parentShell);
        parentShell.setText("View Message");
        this.content = content;
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        container.setLayout(gridLayout);
        GridData gridData = new GridData();
        gridData.widthHint = 350;
        gridData.heightHint = 300;
        container.setLayoutData(gridData);

        messageTextArea = new Text(container, SWT.LEFT | SWT.BORDER | SWT.MULTI);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = 200;
        messageTextArea.setLayoutData(gridData);
        messageTextArea.setText(content);
        messageTextArea.setEditable(false);

        return super.createContents(parent);
    }

    @Override
    protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        if (id == IDialogConstants.CANCEL_ID) {
            return null;
        }
        return super.createButton(parent, id, label, defaultButton);
    }
}
