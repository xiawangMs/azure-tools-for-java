/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common;

import javax.annotation.Nonnull;
import javax.swing.*;

public class AzureTextInput extends BaseAzureTextInput<String> {
    public AzureTextInput() {
        super();
    }

    public AzureTextInput(@Nonnull JTextField comp) {
        super(comp);
    }

    @Override
    public String getValue() {
        return this.getText();
    }

    @Override
    public void setValue(final String val) {
        this.setText(val);
    }
}
