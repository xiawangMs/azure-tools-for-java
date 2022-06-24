package com.microsoft.azure.toolkit.ide.guidance.task;

import com.microsoft.azure.toolkit.ide.guidance.Context;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceTask;
import com.microsoft.azure.toolkit.ide.guidance.config.TaskConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface GuidanceTaskProvider {
    @Nullable
    GuidanceTask createTask(@Nonnull final TaskConfig config, @Nonnull final Context context);
}
