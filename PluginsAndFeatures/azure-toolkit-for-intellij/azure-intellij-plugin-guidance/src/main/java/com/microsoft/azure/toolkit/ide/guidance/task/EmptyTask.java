package com.microsoft.azure.toolkit.ide.guidance.task;

import com.microsoft.azure.toolkit.ide.guidance.Task;

import javax.annotation.Nonnull;

public class EmptyTask implements Task {

    @Override
    public void execute() throws Exception {

    }

    @Nonnull
    @Override
    public String getName() {
        return "task.common.nop";
    }
}
