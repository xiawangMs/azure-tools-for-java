package com.microsoft.azure.toolkit.ide.guidance.phase;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.microsoft.azure.toolkit.ide.guidance.Course;
import com.microsoft.azure.toolkit.ide.guidance.Phase;
import com.microsoft.azure.toolkit.ide.guidance.config.PhaseConfig;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PhaseManager {
    private static final ExtensionPointName<GuidancePhaseProvider> exPoints =
        ExtensionPointName.create("com.microsoft.tooling.msservices.intellij.azure.guidancePhaseProvider");

    private static List<GuidancePhaseProvider> providers;

    public static Phase createPhase(@Nonnull final PhaseConfig type, @Nonnull final Course course) {
        return getTaskProviders().stream()
            .map(provider -> provider.createPhase(type, course))
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new AzureToolkitRuntimeException("Unsupported phase type"));
    }

    public static JPanel createPhasePanel(@Nonnull Phase phase) {
        return getTaskProviders().stream()
            .map(provider -> provider.createPhasePanel(phase))
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new AzureToolkitRuntimeException("Unsupported phase type"));
    }

    public synchronized static List<GuidancePhaseProvider> getTaskProviders() {
        if (CollectionUtils.isEmpty(providers)) {
            providers = exPoints.extensions().collect(Collectors.toList());
        }
        return providers;
    }
}
