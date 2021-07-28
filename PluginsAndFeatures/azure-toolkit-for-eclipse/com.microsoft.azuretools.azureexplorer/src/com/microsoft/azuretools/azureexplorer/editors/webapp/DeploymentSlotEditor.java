/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.editors.webapp;

import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.deploymentslot.DeploymentSlotPropertyViewPresenter;

public class DeploymentSlotEditor extends WebAppBasePropertyEditor {

    public static final String ID = "com.microsoft.azuretools.azureexplorer.editors.webapp.DeploymentSlotEditor";

    public DeploymentSlotEditor() {
        super(new DeploymentSlotPropertyViewPresenter());
    }

}
