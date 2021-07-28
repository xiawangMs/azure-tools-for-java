/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.editors.webapp;

import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.WebAppPropertyViewPresenter;

public class WebAppPropertyEditor extends WebAppBasePropertyEditor {
    public static final String ID = "com.microsoft.azuretools.azureexplorer.editors.webapp.WebAppPropertyEditor";

    public WebAppPropertyEditor() {
        super(new WebAppPropertyViewPresenter());
    }

}
