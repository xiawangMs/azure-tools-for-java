/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.guidance.view.components;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.GraphicsConfig;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.JBColor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.JBUI;
import com.microsoft.azure.toolkit.ide.guidance.Phase;
import com.microsoft.azure.toolkit.ide.guidance.Status;
import com.microsoft.azure.toolkit.ide.guidance.Step;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SummaryPanel extends JPanel {
    // JBUI.CurrentTheme.Tree.Hover.background(true)
    // com.intellij.notification.impl.NotificationComponent.NEW_COLOR
    private static final JBColor BACKGROUND_COLOR = JBColor.namedColor("NotificationsToolwindow.newNotification.background", new JBColor(15134455, 4540746));
    private final Phase phase;
    private JPanel contentPanel;
    private JLabel statusIcon;
    private JLabel titleLabel;
    private JPanel detailsPanel;
    private JTextPane descPanel;
    private boolean focused;

    public SummaryPanel(@Nonnull Phase phase) {
        super();
        this.phase = phase;
        $$$setupUI$$$();
        init();
    }

    private void init() {
        final GridLayoutManager layout = new GridLayoutManager(1, 1);
        final Insets margin = JBUI.insets(8, 8, 10, 8);
        layout.setMargin(margin);
        this.setLayout(layout);
        this.setVisible(false); // invisible in default
        this.add(this.contentPanel, new GridConstraints(0, 0, 1, 1, 0, GridConstraints.ALIGN_FILL, 3, 3, null, null, null, 0));
        //https://stackoverflow.com/questions/7115065/jlabel-vertical-alignment-not-working-as-expected
        this.titleLabel.setBorder(BorderFactory.createEmptyBorder(-2 /*top*/, 0, 0, 0));
        this.titleLabel.setText(this.phase.getTitle());
        this.descPanel.setBorder(null);
        this.descPanel.setText(this.phase.getDescription());
        this.descPanel.setVisible(StringUtils.isNotBlank(this.phase.getDescription()));
        this.initDetailsPanel();
        this.updateStatus(this.phase.getStatus());
        this.phase.addStatusListener(this::updateStatus);
    }

    private void updateStatus(Status status) {
        this.statusIcon.setIcon(AllIcons.General.BalloonInformation);
        this.focused = status == Status.READY || status == Status.RUNNING || status == Status.FAILED || status == Status.SUCCEED;
        this.setVisible(this.focused);
        final Color bgColor = this.focused ? BACKGROUND_COLOR : JBUI.CurrentTheme.ToolWindow.background();
        PhasePanel.doForOffsprings(this.contentPanel, c -> c.setBackground(bgColor));
    }

    protected void paintComponent(@Nonnull Graphics g) {
        super.paintComponent(g);
        if (this.focused) {
            final Graphics2D graphics = (Graphics2D) g;
            final GraphicsConfig config = GraphicsUtil.setupAAPainting(g);
            g.setColor(BACKGROUND_COLOR);
            g.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 10, 10);
            config.restore();
        }
    }

    private void initDetailsPanel() {
        final List<Step> steps = phase.getSteps();
        if (CollectionUtils.isEmpty(steps) || steps.size() == 1) {
            // skip render steps panel for single task
            this.detailsPanel.setVisible(false);
            return;
        }
        this.detailsPanel.setLayout(new GridLayoutManager(steps.size(), 1));
        for (int i = 0; i < steps.size(); i++) {
            final Step step = steps.get(i);
            final HyperlinkLabel hyperlinkLabel = new HyperlinkLabel();
            hyperlinkLabel.setHyperlinkText(StringUtils.capitalize(step.getTitle()));
            hyperlinkLabel.addHyperlinkListener(e -> executeStep(step));
            hyperlinkLabel.setIcon(AllIcons.Ide.External_link_arrow);
            hyperlinkLabel.setIconAtRight(true);
            final GridConstraints gridConstraints = new GridConstraints(i, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0);
            this.detailsPanel.add(hyperlinkLabel, gridConstraints);
        }
    }

    private void executeStep(final Step step) {
        final AzureString title = AzureString.format("run action: %s", step.getTitle());
        AzureTaskManager.getInstance().runInBackground(new AzureTask<>(title, step::execute));
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
    }
}
