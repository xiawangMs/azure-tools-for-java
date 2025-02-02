/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common.action;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.EmptyAction;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ShortcutSet;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.DumbAware;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.intellij.common.IntelliJAzureIcons;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.action.IActionGroup;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.model.Emulatable;
import com.microsoft.azure.toolkit.lib.common.view.IView;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.microsoft.azure.toolkit.lib.common.action.Action.EMPTY_PLACE;
import static com.microsoft.azure.toolkit.lib.common.action.Action.PLACE;

public class IntellijAzureActionManager extends AzureActionManager {
    private static final ExtensionPointName<IActionsContributor> actionsExtensionPoint =
        ExtensionPointName.create("com.microsoft.tooling.msservices.intellij.azure.actions");

    /**
     * register {@code ACTION_SOURCE} as data key, so that PreCachedDataContext can pre cache it.
     */
    private static final DataKey<Object> ACTION_SOURCE = DataKey.create("ACTION_SOURCE");

    private IntellijAzureActionManager() {
        super();
    }

    public static void register() {
        final IntellijAzureActionManager am = new IntellijAzureActionManager();
        register(am);
        final List<IActionsContributor> contributors = actionsExtensionPoint.getExtensionList();
        contributors.stream().sorted(Comparator.comparing(IActionsContributor::getOrder)).forEach((e) -> e.registerActions(am));
        contributors.stream().sorted(Comparator.comparing(IActionsContributor::getOrder)).forEach((e) -> e.registerHandlers(am));
        contributors.stream().sorted(Comparator.comparing(IActionsContributor::getOrder)).forEach((e) -> e.registerGroups(am));
    }

