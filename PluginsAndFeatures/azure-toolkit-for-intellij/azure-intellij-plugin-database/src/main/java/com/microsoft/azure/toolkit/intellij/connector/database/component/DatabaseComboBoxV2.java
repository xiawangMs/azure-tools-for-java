/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.database.component;

import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.database.entity.IDatabase;
import com.microsoft.azure.toolkit.lib.database.entity.IDatabaseServer;
import lombok.Getter;

import java.util.Objects;

public class DatabaseComboBoxV2 extends AzureComboBox<IDatabase> {

    @Getter
    private IDatabaseServer server;

    public void setServer(IDatabaseServer server) {
        if (Objects.equals(server, this.server)) {
            return;
        }
        this.server = server;
        if (server == null) {
            this.clear();
            return;
        }
        this.refreshItems();
    }

    @Override
    protected String getItemText(Object item) {
        return Objects.nonNull(item) && item instanceof IDatabase ? ((IDatabase) item).getName() : super.getItemText(item);
    }

    @Override
    public boolean isRequired() {
        return true;
    }
}
