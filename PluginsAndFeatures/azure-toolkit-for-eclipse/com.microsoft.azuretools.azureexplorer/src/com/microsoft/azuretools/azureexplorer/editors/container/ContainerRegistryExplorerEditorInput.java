/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.editors.container;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class ContainerRegistryExplorerEditorInput implements IEditorInput{

    private String id;
    private String subscriptionId;
    private String registryName;

    public ContainerRegistryExplorerEditorInput(String sid, String id, String name) {
        this.id = id;
        this.subscriptionId = sid;
        this.registryName = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ContainerRegistryExplorerEditorInput) {
            ContainerRegistryExplorerEditorInput input = (ContainerRegistryExplorerEditorInput) o;
            return this.getId().equals(input.getId());
        }
        return false;

    }

    @Override
    public <T> T getAdapter(Class<T> arg0) {
        return null;
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
        return this.registryName;
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return null;
    }

    public String getId() {
        return id;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }
}
