package com.microsoft.azure.toolkit.ide.guidance.task;

import com.microsoft.azure.toolkit.ide.guidance.Context;
import com.microsoft.azure.toolkit.ide.guidance.Task;
import com.microsoft.azure.toolkit.ide.guidance.config.TaskConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface GuidanceTaskProvider {
    @Nullable
    Task createTask(@Nonnull final TaskConfig config, @Nonnull final Context context);
}
