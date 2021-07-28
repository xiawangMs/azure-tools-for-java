/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.editors.webapp;

import java.util.Objects;

public class WebAppPropertyEditorInput extends WebAppBasePropertyEditorInput {

    private String id;
    private String subscriptionId;
    private String webappName;

    /**
     * Constructor.
     * @param sid   Subscription ID
     * @param id    Resource ID
     * @param name  Resource Name
     */
    public WebAppPropertyEditorInput(String sid, String id, String name) {
        this.id = id;
        this.subscriptionId = sid;
        this.webappName = name;
    }

    @Override
    public String getName() {
        return this.webappName;
    }

    public String getId() {
        return id;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WebAppPropertyEditorInput that = (WebAppPropertyEditorInput) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
