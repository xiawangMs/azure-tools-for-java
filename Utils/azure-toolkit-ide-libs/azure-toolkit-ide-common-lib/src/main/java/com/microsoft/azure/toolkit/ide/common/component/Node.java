/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.common.component;

import com.microsoft.azure.toolkit.ide.common.icon.AzureIcon;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.action.IActionGroup;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.view.IView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Node<D> {
    @Nonnull
    @Getter
    @EqualsAndHashCode.Include
    private final D data;
    @Nullable
    private Function<D, AzureIcon> iconProvider;
    @Nullable
    private Function<D, String> labelProvider;
    @Nullable
    private Function<D, String> descProvider;
    @Nullable
    private Function<D, String> tipsProvider;
    @Nonnull
    private Predicate<D> enableWhen = o -> true;
    @Nonnull
    private Predicate<D> visibleWhen = o -> true;
    @Nonnull
    private final List<BiConsumer<D, Object>> clickHandlers = new ArrayList<>();
    @Nonnull
    private final List<BiConsumer<D, Object>> doubleClickHandlers = new ArrayList<>();
    @Nonnull
    private final List<ChildrenBuilder<D, ?>> childrenBuilders = new ArrayList<>();
    private Consumer<? super D> moreChildrenLoader;
    // by default, we will load all children, so return false for has more child
    @Getter(AccessLevel.NONE)
    private Predicate<? super D> hasMoreChildren = ignore -> false;
    @Getter
    @Nullable
    private IActionGroup actions;
    @Getter
    @Nonnull
    private final List<Action<? super D>> inlineActions = new ArrayList<>();
    @Getter
    @Setter
    @Nullable
    protected ViewChangedListener viewChangedListener;
    @Getter
    @Setter
    @Nullable
    protected ChildrenChangedListener childrenChangedListener;
    @Getter
    private boolean lazy = true;
    @Getter
    private Type type = Type.NORMAL;
    @Getter
    private Order newItemOrder = Order.LIST_ORDER;

    public Node(@Nonnull D data) {
        this.data = data;
    }

    public Node<D> withType(@Nonnull final Type type) {
        this.type = type;
        return this;
    }

    public Node<D> withIcon(@Nonnull final String iconPath) {
        this.iconProvider = (any) -> AzureIcon.builder().iconPath(iconPath).build();
        return this;
    }

    public Node<D> withIcon(@Nonnull final AzureIcon icon) {
        this.iconProvider = (any) -> icon;
        return this;
    }

    public Node<D> withIcon(@Nonnull final Function<D, AzureIcon> iconProvider) {
        this.iconProvider = iconProvider;
        return this;
    }

    public Node<D> withLabel(@Nonnull final String label) {
        this.labelProvider = (any) -> label;
        return this;
    }

    public Node<D> withLabel(@Nonnull final Function<D, String> labelProvider) {
        this.labelProvider = labelProvider;
        return this;
    }

    public Node<D> withDescription(@Nonnull final String desc) {
        this.descProvider = (any) -> desc;
        return this;
    }

    public Node<D> withDescription(@Nonnull final Function<D, String> descProvider) {
        this.descProvider = descProvider;
        return this;
    }


    public Node<D> withTips(@Nonnull final String tips) {
        this.tipsProvider = (any) -> tips;
        return this;
    }

    public Node<D> withTips(@Nonnull final Function<D, String> tipsProvider) {
        this.tipsProvider = tipsProvider;
        return this;
    }

    public Node<D> enableWhen(@Nonnull Predicate<D> enableWhen) {
        this.enableWhen = enableWhen;
        return this;
    }

    public Node<D> visibleWhen(@Nonnull Predicate<D> visibleWhen) {
        this.visibleWhen = visibleWhen;
        return this;
    }

    public Node<D> withChildrenLoadLazily(final boolean lazy) {
        this.lazy = lazy;
        return this;
    }

    public Node<D> withActions(String groupId) {
        return this.withActions(AzureActionManager.getInstance().getGroup(groupId));
    }

    public Node<D> withActions(IActionGroup group) {
        this.actions = group;
        return this;
    }

    public Node<D> onClicked(@Nonnull final BiConsumer<D, Object> clickHandler) {
        this.clickHandlers.add(clickHandler);
        return this;
    }

    public Node<D> onClicked(@Nonnull final Consumer<D> clickHandler) {
        this.clickHandlers.add((d, o) -> clickHandler.accept(d));
        return this;
    }

    public Node<D> onClicked(Action.Id<? super D> actionId) {
        this.clickHandlers.add((d, o) -> AzureActionManager.getInstance().getAction(actionId).handle(d, o));
        return this;
    }

    public void click(final Object event) {
        if (!this.clickHandlers.isEmpty()) {
            this.clickHandlers.forEach(h -> h.accept(this.data, event));
        }
    }

    public Node<D> onDoubleClicked(@Nonnull final BiConsumer<D, Object> dblClickHandler) {
        this.doubleClickHandlers.add(dblClickHandler);
        return this;
    }

    public Node<D> onDoubleClicked(@Nonnull final Consumer<D> dblClickHandler) {
        this.doubleClickHandlers.add((d, o) -> dblClickHandler.accept(d));
        return this;
    }

    public Node<D> onDoubleClicked(Action.Id<? super D> actionId) {
        this.doubleClickHandlers.add((d, o) -> AzureActionManager.getInstance().getAction(actionId).handle(d, o));
        return this;
    }

    public void doubleClick(final Object event) {
        if (!this.doubleClickHandlers.isEmpty()) {
            this.doubleClickHandlers.forEach(h -> h.accept(this.data, event));
        }
    }

    public Node<D> addChildren(@Nonnull List<Node<?>> children) {
        return this.addChildren((d) -> children, (cd, n) -> cd);
    }

    public Node<D> addChildren(@Nonnull Function<? super D, ? extends List<Node<?>>> getChildrenNodes) {
        return this.addChildren(getChildrenNodes, (cd, n) -> cd);
    }

    public <C> Node<D> addChildren(
        @Nonnull Function<? super D, ? extends List<C>> getChildrenData,
        @Nonnull BiFunction<C, Node<D>, Node<?>> buildChildNode) {
        this.childrenBuilders.add(new ChildrenBuilder<>(getChildrenData, buildChildNode));
        return this;
    }

    public Node<D> addChild(@Nonnull Node<?> childNode) {
        return this.addChildren(Collections.singletonList(childNode));
    }

    public Node<D> addChild(@Nonnull Function<? super Node<D>, ? extends Node<?>> buildChildNode) {
        return this.addChildren((d) -> Collections.singletonList(this), (cd, n) -> buildChildNode.apply(n));
    }

    public <C> Node<D> addChild(
        @Nonnull Function<? super D, ? extends C> getChildData,
        @Nonnull BiFunction<C, Node<D>, Node<?>> buildChildNode) {
        this.childrenBuilders.add(new ChildrenBuilder<>(d -> Collections.singletonList(getChildData.apply(d)), buildChildNode));
        return this;
    }

    public Node<D> withMoreChildren(@Nonnull Predicate<? super D> hasMoreChildren, Consumer<? super D> moreChildrenLoader) {
        this.hasMoreChildren = hasMoreChildren;
        this.moreChildrenLoader = moreChildrenLoader;
        return this;
    }

    public List<Node<?>> getChildren() {
        try {
            return this.childrenBuilders.stream().flatMap((builder) -> builder.build(this)).collect(Collectors.toList());
        } catch (final Exception e) {
            AzureMessager.getMessager().error(e);
            return Collections.emptyList();
        }
    }

    public View getView() {
        return this.buildView();
    }

    public View buildView() {
        final AzureIcon icon = this.buildIcon();
        final String label = this.buildLabel();
        final String desc = this.buildDescription();
        final String tips = this.buildTips();
        final boolean enabled = this.checkEnabled();
        final boolean visible = this.checkVisible();
        return new View(icon, label, desc, tips, enabled, visible);
    }

    public AzureIcon buildIcon() {
        return Optional.ofNullable(this.iconProvider).map(p -> p.apply(this.data)).orElse(null);
    }

    @Nonnull
    public String buildLabel() {
        return Optional.ofNullable(this.labelProvider).map(p -> p.apply(this.data)).orElse(this.data.toString());
    }

    public String buildDescription() {
        return Optional.ofNullable(this.descProvider).map(p -> p.apply(this.data)).orElse(null);
    }

    public String buildTips() {
        return Optional.ofNullable(this.tipsProvider).map(p -> p.apply(this.data)).orElse(null);
    }

    public boolean checkEnabled() {
        return this.enableWhen.test(this.data);
    }

    public boolean checkVisible() {
        return this.visibleWhen.test(this.data);
    }

    protected void onViewChanged() {
        Optional.ofNullable(this.viewChangedListener).ifPresent(ViewChangedListener::onViewChanged);
    }

    protected void onChildrenChanged(boolean... incremental) {
        Optional.ofNullable(this.childrenChangedListener).ifPresent(r -> r.onChildrenChanged(incremental));
    }

    public boolean hasChildren() {
        return !this.childrenBuilders.isEmpty();
    }

    public boolean hasMoreChildren() {
        return this.hasMoreChildren.test(this.getData());
    }

    public void loadMoreChildren() {
        this.moreChildrenLoader.accept(this.getData());
    }

    public void triggerInlineAction(final Object event, int index) {
        final List<Action<? super D>> enabledActions = this.inlineActions.stream()
            .filter(action -> action.getView(this.data).isEnabled()).toList();
        if (index >= 0 && index < enabledActions.size()) {
            Optional.ofNullable(enabledActions.get(index)).ifPresent(a -> a.handle(this.data, event));
        }
    }

    public Node<D> addInlineAction(Action.Id<? super D> actionId) {
        this.inlineActions.add(AzureActionManager.getInstance().getAction(actionId));
        return this;
    }

    public Node<D> newItemsOrder(@Nonnull final Order order) {
        this.newItemOrder = order;
        return this;
    }

    public void dispose() {
        this.setChildrenChangedListener(null);
        this.setViewChangedListener(null);
    }

    @RequiredArgsConstructor
    private static class ChildrenBuilder<D, C> {
        private final Function<? super D, ? extends List<C>> getChildrenData;
        private final BiFunction<C, Node<D>, Node<?>> buildChildNode;

        private Stream<Node<?>> build(Node<D> n) {
            final List<C> childrenData = this.getChildrenData.apply(n.data);
            return childrenData.stream().filter(Objects::nonNull).map(d -> buildChildNode.apply(d, n));
        }
    }

    public enum Order {
        LIST_ORDER,
        INSERT_ORDER
    }

    @Getter
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class View implements IView.Label {
        private final AzureIcon icon;
        @Nonnull
        private final String label;
        private String description;
        @Getter(AccessLevel.NONE)
        private String tips;
        private boolean enabled = true;
        private boolean visible = true;

        @Nullable
        public String getIconPath() {
            return Objects.isNull(this.icon) ? null : this.icon.getIconPath();
        }

        public String getTips() {
            return Optional.ofNullable(this.tips).filter(StringUtils::isNotBlank).or(() -> Optional.ofNullable(this.getDescription())).filter(StringUtils::isNotBlank).orElseGet(this::getLabel);
        }

        @Override
        public void dispose() {
        }
    }

    @FunctionalInterface
    public static interface ViewChangedListener {
        void onViewChanged();
    }

    @FunctionalInterface
    public static interface ChildrenChangedListener {
        void onChildrenChanged(boolean... incremental);
    }

    public static enum Type {
        NORMAL, ACTION
    }
}
