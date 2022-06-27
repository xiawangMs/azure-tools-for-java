package com.microsoft.azure.toolkit.ide.guidance.phase;

import com.microsoft.azure.toolkit.ide.guidance.Course;
import com.microsoft.azure.toolkit.ide.guidance.Phase;
import com.microsoft.azure.toolkit.ide.guidance.config.PhaseConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;

public interface GuidancePhaseProvider {
    @Nullable
    Phase createPhase(@Nonnull final PhaseConfig config, @Nonnull final Course course);
    @Nullable

    JPanel createPhasePanel(@Nonnull Phase phase);
}
