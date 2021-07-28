/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.messages;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import com.microsoft.azuretools.core.Activator;


public class MessageHandler {

    private static Activator LOG = Activator.getDefault();
    private static ResourceBundle commonResourceBundle = ResourceBundle.getBundle("com.microsoft.azuretools.azureexplorer.messages.common");

    private static final String BUNDLE_PACKAGE_NAME = "com.microsoft.azuretools.azureexplorer.messages.%s";
    private static final String CANNOT_FIND_BUNDLE_ERROR = "Cannot find package: %s";
    private static final String CANNOT_FIND_KEY_ERROR = "Cannot find key: %s";
    private static final String BUNDLE_OR_KEY_IS_NULL = "Bundle or key is null";

    public static ResourceBundle getResourceBundle(String module) {
        String fullBundleName = String.format(BUNDLE_PACKAGE_NAME, module);
        try {
            return ResourceBundle.getBundle(fullBundleName);
        } catch (MissingResourceException ex){
            //TODO : Exceoption handler
            LOG.log(String.format(CANNOT_FIND_BUNDLE_ERROR, fullBundleName), ex);
        }
        return null;
    }

    public static String getResourceString(ResourceBundle bundle, String key) {
        if (bundle == null || key == null) {
            LOG.log(BUNDLE_OR_KEY_IS_NULL);
            return "";
        }
        try {
            return bundle.getString(key);
        } catch (Exception ex) {
            LOG.log(String.format(CANNOT_FIND_KEY_ERROR, key), ex);
            return key;
        }
    }

    public static String getCommonStr(String key) {
        return getResourceString(commonResourceBundle, key);
    }
}
