package com.microsoft.azure.toolkit.ide.guidance.view.components;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.UIUtil;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceViewManager;
import com.microsoft.azure.toolkit.ide.guidance.config.CourseConfig;
import com.microsoft.azure.toolkit.ide.guidance.view.ViewUtils;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.util.Objects;

public class CoursePanel {
    private final CourseConfig course;
    @Getter
    private JPanel rootPanel;
    private JLabel lblTitle;
    private JTextPane areaDescription;
    private JButton startButton;

    private final Project project;

    public CoursePanel(@Nonnull final CourseConfig course, @Nonnull final Project project) {
        super();
        this.course = course;
        this.project = project;
        $$$setupUI$$$();
        init();
    }

    private void init() {
        this.lblTitle.setFont(JBFont.h4());
        // render course
        // this.lblIcon.setIcon(IntelliJAzureIcons.getIcon(AzureIcons.Common.AZURE));
        this.lblTitle.setText(course.getTitle());
        this.lblTitle.setPreferredSize(new Dimension(-1, startButton.getPreferredSize().height));
        this.startButton.setVisible(false);
        this.startButton.addActionListener(e -> openGuidance());
        this.areaDescription.setFont(JBFont.medium());
        this.areaDescription.setForeground(UIUtil.getLabelInfoForeground());
        this.areaDescription.setText(course.getDescription());
    }

    public void toggleSelectedStatus(final boolean isSelected) {
        if (Objects.equals(isSelected, startButton.isVisible())) {
            return;
        }
        this.startButton.setVisible(isSelected);
        ViewUtils.setBackgroundColor(this.rootPanel, isSelected ? ViewUtils.NOTIFICATION_BACKGROUND_COLOR : UIUtil.getLabelBackground());
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
    }

    public void addMouseListener(@Nonnull final MouseListener coursePanelListener) {
        this.rootPanel.addMouseListener(coursePanelListener);
    }

    @AzureOperation(name = "guidance.open_course.course", params = {"this.course.getTitle()"}, type = AzureOperation.Type.ACTION)
    public void openGuidance() {
        GuidanceViewManager.getInstance().openCourseView(project, course);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        this.rootPanel = new RoundedPanel(5);
    }
}
