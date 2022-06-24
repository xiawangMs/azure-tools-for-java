package com.microsoft.azure.toolkit.ide.guidance;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.microsoft.azure.toolkit.ide.guidance.config.SequenceConfig;
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

    public void showGuidance(@Nonnull final Project project, @Nonnull final SequenceConfig sequenceConfig) {
        final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(GuidanceViewManager.TOOL_WINDOW_ID);
        if (toolWindow == null) {
            return;
        }
        AzureTaskManager.getInstance().runLater(() -> {
            toolWindow.show();
            final GuidanceView guidanceView = GuidanceViewFactory.getGuidanceView(project);
            if (Objects.nonNull(guidanceView)) {
                final Guidance guidance = GuidanceViewManager.createProcess(sequenceConfig, project);
                guidanceView.showGuidance(guidance);
            }
        });
    }

    public void showGuidanceWelcome(@Nonnull final Project project) {
        final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(GuidanceViewManager.TOOL_WINDOW_ID);
        if (toolWindow == null) {
            return;
        }
        AzureTaskManager.getInstance().runLater(() -> {
            toolWindow.show();
            final GuidanceView guidanceView = GuidanceViewFactory.getGuidanceView(project);
            if (Objects.nonNull(guidanceView)) {
                guidanceView.showWelcomePage();
            }
        });
    }

    public void closeGuidance(@Nonnull final Project project) {
        final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(GuidanceViewManager.TOOL_WINDOW_ID);
        if (toolWindow == null) {
            return;
        }
        AzureTaskManager.getInstance().runLater(() -> {
            final GuidanceView guidanceView = GuidanceViewFactory.getGuidanceView(project);
            if (Objects.nonNull(guidanceView)) {
                guidanceView.showWelcomePage();
            }
            toolWindow.hide();
        });
    }

    private static Guidance createProcess(@Nonnull final SequenceConfig config, @Nonnull Project project) {
        final Guidance guidance = new Guidance(config, project);
        AzureTaskManager.getInstance().runOnPooledThread(guidance::prepare);
        return guidance;
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
