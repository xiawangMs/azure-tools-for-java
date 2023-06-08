package com.microsoft.azure.toolkit.intellij.facet.projectexplorer;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.microsoft.azure.toolkit.ide.common.IExplorerNodeProvider;
import com.microsoft.azure.toolkit.intellij.connector.Connection;
import com.microsoft.azure.toolkit.intellij.connector.Resource;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.AzureModule;
import com.microsoft.azure.toolkit.intellij.explorer.AzureExplorer;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class LocalConnectionsNode extends AbstractTreeNode<AzureModule> {
    public LocalConnectionsNode(@Nonnull final AzureModule module) {
        super(module.getProject(), module);
    }

    @Override
    @Nonnull
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        try {
            final AzureModule module = Objects.requireNonNull(this.getValue());
            return Optional.of(module).stream()
                .map(AzureModule::getDefaultProfile).filter(Objects::nonNull)
                .flatMap(p -> p.getConnections().stream())
                .map(Connection::getResource)
                .map(Resource::getData)
                .filter(Objects::nonNull)
                .map(d -> AzureExplorer.manager.createNode(d, null, IExplorerNodeProvider.ViewType.APP_CENTRIC))
                .map(r -> new ResourceNode(module.getProject(), r))
                .toList();
        } catch (final Exception e) {
            e.printStackTrace();
            final String message = ExceptionUtils.getRootCauseMessage(e);
            final ArrayList<AbstractTreeNode<?>> list = new ArrayList<>();
            list.add(new WarningNode(this.myProject, message));
            return list;
        }
    }

    @Override
    protected void update(@Nonnull final PresentationData presentation) {
        presentation.setPresentableText("Resource Connections");
        presentation.setIcon(AllIcons.Nodes.HomeFolder);
    }
}