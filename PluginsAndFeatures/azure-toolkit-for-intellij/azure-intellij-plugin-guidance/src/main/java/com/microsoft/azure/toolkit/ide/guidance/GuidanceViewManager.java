package com.microsoft.azure.toolkit.ide.guidance;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.microsoft.azure.toolkit.ide.guidance.config.CourseConfig;
import com.microsoft.azure.toolkit.ide.guidance.view.GuidanceView;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class GuidanceViewManager {

    public static final String TOOL_WINDOW_ID = "Get Started with Azure";

    private static final GuidanceViewManager instance = new GuidanceViewManager();

    public static GuidanceViewManager getInstance() {
        return instance;
    }

    public void openCourseView(@Nonnull final Project project, @Nonnull final CourseConfig courseConfig) {
        final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(GuidanceViewManager.TOOL_WINDOW_ID);
        if (toolWindow == null) {
            return;
        }
        AzureTaskManager.getInstance().runLater(() -> {
            toolWindow.show();
            final GuidanceView guidanceView = GuidanceViewFactory.getGuidanceView(project);
            if (Objects.nonNull(guidanceView)) {
                final Course course = GuidanceViewManager.createCourse(courseConfig, project);
                guidanceView.showCourseView(course);
            }
        });
    }

    public void showCoursesView(@Nonnull final Project project) {
        final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(GuidanceViewManager.TOOL_WINDOW_ID);
        if (toolWindow == null) {
            return;
        }
        AzureTaskManager.getInstance().runLater(() -> {
            toolWindow.show();
            final GuidanceView guidanceView = GuidanceViewFactory.getGuidanceView(project);
            if (Objects.nonNull(guidanceView)) {
                guidanceView.showCoursesView();
            }
        });
    }

    public void closeCourseView(@Nonnull final Project project) {
        final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(GuidanceViewManager.TOOL_WINDOW_ID);
        if (toolWindow == null) {
            return;
        }
        AzureTaskManager.getInstance().runLater(() -> {
            final GuidanceView guidanceView = GuidanceViewFactory.getGuidanceView(project);
            if (Objects.nonNull(guidanceView)) {
                guidanceView.showCoursesView();
            }
            toolWindow.hide();
        });
    }

    private static Course createCourse(@Nonnull final CourseConfig config, @Nonnull Project project) {
        final Course course = new Course(config, project);
        AzureTaskManager.getInstance().runOnPooledThread(course::prepare);
        return course;
    }

    public static class GuidanceViewFactory implements ToolWindowFactory, DumbAware {
        private static final Map<Project, GuidanceView> guidanceViewMap = new ConcurrentHashMap<>();

        @Override
        public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
            final GuidanceView view = new GuidanceView(project);
            guidanceViewMap.put(project, view);
            final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
            final Content content = contentFactory.createContent(view, "", false);
            toolWindow.getContentManager().addContent(content);
        }

        public static GuidanceView getGuidanceView(@Nonnull final Project project) {
            return guidanceViewMap.get(project);
        }
    }
}
