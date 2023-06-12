/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.facet.projectexplorer;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.connector.Connection;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.Profile;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EnvironmentVariablesNode extends AbstractTreeNode<Connection<?, ?>> implements IAzureFacetNode {
    @Nonnull
    private final Profile profile;

    public EnvironmentVariablesNode(@Nonnull Project project, @Nonnull Profile profile, @Nonnull Connection<?, ?> connection) {
        super(project, connection);
        this.profile = profile;
    }

    @Override
    @Nonnull
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        final Connection<?, ?> connection = this.getValue();
        final ArrayList<AbstractTreeNode<?>> children = new ArrayList<>();
        final List<Pair<String, String>> generated = this.profile.getGeneratedEnvironmentVariables(connection);
        return generated.stream().map(g -> new EnvironmentVariableNode(this.getProject(), g)).toList();
    }

    @Override
    protected void update(@Nonnull final PresentationData presentation) {
        presentation.setPresentableText("Environment Variables");
        presentation.setIcon(AllIcons.Actions.Properties);
    }

    @Override
    @Nullable
    public Object getData(@Nonnull String dataId) {
        return StringUtils.equalsIgnoreCase(dataId, "ACTION_SOURCE") ? this.getValue() : null;
    }

    @Override
    public String toString() {
        return "Environment Variables";
    }
}