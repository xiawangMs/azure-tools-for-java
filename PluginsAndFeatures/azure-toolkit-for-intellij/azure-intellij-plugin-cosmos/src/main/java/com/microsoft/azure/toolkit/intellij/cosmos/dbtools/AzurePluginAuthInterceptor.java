/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.cosmos.dbtools;

import com.intellij.credentialStore.Credentials;
import com.intellij.database.dataSource.DatabaseConnectionInterceptor;
import com.intellij.database.dataSource.DatabaseConnectionPoint;
import com.intellij.database.dataSource.DatabaseCredentialsAuthProvider;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@SuppressWarnings("UnstableApiUsage")
public class AzurePluginAuthInterceptor implements DatabaseConnectionInterceptor {
    @Nullable
    public CompletionStage<ProtoConnection> intercept(@NotNull DatabaseConnectionInterceptor.ProtoConnection proto, boolean silent) {
        final DatabaseConnectionPoint connectionPoint = proto.getConnectionPoint();
        final Credentials credentials = proto.getCredentials().getCredentials(connectionPoint);
        final String username = connectionPoint.getAdditionalProperty(AzureCosmosDbAccountParamEditor.KEY_USERNAME);
        if (StringUtils.isBlank(credentials.getUserName()) && StringUtils.isNotBlank(username)) {
            DatabaseCredentialsAuthProvider.applyCredentials(proto, new Credentials(username, credentials.getPasswordAsString()), false);
            return CompletableFuture.completedFuture(proto);
        } else {
            return null;
        }
    }
}