/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.cosmos.dbtools;

import com.intellij.icons.AllIcons;

public class Dbms {
    public static final com.intellij.database.Dbms AZ_COSMOS_MONGO = com.intellij.database.Dbms.create("AZ_COSMOS_MONGO", "Azure Cosmos DB (MongoDB)", () -> AllIcons.Providers.Azure);
    public static final com.intellij.database.Dbms AZ_COSMOS_CASSANDRA = com.intellij.database.Dbms.create("AZ_COSMOS_CASSANDRA", "Azure Cosmos DB (Cassandra)", () -> AllIcons.Providers.Azure);
}
