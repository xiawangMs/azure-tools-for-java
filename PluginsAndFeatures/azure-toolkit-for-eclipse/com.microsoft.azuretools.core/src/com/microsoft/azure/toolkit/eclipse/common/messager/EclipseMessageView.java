/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.messager;

import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.view.IView;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import java.util.Optional;

public interface EclipseMessageView {

    default void addActionButton(Action<?> action, Composite container) {
        final Button btn = new Button(container, SWT.PUSH);
        final String title = Optional.ofNullable(action.view(null)).map(IView.Label::getLabel).orElse(action.toString());
        btn.setText(title);
        btn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                action.handle(null, e);
            }
        });
    }

    /**
     * refer org.eclipse.jface.dialogs.IconAndMessageDialog#getSWTImage(int)
     */
    default Image getIcon() {
        final Display display = this.getDisplay();
        Assert.isNotNull(display, "The dialog should be created in UI thread");
        int imageId;
        switch (this.getMessage().getType()) {
            case INFO:
            case SUCCESS:
                imageId = 2;
                break;
            case WARNING:
                imageId = 8;
                break;
            case ERROR:
                imageId = 1;
                break;
            default:
                imageId = 4;
        }
        final Image[] image = new Image[1];
        display.syncExec(() -> image[0] = display.getSystemImage(imageId));
        return image[0];
    }

    Display getDisplay();

    EclipseAzureMessage getMessage();
}
