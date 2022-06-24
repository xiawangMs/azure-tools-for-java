package com.microsoft.azure.toolkit.ide.guidance.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.microsoft.azure.toolkit.ide.guidance.Guidance;

import javax.annotation.Nonnull;
import javax.swing.*;

public class GuidanceView extends SimpleToolWindowPanel {
    private final Project project;
    private JPanel pnlRoot;
    private SequenceView pnlProcess;
    private WelcomeView pnlWelcome;

    public GuidanceView(final Project project) {
        super(true);
        this.project = project;
        $$$setupUI$$$();
        this.setContent(pnlRoot);
        showWelcomePage();
    }

    public void showWelcomePage() {
        pnlProcess.setVisible(false);
        pnlWelcome.setVisible(true);
    }

    public void showGuidance(@Nonnull Guidance guidance) {
        pnlWelcome.setVisible(false);
        pnlProcess.setVisible(true);
        pnlProcess.showProcess(guidance);
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        this.pnlProcess = new SequenceView(project);
        this.pnlWelcome = new WelcomeView(project);
    }
}
