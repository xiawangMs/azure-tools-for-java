package com.microsoft.azure.toolkit.ide.guidance.task;

import com.microsoft.azure.toolkit.ide.guidance.ComponentContext;
import com.microsoft.azure.toolkit.ide.guidance.Task;

import javax.annotation.Nonnull;

public class CleanUpResourceTask implements Task {
    private final ComponentContext taskContext;

    public CleanUpResourceTask(@Nonnull final ComponentContext context) {
        this.taskContext = context;
    }

    @Override
    public void execute() {

    }

    @Nonnull
    @Override
    public String getName() {
        return "task.common.cleanup";
    }
}
