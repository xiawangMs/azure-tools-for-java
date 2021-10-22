/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.aad;

import com.microsoft.azure.toolkit.intellij.common.AzureTextInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Component for the client ID text input.
 */
class AzureClientIdInput extends AzureTextInput {

    public AzureClientIdInput() {
        super();
        this.setValidator(this::doValidateValue);
        this.setRequired(true);
    }

    @Nonnull
    public AzureValidationInfo doValidateValue() {
        final var value = this.getValue();
        if (!isValid(value)) {
            return AzureValidationInfo.builder()
                    .input(this)
                    .message(MessageBundle.message("action.azure.aad.registerApp.clientIdInvalid"))
                    .build();
        }
        return AzureValidationInfo.OK;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static boolean isValid(@Nonnull String value) {
        if (value.isBlank()) {
            return false;
        }

        try {
            UUID.fromString(value);
            return value.length() == 36;
        } catch (Exception e) {
            return false;
        }
    }
}
