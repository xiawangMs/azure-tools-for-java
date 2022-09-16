package com.microsoft.azure.toolkit.ide.guidance;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.microsoft.azure.toolkit.ide.guidance.view.CourseView;
import com.microsoft.azure.toolkit.ide.guidance.view.components.SummaryPanel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

public class GuidanceToolWindowListener implements ToolWindowManagerListener {
    private final Project project;
    public GuidanceToolWindowListener(Project project) {
        this.project = project;
    }

    @Override
    public void toolWindowShown(@NotNull ToolWindow toolWindow) {
        ToolWindowManagerListener.super.toolWindowShown(toolWindow);
        if (Objects.equals(toolWindow.getId(), GuidanceViewManager.TOOL_WINDOW_ID)) {
            final CourseView courseView = GuidanceViewManager.GuidanceViewFactory.getGuidanceView(project).getPnlCourse();
            if (Objects.nonNull(courseView)) {
                final Component[] components = courseView.getPhasesPanel().getComponents();
                for (final Component component : components) {
                    if (component instanceof SummaryPanel) {
                        ((SummaryPanel) component).updateDefaultButton();
                        break;
                    }
                }
            }
        }
    }
}
