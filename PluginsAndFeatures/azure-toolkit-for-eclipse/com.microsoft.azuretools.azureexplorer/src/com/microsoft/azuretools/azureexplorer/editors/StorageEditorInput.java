/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.editors;

import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.tooling.msservices.model.storage.ClientStorageAccount;
import com.microsoft.tooling.msservices.model.storage.StorageServiceTreeItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class StorageEditorInput implements IEditorInput {
    private String storageAccount;
    private String connectionString;
    private StorageServiceTreeItem item;

    public StorageEditorInput(String storageAccount, String connectionString, StorageServiceTreeItem item) {
        this.storageAccount = storageAccount;
        this.connectionString = connectionString;
        this.item = item;
    }

    public String getStorageAccount() {
        return storageAccount;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public StorageServiceTreeItem getItem() {
        return item;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return null;
    }

    @Override
    public Object getAdapter(Class aClass) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StorageEditorInput that = (StorageEditorInput) o;

        if (!storageAccount.equals(that.storageAccount) || !connectionString.equals(that.connectionString))
            return false;
        return item.getName().equals(that.item.getName());
    }

    @Override
    public int hashCode() {
        int result = connectionString.hashCode();
        result = 31 * result + item.getName().hashCode();
        return result;
    }
}
