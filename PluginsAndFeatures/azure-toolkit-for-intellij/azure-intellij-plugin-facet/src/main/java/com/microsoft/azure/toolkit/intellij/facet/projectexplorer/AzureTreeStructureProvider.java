package com.microsoft.azure.toolkit.intellij.facet.projectexplorer;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ClientProperty;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.AzureModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.intellij.ui.AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED;

public final class AzureTreeStructureProvider implements TreeStructureProvider {
    private final Project myProject;

    public AzureTreeStructureProvider(Project project) {
        myProject = project;
        final AbstractProjectViewPane currentProjectViewPane = ProjectView.getInstance(project).getCurrentProjectViewPane();
        final JTree tree = currentProjectViewPane.getTree();
        ClientProperty.put(tree, ANIMATION_IN_RENDERER_ALLOWED, true);
    }

    @Override
    @Nonnull
    public Collection<AbstractTreeNode<?>> modify(@Nonnull AbstractTreeNode<?> parent, @Nonnull Collection<AbstractTreeNode<?>> children, ViewSettings settings) {
        final AzureModule azureModule = getIfAzureModule(parent);
        if (Objects.nonNull(azureModule)) {
            final AbstractTreeNode<?> dotAzureDir = children.stream()
                .filter(n -> n instanceof PsiDirectoryNode)
                .map(n -> ((PsiDirectoryNode) n))
                .filter(d -> Objects.nonNull(d.getVirtualFile()) && ".azure".equalsIgnoreCase(d.getVirtualFile().getName()))
                .findAny().orElse(null);
            final List<AbstractTreeNode<?>> nodes = new LinkedList<>();
            nodes.add(new AzureRootNode(azureModule, dotAzureDir));
            nodes.addAll(children);
            nodes.removeIf(n -> Objects.equals(n, dotAzureDir));
            return nodes;
        }
        return children;
    }

    @Nullable
    private AzureModule getIfAzureModule(final AbstractTreeNode<?> parent) {
        if (parent instanceof PsiDirectoryNode) {
            final VirtualFile file = ((PsiDirectoryNode) parent).getValue().getVirtualFile();
            final Module module = ModuleUtil.findModuleForFile(file, myProject);
            if (Objects.nonNull(module) && Objects.equals(ProjectUtil.guessModuleDir(module), file)) {
                final AzureModule azureModule = AzureModule.from(module);
                if (azureModule.isInitialized()) {
                    return azureModule;
                }
            }
        }
        return null;
    }
}


