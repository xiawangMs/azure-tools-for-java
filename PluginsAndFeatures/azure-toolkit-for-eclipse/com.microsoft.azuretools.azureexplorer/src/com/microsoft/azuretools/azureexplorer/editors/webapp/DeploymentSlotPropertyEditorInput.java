/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.editors.webapp;

import java.util.Objects;

public class DeploymentSlotPropertyEditorInput extends WebAppBasePropertyEditorInput {

    private String id;
    private String subscriptionId;
    private String webappId;
    private String slotName;

    public DeploymentSlotPropertyEditorInput(String id, String sid, String webappId, String slotName) {
        this.id = id;
        this.subscriptionId = sid;
        this.webappId = webappId;
        this.slotName = slotName;
    }

    public String getId() {
        return id;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getWebappId() {
        return webappId;
    }

    @Override
    public String getName() {
        return this.slotName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeploymentSlotPropertyEditorInput that = (DeploymentSlotPropertyEditorInput) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
