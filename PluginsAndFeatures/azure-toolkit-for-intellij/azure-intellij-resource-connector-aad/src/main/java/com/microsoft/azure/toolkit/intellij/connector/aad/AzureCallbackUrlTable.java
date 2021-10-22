/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.aad;

import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.ui.ListTableModel;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AzureCallbackUrlTable extends ListTableWithButtons<StringBuilder> implements AzureFormInput<List<StringBuilder>> {
    public AzureCallbackUrlTable() {
        getTableView().getEmptyText().setText(MessageBundle.message("dialog.identity.ad.register_app.callback_url.emptyText"));
    }

    @Override
    protected ListTableModel<StringBuilder> createListModel() {
        return new ListTableModel<>(new NameColumnInfo());
    }

    @Override
    protected StringBuilder createElement() {
        return new StringBuilder();
    }

    @Override
    protected boolean isEmpty(StringBuilder stringBuilder) {
        return stringBuilder.length() == 0;
    }

    @Override
    protected StringBuilder cloneElement(StringBuilder stringBuilder) {
        return new StringBuilder(stringBuilder);
    }

    @Override
    protected boolean canDeleteElement(StringBuilder stringBuilder) {
        return true;
    }

    @Override
    public List<StringBuilder> getValue() {
        return getElements();
    }

    @Override
    public void setValue(List<StringBuilder> val) {
        this.setValues(val);
    }

    @NotNull
    @Override
    public AzureValidationInfo doValidate() {
        var urls = this.getValue();
        if (urls == null || urls.isEmpty()) {
            return AzureFormInput.super.doValidate();
        }

        for (var url : urls) {
            if (!AzureUtils.isValidCallbackURL(url.toString())) {
                return AzureValidationInfo.builder()
                        .input(this)
                        .message(MessageBundle.message("action.azure.aad.registerApp.callbackURLInvalid", url.toString()))
                        .build();
            }
        }

        return AzureValidationInfo.OK;
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public String getLabel() {
        return MessageBundle.message("dialog.identity.ad.register_app.callback_url").replace(":", "");
    }

    private static class NameColumnInfo extends ElementsColumnInfoBase<StringBuilder> {
        protected NameColumnInfo() {
            super("URL");
        }

        @Override
        public void setValue(StringBuilder stringBuilder, @NlsContexts.ListItem String value) {
            stringBuilder.replace(0, stringBuilder.length(), value);
        }

        @Override
        protected @Nullable String getDescription(StringBuilder stringBuilder) {
            return null;
        }

        @Override
        public @Nullable String valueOf(StringBuilder stringBuilder) {
            return stringBuilder.toString();
        }

        @Override
        public boolean isCellEditable(StringBuilder stringBuilder) {
            return true;
        }
    }
}
