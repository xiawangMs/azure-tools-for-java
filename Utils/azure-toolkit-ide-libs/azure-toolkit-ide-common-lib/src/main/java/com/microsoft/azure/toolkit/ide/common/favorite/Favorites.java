/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.common.favorite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.toolkit.ide.common.IExplorerNodeProvider;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.common.component.AzureModuleLabelView;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcons;
import com.microsoft.azure.toolkit.ide.common.store.AzureStoreManager;
import com.microsoft.azure.toolkit.ide.common.store.IMachineStore;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.Account;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResourceModule;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class Favorites extends AbstractAzResourceModule {
    private static final String FAVORITE_ICON = AzureIcons.Common.FAVORITE.getIconPath();
    private static final String FAVORITE_GROUP = "{favorites_group}";
    @Getter
    private static final Favorites instance = new Favorites();
    public static final String NAME = "favorites";
    List<String> favorites = new LinkedList<>();

    private Favorites() {
        super(NAME, AzResource.NONE);
        AzureEventBus.on("account.logged_out.account", new AzureEventBus.EventListener((e) -> {
            this.clear();
            this.refresh();
        }));
        AzureEventBus.on("account.logged_in.account", new AzureEventBus.EventListener((e) -> this.refresh()));
    }

    @Override
    public synchronized void clear() {
        super.clear();
        this.favorites.clear();
    }

    @Nonnull
    @Override
    public synchronized List<AbstractAzResource<?, ?, ?>> list() {
        if (!Azure.az(AzureAccount.class).isLoggedIn()) {
            return Collections.emptyList();
        }
        final List<AbstractAzResource<?, ?, ?>> result = super.list();
        result.sort(Comparator.comparing(item -> this.favorites.indexOf(item.getName().toLowerCase())));
        return result;
    }

    @Override
    protected void reloadResources() {
        final Account account = Azure.az(AzureAccount.class).account();
        final String user = account.getUsername();
        final IMachineStore store = AzureStoreManager.getInstance().getMachineStore();
        final String favorites = store.getProperty(this.getName(), user);
        if (StringUtils.isNotBlank(favorites)) {
            final ObjectMapper mapper = new ObjectMapper();
            try {
                this.favorites = new LinkedList<>(Arrays.asList(mapper.readValue(favorites, String[].class))).stream()
                    .map(String::toLowerCase).distinct().collect(Collectors.toList());
            } catch (final JsonProcessingException ex) {
                AzureMessager.getMessager().error("failed to load favorites.");
                this.favorites = new LinkedList<>();
            }
        }
        this.favorites.stream().filter(id -> this.favorites.contains(id.toLowerCase()))
            .map(id -> Azure.az().getById(id)).filter(Objects::nonNull)
            .forEach(f -> this.addResourceToLocal(f.getId(), f));
    }

    @Nullable
    @Override
    protected AbstractAzResource<?, ?, ?> loadResourceFromAzure(@Nonnull String resourceId, @Nullable String resourceGroup) {
        throw new AzureToolkitRuntimeException("not supported operation.");
    }

    @Override
    protected void deleteResourceFromAzure(@Nonnull String resourceId) {
        this.favorites.remove(resourceId.toLowerCase());
        this.persist();
    }

    protected void addResourceToAzure(@Nonnull String resourceId) {
        this.favorites.add(0, resourceId.toLowerCase());
        this.persist();
    }

    public synchronized void pin(@Nonnull AbstractAzResource<?, ?, ?> resource) {
        if (this.exists(resource.getId())) {
            final String message = String.format("%s '%s' is already pinned.", resource.getResourceTypeName(), resource.getName());
            AzureMessager.getMessager().warning(message);
            return;
        }
        this.addResourceToLocal(resource.getId(), resource);
        this.addResourceToAzure(resource.getId());
    }

    public synchronized void unpin(@Nonnull String resourceId) {
        this.deleteResourceFromLocal(resourceId);
        this.deleteResourceFromAzure(resourceId);
    }

    @Nonnull
    @Override
    @SneakyThrows
    public String toResourceId(@Nonnull String resourceId, @Nullable String resourceGroup) {
        return resourceId;
    }

    @Nonnull
    @Override
    protected AbstractAzResource<?, ?, ?> newResource(@Nonnull Object r) {
        if (r instanceof AbstractAzResource) {
            return (AbstractAzResource<?, ?, ?>) r;
        }
        throw new AzureToolkitRuntimeException("not supported operation.");
    }

    @Nonnull
    @Override
    protected AbstractAzResource<?, ?, ?> newResource(@Nonnull String name, @Nullable String resourceGroupName) {
        throw new AzureToolkitRuntimeException("not supported operation.");
    }

    @Nonnull
    @Override
    public String getResourceTypeName() {
        return "Favorites";
    }

    public boolean exists(@Nonnull String resourceId) {
        return this.favorites.contains(resourceId.toLowerCase());
    }

    public void unpinAll() {
        this.clear();
        this.persist();
        this.refresh();
    }

    public void persist() {
        AzureTaskManager.getInstance().runOnPooledThread(() -> {
            final IMachineStore store = AzureStoreManager.getInstance().getMachineStore();
            final Account account = Azure.az(AzureAccount.class).account();
            final String user = account.getUsername();
            final ObjectMapper mapper = new ObjectMapper();
            try {
                store.setProperty(this.getName(), user, mapper.writeValueAsString(this.favorites));
            } catch (final JsonProcessingException e) {
                AzureMessager.getMessager().error("failed to persist favorites.");
            }
        });
    }

    public static Node<Favorites> buildFavoriteRoot(IExplorerNodeProvider.Manager manager) {
        final AzureActionManager am = AzureActionManager.getInstance();
        final AzureActionManager.Shortcuts shortcuts = am.getIDEDefaultShortcuts();

        final ActionView.Builder unpinAllView = new ActionView.Builder("Unmark All As Favorite", AzureIcons.Action.UNPIN.getIconPath())
            .enabled(s -> s instanceof Favorites);
        final Consumer<Favorites> unpinAllHandler = Favorites::unpinAll;
        final Action.Id<Favorites> UNPIN_ALL = Action.Id.of("resource.unpin_all");
        final Action<Favorites> unpinAllAction = new Action<>(UNPIN_ALL, unpinAllHandler, unpinAllView);
        unpinAllAction.setShortcuts("control F11");

        final AzureModuleLabelView<Favorites> rootView = new AzureModuleLabelView<>(Favorites.getInstance(), "Favorites", FAVORITE_ICON);
        return new Node<>(Favorites.getInstance(), rootView).lazy(false)
            .actions(new ActionGroup(unpinAllAction, "---", ResourceCommonActionsContributor.REFRESH))
            .addChildren(Favorites::list, (o, parent) -> {
                final Node<?> node = manager.createNode(o, parent, IExplorerNodeProvider.ViewType.APP_CENTRIC);
                if (node.view() instanceof AzureResourceLabelView) {
                    node.view(new FavoriteNodeView((AzureResourceLabelView<?>) node.view()));
                }
                return node;
            });
    }
}
