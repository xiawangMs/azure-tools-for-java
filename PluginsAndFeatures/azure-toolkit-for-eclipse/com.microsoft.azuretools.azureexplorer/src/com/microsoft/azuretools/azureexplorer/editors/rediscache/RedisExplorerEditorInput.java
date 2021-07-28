/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.editors.rediscache;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class RedisExplorerEditorInput implements IEditorInput {

    private String id;
    private String subscriptionId;
    private String redisName;

    public RedisExplorerEditorInput(String sid, String id, String name) {
        this.id = id;
        this.subscriptionId = sid;
        this.redisName = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof RedisExplorerEditorInput) {
            RedisExplorerEditorInput input = (RedisExplorerEditorInput) o;
            return this.id.equals(input.getId());
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
        return this.redisName;
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

    public void setId(String id) {
        this.id = id;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getRedisName() {
        return redisName;
    }

    public void setRedisName(String redisName) {
        this.redisName = redisName;
    }

}
