/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.springcloud;

import com.microsoft.azure.management.appplatform.v2019_05_01_preview.implementation.ServiceResourceInner;
import com.microsoft.azuretools.core.mvp.ui.base.MvpView;

import java.util.List;

public interface SpringCloudModuleView extends MvpView {
    void renderChildren(List<ServiceResourceInner> springCloudServices);
}
