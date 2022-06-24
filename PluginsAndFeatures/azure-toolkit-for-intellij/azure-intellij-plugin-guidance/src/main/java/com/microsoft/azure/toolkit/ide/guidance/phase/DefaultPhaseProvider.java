package com.microsoft.azure.toolkit.ide.guidance.phase;

import com.microsoft.azure.toolkit.ide.guidance.Guidance;
import com.microsoft.azure.toolkit.ide.guidance.Phase;
import com.microsoft.azure.toolkit.ide.guidance.config.PhaseConfig;
import com.microsoft.azure.toolkit.ide.guidance.view.components.PhasePanel;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.swing.*;

public class DefaultPhaseProvider implements GuidancePhaseProvider {
    @Override
    public Phase createPhase(@Nonnull PhaseConfig config, @Nonnull Guidance guidance) {
        return StringUtils.isEmpty(config.getType()) ? new Phase(config, guidance) : null;
    }

    @Override
    public JPanel createPhasePanel(@Nonnull Phase phase) {
        return StringUtils.isEmpty(phase.getType()) ? new PhasePanel(phase) : null;
    }
}
