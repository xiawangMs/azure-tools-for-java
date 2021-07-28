/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azurecommons.tasks;

import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;

public interface CancellableTask {
    interface CancellationHandle {
        boolean isCancelled();
    }

    interface CancellableTaskHandle extends CancellationHandle {
        boolean isFinished();

        boolean isSuccessful();

        @Nullable
        Throwable getException();

        void cancel();
    }

    void run(CancellationHandle cancellationHandle) throws Throwable;

    void onCancel();

    void onSuccess();

    void onError(@NotNull Throwable exception);
}
