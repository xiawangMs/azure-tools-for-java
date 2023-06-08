package com.microsoft.azure.toolkit.intellij.facet.projectexplorer;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

public class WarningNode extends AbstractTreeNode<String> {
    public WarningNode(@Nonnull Project project, final String warningMessage) {
        super(project, warningMessage);
    }

    @Override
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        return Collections.emptyList();
    }

    @Override
    protected void update(@Nonnull final PresentationData presentation) {
        presentation.addText(this.getValue(), SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES);
    }
}

