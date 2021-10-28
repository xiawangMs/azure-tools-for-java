/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.messager;

import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class EclipseMessageDialog extends IconAndMessageDialog implements EclipseMessageView {
    @Nonnull
    private final EclipseAzureMessage message;

    public EclipseMessageDialog(@Nonnull EclipseAzureMessage message, Shell shell) {
        super(shell);
        this.message = message;
    }

    protected Control createMessageArea(Composite composite) {
        final Image image = this.getImage();
        if (image != null) {
            this.imageLabel = new Label(composite, 0);
            image.setBackground(this.imageLabel.getBackground());
            this.imageLabel.setImage(image);
            GridDataFactory.fillDefaults().align(16777216, 1).applyTo(this.imageLabel);
        }

        final Browser browser = new Browser(composite, SWT.NONE);
        browser.setText(this.getMessage().getContent());
        browser.setSize(360, 160);
        browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridDataFactory.fillDefaults()
                .align(4, 1)
                .grab(true, false)
                .hint(this.convertHorizontalDLUsToPixels(300), -1)
                .applyTo(browser);
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        Arrays.stream(this.getMessage().getActions()).forEach(a -> this.addActionButton(a, parent));
    }

    @Override
    protected Image getImage() {
        return this.getIcon();
    }

    @Override
    public Display getDisplay() {
        Shell shell = super.getShell();
        if (shell == null || shell.isDisposed()) {
            shell = this.getParentShell();
        }
        return shell != null && !shell.isDisposed() ? shell.getDisplay() : Display.getCurrent();
    }

    @Override
    @Nonnull
    public EclipseAzureMessage getMessage() {
        return message;
    }
}
