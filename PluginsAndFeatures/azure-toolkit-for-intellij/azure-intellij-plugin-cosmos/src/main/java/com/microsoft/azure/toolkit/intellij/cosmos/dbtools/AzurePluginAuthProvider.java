/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.cosmos.dbtools;

import com.intellij.database.access.DatabaseCredentials;
import com.intellij.database.dataSource.DatabaseConnectionConfig;
import com.intellij.database.dataSource.DatabaseCredentialsAuthProvider;
import com.intellij.database.dataSource.LocalDataSource;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.microsoft.azure.toolkit.intellij.cosmos.dbtools.AzureCosmosDbAccountParamEditor.KEY_COSMOS_ACCOUNT_ID;

@Getter
@SuppressWarnings("UnstableApiUsage")
public class AzurePluginAuthProvider extends DatabaseCredentialsAuthProvider {
    public static final String ID = "azure_plugin_auto";
    private final String id = ID;

    @Override
    public @Nls @NotNull String getDisplayName() {
        return "Auto (by Azure Toolkit for IntelliJ)";
    }

    @Override
    public @Nullable AuthWidget createWidget(@Nullable Project project, @NotNull DatabaseCredentials credentials, @NotNull DatabaseConnectionConfig config) {
        return new AzurePluginAuthWidget(credentials, config);
    }

    @Override
    public boolean isApplicable(@NotNull LocalDataSource localDataSource, @NotNull ApplicabilityLevel level) {
        if (localDataSource.getDbms() == Dbms.AZ_COSMOS_MONGO) {
            if (level == ApplicabilityLevel.PREFERRED) {
                final String accountId = localDataSource.getAdditionalProperty(KEY_COSMOS_ACCOUNT_ID);
                return StringUtils.isNotBlank(accountId);
            } else if (level.isApplicableOrDefault()) {
                return Azure.az(AzureAccount.class).isLoggedIn();
            }
            return true;
        }
        return false;
    }
}
