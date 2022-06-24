package com.microsoft.azure.toolkit.ide.guidance.view;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBFont;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcons;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceConfigManager;
import com.microsoft.azure.toolkit.ide.guidance.config.SequenceConfig;
import com.microsoft.azure.toolkit.ide.guidance.view.components.SequencePanel;
import com.microsoft.azure.toolkit.intellij.common.IntelliJAzureIcons;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import org.apache.commons.collections.CollectionUtils;
import rx.schedulers.Schedulers;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WelcomeView {
    private JPanel pnlRoot;
    private JPanel pnlProcesses;
    private JLabel lblTitle;
    private JPanel pnlLoading;
    private JLabel lblLoading;

    private final Project project;

    private final List<SequencePanel> sequencePanels = new ArrayList<>();

    public WelcomeView(@Nonnull Project project) {
        this.project = project;
        $$$setupUI$$$();
        init();
        JBList a;
    }

    private void init() {
        this.lblTitle.setFont(JBFont.h2().asBold());
        this.lblLoading.setIcon(IntelliJAzureIcons.getIcon(AzureIcons.Common.REFRESH_ICON));
        AzureTaskManager.getInstance().runInBackgroundAsObservable("Loading lesson", () -> GuidanceConfigManager.getInstance().loadSequenceConfig())
                .subscribeOn(Schedulers.computation())
                .subscribe(processes -> AzureTaskManager.getInstance().runLater(() -> this.fillProcess(processes)));
    }

    private void fillProcess(final List<SequenceConfig> sequenceConfigs) {
        this.sequencePanels.clear();
        this.lblLoading.setVisible(true);
        if (CollectionUtils.isEmpty(sequenceConfigs)) {
            return;
        }
        this.pnlProcesses.setLayout(new GridLayoutManager(sequenceConfigs.size(), 1));
        for (int i = 0; i < sequenceConfigs.size(); i++) {
            final SequencePanel sequencePanel = new SequencePanel(sequenceConfigs.get(i), this.project);
            sequencePanel.getRootPanel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(sequencePanel.getRootPanel(), new SequencePanelListener(sequencePanel));
            this.sequencePanels.add(sequencePanel);
            this.pnlProcesses.add(sequencePanel.getRootPanel(),
                    new GridConstraints(i, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0));
        }
        this.lblLoading.setVisible(false);
        this.sequencePanels.get(0).toggleSelectedStatus(true);
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
    }

    public void setVisible(boolean visible) {
        this.pnlRoot.setVisible(visible);
    }

    private void cleanUpSelection() {
        sequencePanels.forEach(panel -> panel.toggleSelectedStatus(false));
    }

    private void addMouseListener(@Nonnull final JComponent component, @Nonnull MouseListener mouseListener) {
        component.addMouseListener(mouseListener);
        Arrays.stream(component.getComponents()).forEach(child -> {
            if (child instanceof JComponent) {
                addMouseListener((JComponent) child, mouseListener);
            }
        });
    }

    class SequencePanelListener extends MouseAdapter {
        private final SequencePanel panel;

        public SequencePanelListener(@Nonnull final SequencePanel panel) {
            this.panel = panel;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            cleanUpSelection();
            panel.toggleSelectedStatus(true);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            panel.openGuidance();
        }
    }
}
