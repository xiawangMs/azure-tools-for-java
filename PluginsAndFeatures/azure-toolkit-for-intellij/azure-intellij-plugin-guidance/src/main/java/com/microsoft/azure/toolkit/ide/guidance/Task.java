/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.guidance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Task {

    void execute() throws Exception;

    default void prepare() {
    }

    default boolean isDone() {
        return false;
    }

    @Nonnull
    String getName();

    @Nullable
    default String getDescription() {
        return this.getName() + "desc";
    }

    @Nullable
    default String getDoc() {
        return null;
    }
}
