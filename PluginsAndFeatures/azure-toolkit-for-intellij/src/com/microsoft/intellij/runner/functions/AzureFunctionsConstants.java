/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner.functions;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class AzureFunctionsConstants {
    public static final String DISPLAY_NAME = "Azure Functions";
    public static final String AZURE_FUNCTIONS_ICON = "azure-functions-small.png";

    public static final Map<String, String> HINT = new HashMap<String, String>() {{
            put("AzureWebJobsStorage", "The Azure Functions runtime uses this storage account connection " +
                    "string for all functions except for HTTP triggered functions.");
            put("FUNCTIONS_WORKER_RUNTIME", "The language worker runtime to load in the function app.");
        }};

    public static String getAppSettingHint(String appSettingKey) {
        return HINT.containsKey(appSettingKey) ? HINT.get(appSettingKey) : StringUtils.EMPTY;
    }
}
