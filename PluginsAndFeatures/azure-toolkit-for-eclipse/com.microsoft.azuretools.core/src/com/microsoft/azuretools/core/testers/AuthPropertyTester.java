/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.testers;

import org.eclipse.core.expressions.PropertyTester;

import com.microsoft.azuretools.authmanage.IdeAzureAccount;

public class AuthPropertyTester extends PropertyTester {
    public static final String PROPERTY_NAMESPACE = "com.microsoft.azuretools.core.testers";
    public static final String PROPERTY_IS_SIGNED_IN = "isSignedIn";

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

        if (PROPERTY_IS_SIGNED_IN.equals(property)) {
            try {
                return IdeAzureAccount.getInstance().isLoggedIn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }
}
