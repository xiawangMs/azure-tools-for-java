/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.ijidea.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.microsoft.azuretools.authmanage.interact.INotification;

import javax.swing.*;

/**
 * Created by shch on 10/12/2016.
 */
public class NotificationWindow implements INotification {
    @Override
    public void deliver(String subject, String message) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                JPanel panel = new JPanel();
                Messages.showMessageDialog(panel, message, subject, AllIcons.General.Information);
            }
        });

    }
}
