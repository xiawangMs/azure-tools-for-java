/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.common.action;

import com.microsoft.azure.toolkit.ide.common.component.IView;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Accessors(chain = true, fluent = true)
public class Action<D> {
    @SuppressWarnings("rawtypes")
    public static final Consumer EMPTY_HANDLER = (s) -> {
        throw new AzureToolkitRuntimeException("handler not registered");
    };
    public static final String SOURCE = "ACTION_SOURCE";
    @Nonnull
    private List<AbstractMap.SimpleEntry<Object, Object>> handlers = new ArrayList<>();
    @Nullable
    @Getter
    private ActionView.Builder<D> view;

    public Action(@Nonnull Consumer<D> handler) {
        if (handler != EMPTY_HANDLER) {
            this.registerHandler((d) -> true, handler);
        }
    }

    public <E> Action(@Nonnull BiConsumer<D, E> handler) {
        if (handler != EMPTY_HANDLER) {
            this.registerHandler((d, e) -> true, handler);
        }
    }

    public Action(@Nonnull Consumer<D> handler, @Nullable ActionView.Builder<D> view) {
        this.view = view;
        if (handler != EMPTY_HANDLER) {
            this.registerHandler((d) -> true, handler);
        }
    }

    public <E> Action(@Nonnull BiConsumer<D, E> handler, @Nullable ActionView.Builder<D> view) {
        this.view = view;
        if (handler != EMPTY_HANDLER) {
            this.registerHandler((d, e) -> true, handler);
        }
    }

    private Action(@Nonnull List<AbstractMap.SimpleEntry<Object, Object>> handlers, @Nullable ActionView.Builder<D> view) {
        this.view = view;
        this.handlers = handlers;
    }

    @Nullable
    public IView.Label view(D source) {
        return Objects.nonNull(this.view) ? this.view.toActionView(source) : null;
    }

    @SuppressWarnings("unchecked")
    public void handle(D source, Object e) {
        for (int i = this.handlers.size() - 1; i >= 0; i--) {
            final AbstractMap.SimpleEntry<Object, Object> p = this.handlers.get(i);
            final Object condition = p.getKey();
            if (condition instanceof BiPredicate && ((BiPredicate<D, Object>) condition).test(source, e)) {
                ((BiConsumer<D, Object>) p.getValue()).accept(source, e);
                return;
            } else if (condition instanceof Predicate && ((Predicate<D>) condition).test(source)) {
                ((Consumer<D>) p.getValue()).accept(source);
                return;
            }
        }
    }

    public void handle(D source) {
        this.handle(source, null);
    }

    public void registerHandler(@Nonnull Predicate<D> condition, @Nonnull Consumer<D> handler) {
        this.handlers.add(new AbstractMap.SimpleEntry<>(condition, handler));
    }

    public <E> void registerHandler(@Nonnull BiPredicate<D, E> condition, @Nonnull BiConsumer<D, E> handler) {
        this.handlers.add(new AbstractMap.SimpleEntry<>(condition, handler));
    }

    public static <T> Consumer<T> emptyHandler() {
        //noinspection unchecked
        return (Consumer<T>) EMPTY_HANDLER;
    }

    @Getter
    @Accessors(chain = true, fluent = true)
    public static class Proxy<D> extends Action<D> {
        @Nonnull
        private final String id;
        @Nonnull
        private final Action<D> action;

        public Proxy(@Nonnull Action<D> action, @Nonnull String id) {
            super(action.handlers, action.view);
            this.id = id;
            this.action = action;
        }
    }
}

