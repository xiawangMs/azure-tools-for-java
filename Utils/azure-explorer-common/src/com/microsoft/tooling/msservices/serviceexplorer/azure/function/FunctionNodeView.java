/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.function;

import com.microsoft.azure.management.appservice.FunctionEnvelope;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.base.WebAppBaseNodeView;

import java.util.List;

public interface FunctionNodeView extends WebAppBaseNodeView {
    void renderSubModules(List<FunctionEnvelope> functionEnvelopes);
}