    public <D> void registerAction(Action<D> action) {
        final ActionManager manager = ActionManager.getInstance();
        if (Objects.isNull(manager.getAction(action.getId()))) {
            final AnActionWrapper<D> wrapper = new AnActionWrapper<>(action);
            manager.registerAction(action.getId(), wrapper);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D> Action<D> getAction(Action.Id<D> id) {
        final AnAction origin = ActionManager.getInstance().getAction(id.getId());
        if (origin instanceof AnActionWrapper) {
            return (Action<D>) ((AnActionWrapper<?>) origin).getAction();
        } else {
            return new Action<>(id)
                .withLabel(Objects.requireNonNull(origin.getTemplateText()))
                .withHandler((D d, AnActionEvent e) -> origin.actionPerformed(e))
                .withAuthRequired(false);
        }
    }

    @Override
    public void registerGroup(String id, ActionGroup group) {
        final ActionGroupWrapper nativeGroup = new ActionGroupWrapper(group);
        group.setOrigin(nativeGroup);
        final ActionManager manager = ActionManager.getInstance();
        if (Objects.isNull(manager.getAction(id))) {
            manager.registerAction(id, nativeGroup);
        }
    }

    @Override
    public IActionGroup getGroup(String id) {
        return (ActionGroupWrapper) ActionManager.getInstance().getAction(id);
    }

    public static String convertToAzureActionPlace(@Nonnull final String place) {
        return StringUtils.equalsAnyIgnoreCase(place, ActionPlaces.PROJECT_VIEW_POPUP, ActionPlaces.PROJECT_VIEW_TOOLBAR)
            ? ResourceCommonActionsContributor.PROJECT_VIEW : place;
    }

    @Getter
    public static class AnActionWrapper<T> extends AnAction implements DumbAware {
        @Nonnull
        private final Action<T> action;

        public AnActionWrapper(@Nonnull Action<T> action) {
            super();
            this.action = action;
        }

        @Override
        public @Nonnull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.BGT;
        }

        @Nullable
        public ShortcutSet getShortcuts() {
            final Object shortcuts = action.getShortcut();
            if (shortcuts instanceof Action.Id) {
                return ActionManager.getInstance().getAction(((Action.Id<?>) shortcuts).getId()).getShortcutSet();
            } else if (shortcuts instanceof String) {
                return CustomShortcutSet.fromString((String) shortcuts);
            } else if (shortcuts instanceof String[]) {
                return CustomShortcutSet.fromString((String[]) shortcuts);
            } else if (shortcuts instanceof ShortcutSet) {
                return (ShortcutSet) shortcuts;
            } else {
                return null;
            }
        }

        @Nullable
        @SuppressWarnings("unchecked")
        private T getSource(@Nonnull AnActionEvent e) {
            return Optional.ofNullable((T) e.getDataContext().getData(Action.SOURCE))
                .or(() -> Optional.ofNullable(e.getData(CommonDataKeys.NAVIGATABLE_ARRAY))
                    .filter(d -> d.length == 1 && d[0] instanceof DataProvider)
                    .map(d -> (DataProvider) d[0])
                    .map(d -> (T) d.getData(Action.SOURCE)))
                .orElse(null);
        }

        @Override
        public void actionPerformed(@Nonnull AnActionEvent e) {
            final T source = getSource(e);
            this.action.getContext().setTelemetryProperty(PLACE, StringUtils.firstNonBlank(e.getPlace(), EMPTY_PLACE));
            this.action.handle(source, e);
        }

        @Override
        public void update(@Nonnull AnActionEvent e) {
            final T source = getSource(e);
            final String place = convertToAzureActionPlace(e.getPlace());
            final Presentation presentation = e.getPresentation();
            final IView.Label view = this.action.getView(source, place);
            final boolean visible;
            final boolean isAbstractAzResource = source instanceof AbstractAzResource;

            if (source instanceof Emulatable && ((Emulatable) source).isEmulatorResource()) {
                visible = view.isVisible() && Objects.nonNull(action.getHandler(source, e));
            } else if (isAbstractAzResource && "[LinkedCluster]".equals(((AbstractAzResource<?, ?, ?>) source).getSubscription().getId())) {
                visible = true;
            } else {
                final boolean isResourceInOtherSubs = isAbstractAzResource && !((AbstractAzResource<?, ?, ?>) source).getSubscription().isSelected();
                visible = !isResourceInOtherSubs && view.isVisible() && Objects.nonNull(action.getHandler(source, e));
            }

            presentation.setVisible(visible);
            if (visible) {
                final boolean enabled = view.isEnabled() && Objects.nonNull(action.getHandler(source, e));
                presentation.setEnabled(enabled);
                presentation.setIcon(Optional.ofNullable(view.getIconPath()).map(IntelliJAzureIcons::getIcon).orElse(null));
                presentation.setText(view.getLabel());
                presentation.setDescription(view.getDescription());
            }
        }
    }

    @Getter
    public static class ActionGroupWrapper extends DefaultActionGroup implements IActionGroup, DumbAware {

        private final ActionGroup group;

        public ActionGroupWrapper(@Nonnull ActionGroup group) {
            super();
            this.group = group;
            this.setPopup(true);
            this.addActions(group.getActions());
        }

        @Override
        public void update(@Nonnull AnActionEvent e) {
            super.update(e);
            final IView.Label view = this.group.getView();
            final Presentation presentation = e.getPresentation();
            Optional.ofNullable(view).ifPresent(v -> {
                presentation.setText(v.getLabel());
                Optional.ofNullable(v.getIconPath()).filter(StringUtils::isNotBlank).ifPresent(IntelliJAzureIcons::getIcon);
            });
        }

        @Override
        public @Nonnull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.BGT;
        }

        private void addActions(List<Object> actions) {
            for (final Object raw : actions) {
                doAddAction(raw);
            }
        }

        @Override
        public IView.Label getView() {
            return group.getView();
        }

        @Override
        public List<Object> getActions() {
            return group.getActions();
        }

        @Override
        public void addAction(Object raw) {
            this.group.addAction(raw);
            this.doAddAction(raw);
        }

        public void doAddAction(Object raw) {
            if (raw instanceof Action.Id) {
                raw = ((Action.Id<?>) raw).getId();
            }
            if (raw instanceof String) {
                final String actionId = (String) raw;
                if (actionId.startsWith("-")) {
                    final String title = actionId.replaceAll("-", "").trim();
                    if (StringUtils.isBlank(title)) {
                        this.addSeparator();
                    } else {
                        this.addSeparator(title);
                    }
                } else if (StringUtils.isNotBlank(actionId)) {
                    final ActionManager am = ActionManager.getInstance();
                    final AnAction action = am.getAction(actionId);
                    if (action instanceof com.intellij.openapi.actionSystem.ActionGroup) {
                        this.add(action);
                    } else if (Objects.nonNull(action)) {
                        this.add(EmptyAction.wrap(action));
                    }
                }
            } else if (raw instanceof Action<?>) {
                this.add(new AnActionWrapper<>((Action<?>) raw));
            } else if (raw instanceof ActionGroup) {
                this.add(new ActionGroupWrapper((ActionGroup) raw));
            }
        }

        public void registerCustomShortcutSetForActions(JComponent component, @Nullable Disposable disposable) {
            for (final AnAction origin : this.getChildActionsOrStubs()) {
                final AnAction real = origin instanceof com.intellij.openapi.actionSystem.AnActionWrapper ?
                    ((com.intellij.openapi.actionSystem.AnActionWrapper) origin).getDelegate() : origin;
                if (real instanceof AnActionWrapper) {
                    final ShortcutSet shortcuts = ((AnActionWrapper<?>) real).getShortcuts();
                    if (Objects.nonNull(shortcuts)) {
                        origin.registerCustomShortcutSet(shortcuts, component, disposable);
                    }
                }
            }
        }
    }

    @Override
    public Shortcuts getIDEDefaultShortcuts() {
        return new Shortcuts() {
            @Override
            public Object add() {
                return CommonShortcuts.getNew();
            }

            @Override
            public Object delete() {
                return CommonShortcuts.getDelete();
            }

            @Override
            public Object view() {
                return CommonShortcuts.getViewSource();
            }

            @Override
            public Object edit() {
                return CommonShortcuts.getEditSource();
            }

            @Override
            public Object refresh() {
                return Action.Id.of(IdeActions.ACTION_REFRESH);
            }

            @Override
            public Object start() {
                return "ctrl F1";
            }

            @Override
            public Object restart() {
                return "ctrl alt F5";
            }

            @Override
            public Object stop() {
                return Action.Id.of(IdeActions.ACTION_STOP_PROGRAM);
            }

            @Override
            public Object deploy() {
                return Action.Id.of(IdeActions.ACTION_DEFAULT_RUNNER);
            }

            @Override
            public Object copy() {
                return CommonShortcuts.getCopy();
            }

            @Override
            public Object paste() {
                return CommonShortcuts.getPaste();
            }
        };
    }
}
