/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.cosmos.dbtools;

import com.intellij.database.access.DatabaseCredentials;
import com.intellij.database.dataSource.DatabaseConnectionConfig;
import com.intellij.database.dataSource.DatabaseCredentialsAuthProviderUi.UserPassWidget;
import com.intellij.database.dataSource.DatabaseCredentialsAuthProviderUi.UserWidget;
import com.intellij.database.dataSource.DatabasePasswordField;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.lang.reflect.Field;

@Getter
public class AzurePluginAuthWidget extends UserPassWidget {
    private JBPasswordField passwordField;
    private JBTextField userField;
    private boolean rendered = false;

    public AzurePluginAuthWidget(@NotNull DatabaseCredentials credentials, @NotNull DatabaseConnectionConfig config) {
        super(credentials, config);
    }

    @SneakyThrows
    @Override
    protected JPanel createPanel() {
        final JPanel panel = super.createPanel();
        this.rendered = true;
        final Field fPasswordField = FieldUtils.getField(UserPassWidget.class, "myPasswordField", true);
        final Field fUserField = FieldUtils.getField(UserWidget.class, "myUserField", true);
        this.passwordField = ((DatabasePasswordField) FieldUtils.readField(fPasswordField, this)).getComponent();
        this.userField = (JBTextField) FieldUtils.readField(fUserField, this);
        passwordField.setEnabled(false);
        userField.setEnabled(false);
        return panel;
    }
}
