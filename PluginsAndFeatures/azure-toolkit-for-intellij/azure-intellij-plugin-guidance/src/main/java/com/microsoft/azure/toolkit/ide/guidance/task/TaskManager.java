package com.microsoft.azure.toolkit.ide.guidance.task;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.microsoft.azure.toolkit.ide.guidance.Context;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceTask;
import com.microsoft.azure.toolkit.ide.guidance.config.TaskConfig;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TaskManager {

    private static final ExtensionPointName<GuidanceTaskProvider> exPoints =
        ExtensionPointName.create("com.microsoft.tooling.msservices.intellij.azure.guidanceTaskProvider");
    private static List<GuidanceTaskProvider> providers;

    public synchronized static List<GuidanceTaskProvider> getTaskProviders() {
        if (CollectionUtils.isEmpty(providers)) {
            providers = exPoints.extensions().collect(Collectors.toList());
        }
        return providers;
    }

    public static GuidanceTask createTask(@Nonnull final TaskConfig config, @Nonnull final Context context) {
        return getTaskProviders().stream()
                .map(provider -> provider.createTask(config, context))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new AzureToolkitRuntimeException(String.format("Unsupported task :%s", config.getName())));
    }
}
