package com.microsoft.azure.toolkit.intellij.facet.projectexplorer;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.util.messages.MessageBusConnection;
import com.microsoft.azure.toolkit.ide.common.IExplorerNodeProvider;
import com.microsoft.azure.toolkit.intellij.connector.Connection;
import com.microsoft.azure.toolkit.intellij.connector.ConnectionTopics;
import com.microsoft.azure.toolkit.intellij.connector.Resource;
import com.microsoft.azure.toolkit.intellij.connector.ResourceConnectionActionsContributor;
import com.microsoft.azure.toolkit.intellij.connector.dotazure.AzureModule;
import com.microsoft.azure.toolkit.intellij.explorer.AzureExplorer;
import com.microsoft.azure.toolkit.lib.auth.AzureToolkitAuthenticationException;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.action.IActionGroup;
import com.microsoft.azure.toolkit.lib.common.event.AzureEvent;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static com.microsoft.azure.toolkit.intellij.connector.ConnectionTopics.CONNECTION_CHANGED;

public class LocalConnectionsNode extends AbstractTreeNode<AzureModule> implements IAzureFacetNode {

    public LocalConnectionsNode(@Nonnull final AzureModule module) {
        super(module.getProject(), module);
        final AzureEventBus.EventListener listener = new AzureEventBus.EventListener(this::onEvent);
        final MessageBusConnection connection = module.getProject().getMessageBus().connect();
        connection.subscribe(CONNECTION_CHANGED, (ConnectionTopics.ConnectionChanged) (p, conn, action) -> {
            if (conn.getConsumer().getId().equalsIgnoreCase(module.getName())) {
                refresh();
            }
        });
        AzureEventBus.on("connector.refreshed.module_connections", listener);
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
            final ArrayList<AbstractTreeNode<?>> list = new ArrayList<>();
            if (e instanceof AzureToolkitAuthenticationException) {
                list.add(new ActionNode<>(this.myProject, Action.AUTHENTICATE));
            } else {
                list.add(new ExceptionNode(this.myProject, e));
            }
            return list;
        }
    }

    @Override
    protected void update(@Nonnull final PresentationData presentation) {
        presentation.setPresentableText("Resource Connections");
        presentation.setIcon(AllIcons.Nodes.HomeFolder);
    }

    private void onEvent(AzureEvent azureEvent) {
        final Object payload = azureEvent.getSource();
        if (payload instanceof AzureModule && Objects.equals(payload, getValue())) {
            refresh();
        }
    }

    private void refresh() {
        final AbstractProjectViewPane currentProjectViewPane = ProjectView.getInstance(getProject()).getCurrentProjectViewPane();
        currentProjectViewPane.updateFromRoot(true);
    }

    @Override
    @Nullable
    public Object getData(@Nonnull String dataId) {
        return StringUtils.equalsIgnoreCase(dataId, "ACTION_SOURCE") ? this.getValue() : null;
    }

    @Nullable
    @Override
    public IActionGroup getActionGroup() {
        return AzureActionManager.getInstance().getGroup(ResourceConnectionActionsContributor.EXPLORER_MODULE_LOCAL_CONNECTIONS_ACTIONS);
    }
}