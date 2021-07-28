/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.ijidea.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.microsoft.aad.adal4j.AuthenticationCallback;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.DeviceCode;
import com.microsoft.azuretools.adauth.IDeviceLoginUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeviceLoginUI implements IDeviceLoginUI {
    private DeviceLoginWindow deviceLoginWindow;

    @Nullable
    @Override
    public AuthenticationResult authenticate(@NotNull final AuthenticationContext ctx,
                                             @NotNull final DeviceCode deviceCode,
                                             final AuthenticationCallback<AuthenticationResult> callback) {
        ApplicationManager.getApplication().invokeAndWait(() -> buildAndShow(ctx, deviceCode, callback));
        return deviceLoginWindow.getAuthenticationResult();
    }

    private void buildAndShow(@NotNull final AuthenticationContext ctx, @NotNull final DeviceCode deviceCode,
                              final AuthenticationCallback<AuthenticationResult> callback) {
        deviceLoginWindow = new DeviceLoginWindow(ctx, deviceCode, callback);
        deviceLoginWindow.show();
    }
}
