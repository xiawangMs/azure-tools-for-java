/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common.component;

import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.EmptyAction;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.tree.TreeModelAdapter;
import com.intellij.util.ui.tree.TreeUtil;
import com.microsoft.azure.toolkit.ide.common.component.NodeView;
import com.microsoft.azure.toolkit.intellij.common.IntelliJAzureIcons;
import com.microsoft.azure.toolkit.intellij.common.action.IntellijAzureActionManager;
import com.microsoft.azure.toolkit.lib.AzService;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.IActionGroup;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResourceModule;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzServiceSubscription;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.common.view.IView;
import com.microsoft.azure.toolkit.lib.resource.AzureResources;
import com.microsoft.azure.toolkit.lib.resource.ResourcesServiceSubscription;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class TreeUtils {
    public static final Key<Pair<Object, Long>> HIGHLIGHTED_RESOURCE_KEY = Key.create("TreeHighlightedResource");
    public static final Key<List<AbstractAzResource<?, ?, ?>>> RESOURCES_TO_SHOW_KEY = Key.create("ResourcesToShow");
    public static final int INLINE_ACTION_ICON_OFFSET = 28;
    public static final int INLINE_ACTION_ICON_WIDTH = 16;
    public static final int INLINE_ACTION_ICON_MARGIN = 8;

    public static void installSelectionListener(@Nonnull JTree tree) {
        tree.addTreeSelectionListener(e -> {
            final Object n = tree.getLastSelectedPathComponent();
            Disposable selectionDisposable = (Disposable) tree.getClientProperty("SELECTION_DISPOSABLE");
            if (selectionDisposable != null) {
                Disposer.dispose(selectionDisposable);
            }
            if (n instanceof Tree.TreeNode) {
                final Tree.TreeNode<?> node = (Tree.TreeNode<?>) n;
                final String place = "azure.explorer." + (TreeUtils.isInAppCentricView(node) ? "app" : "type");
                final IActionGroup actions = node.inner.actions();
                if (Objects.nonNull(actions)) {
                    final ActionManager am = ActionManager.getInstance();
                    selectionDisposable = Disposer.newDisposable();
                    tree.putClientProperty("SELECTION_DISPOSABLE", selectionDisposable);
                    final IntellijAzureActionManager.ActionGroupWrapper group = toIntellijActionGroup(actions);
                    group.registerCustomShortcutSetForActions(tree, selectionDisposable);
                }
            }
        });
    }

    public static void installExpandListener(@Nonnull JTree tree) {
        final TreeWillExpandListener listener = new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) {
                final Object component = event.getPath().getLastPathComponent();
                if (component instanceof Tree.TreeNode) {
                    final Tree.TreeNode<?> treeNode = (Tree.TreeNode<?>) component;
                    if (treeNode.getAllowsChildren()) {
                        treeNode.loadChildren();
                    }
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) {

            }
        };
        tree.addTreeWillExpandListener(listener);
    }

    public static void installMouseListener(@Nonnull JTree tree) {
        tree.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                final Tree.TreeNode<?> node = getTreeNodeAtMouse(tree, e);
                final boolean isMouseAtActionIcon = isMouseAtInlineActionIcon(tree, e, Optional.ofNullable(node)
                        .map(Tree.TreeNode::getInlineActionViews).map(List::size).orElse(0));
                final Cursor cursor = isMouseAtActionIcon ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor();
                tree.setCursor(cursor);
            }
        });
        final MouseAdapter popupHandler = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final Object n = tree.getLastSelectedPathComponent();
                if (n instanceof Tree.TreeNode) {
                    final Tree.TreeNode<?> node = (Tree.TreeNode<?>) n;
                    final String place = "azure.explorer." + (TreeUtils.isInAppCentricView(node) ? "app" : "type");
                    if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
                        final IActionGroup actions = node.inner.actions();
                        if (Objects.nonNull(actions)) {
                            final ActionManager am = ActionManager.getInstance();
                            final IntellijAzureActionManager.ActionGroupWrapper group = toIntellijActionGroup(actions);
                            final ActionPopupMenu menu = am.createActionPopupMenu(place, group);
                            menu.setTargetComponent(tree);
                            final JPopupMenu popupMenu = menu.getComponent();
                            popupMenu.show(tree, e.getX(), e.getY());
                        }
                    } else if (node.inner.hasClickAction()) {
                        final DataContext context = DataManager.getInstance().getDataContext(tree);
                        final AnActionEvent event = AnActionEvent.createFromAnAction(new EmptyAction(), e, place, context);
                        node.inner.triggerClickAction(event);
                    } else if (e.getClickCount() == 2) {
                        final DataContext context = DataManager.getInstance().getDataContext(tree);
                        final AnActionEvent event = AnActionEvent.createFromAnAction(new EmptyAction(), e, place, context);
                        node.inner.triggerDoubleClickAction(event);
                    }
                } else if (n instanceof Tree.LoadMoreNode && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    ((Tree.LoadMoreNode) n).load();
                }
                super.mouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                final Tree.TreeNode<?> node = getTreeNodeAtMouse(tree, e);
                final int inlineActionIndex = getHoverInlineActionIndex(tree, e);
                final List<IView.Label> inlineActionViews = Optional.ofNullable(node)
                        .map(Tree.TreeNode::getInlineActionViews).orElse(new ArrayList<>());
                final boolean isMouseAtInlineActionIcon = isMouseAtInlineActionIcon(tree, e, inlineActionViews.size());
                if (Objects.nonNull(node) && e.getClickCount() == 1 && isMouseAtInlineActionIcon) {
                    final String place = "azure.explorer." + (TreeUtils.isInAppCentricView(node) ? "app" : "type");
                    final DataContext context = DataManager.getInstance().getDataContext(tree);
                    final AnActionEvent event = AnActionEvent.createFromAnAction(new EmptyAction(), e, place, context);
                    node.inner.triggerInlineAction(event, inlineActionIndex);
                }
            }
        };
        tree.addMouseListener(popupHandler);
    }

    @Nullable
    public static Tree.TreeNode<?> getTreeNodeAtMouse(@Nonnull JTree tree, MouseEvent e) {
        final TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return null;
        }
        final Object node = path.getLastPathComponent();
        if (node instanceof Tree.TreeNode) {
            return (Tree.TreeNode<?>) node;
        }
        return null;
    }

    private static boolean isMouseAtInlineActionIcon(@Nonnull JTree tree, MouseEvent e, int maxIndex) {
        final int hoverIndex = getHoverInlineActionIndex(tree, e);
        final int offset = tree.getWidth() - e.getX() - INLINE_ACTION_ICON_OFFSET + INLINE_ACTION_ICON_WIDTH - hoverIndex * (INLINE_ACTION_ICON_WIDTH + INLINE_ACTION_ICON_MARGIN);
        final boolean isInActionIcon = offset > 0 && offset < INLINE_ACTION_ICON_WIDTH;
        return hoverIndex >= 0 && hoverIndex <= maxIndex && isInActionIcon;
    }

    private static int getHoverInlineActionIndex(@Nonnull JTree tree, MouseEvent e) {
        final int width = tree.getWidth();
        final int x = e.getX() - INLINE_ACTION_ICON_WIDTH;
        return (width - x - INLINE_ACTION_ICON_OFFSET) / (INLINE_ACTION_ICON_WIDTH + INLINE_ACTION_ICON_MARGIN);
    }

    private static IntellijAzureActionManager.ActionGroupWrapper toIntellijActionGroup(IActionGroup actions) {
        final ActionManager am = ActionManager.getInstance();
        if (actions instanceof IntellijAzureActionManager.ActionGroupWrapper) {
            return (IntellijAzureActionManager.ActionGroupWrapper) actions;
        }
        return new IntellijAzureActionManager.ActionGroupWrapper((ActionGroup) actions);
    }

    public static void renderLoadModeNode(JTree tree, @Nonnull Tree.LoadMoreNode node, boolean selected, @Nonnull SimpleColoredComponent renderer) {
        final SimpleTextAttributes attributes = SimpleTextAttributes.GRAY_ATTRIBUTES;
        renderer.append("more...", attributes);
        renderer.setToolTipText("double click to load more.");
    }

    public static void renderMyTreeNode(JTree tree, @Nonnull Tree.TreeNode<?> node, boolean selected, @Nonnull SimpleColoredComponent renderer) {
        final NodeView view = node.inner.view();
        if (BooleanUtils.isFalse(node.loaded)) {
            renderer.setIcon(AnimatedIcon.Default.INSTANCE);
        } else {
            renderer.setIcon(Optional.ofNullable(view.getIcon()).map(IntelliJAzureIcons::getIcon).orElse(null));
        }
        final Object highlighted = tree.getClientProperty(HIGHLIGHTED_RESOURCE_KEY);
        final boolean toHighlightThisNode = Optional.ofNullable(highlighted).map(h -> ((Pair<Object, Long>) h))
            .filter(h -> Objects.equals(node.getUserObject(), h.getLeft())).isPresent();
        SimpleTextAttributes attributes = view.isEnabled() ? SimpleTextAttributes.REGULAR_ATTRIBUTES : SimpleTextAttributes.GRAY_ATTRIBUTES;
        if (selected && toHighlightThisNode) {
            attributes = attributes.derive(SimpleTextAttributes.STYLE_SEARCH_MATCH, JBColor.RED, JBColor.YELLOW, null);
        } else if (selected) {
            tree.putClientProperty(HIGHLIGHTED_RESOURCE_KEY, null);
        }
        renderer.append(view.getLabel(), attributes);
        renderer.append(Optional.ofNullable(view.getDescription()).filter(StringUtils::isNotBlank).map(d -> " " + d).orElse(""), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES, true);
        renderer.setToolTipText(Optional.ofNullable(view.getTips()).filter(StringUtils::isNotBlank).orElse(view.getLabel()));
    }

    public static boolean isInAppCentricView(@Nonnull DefaultMutableTreeNode node) {
        return isInAppCentricView(new TreePath(node.getPath()));
    }

    public static boolean isInAppCentricView(@Nonnull TreePath path) {
        if (path.getPathCount() < 2) {
            return false;
        }
        final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getPathComponent(1);
        return treeNode.getUserObject() instanceof AzureResources;
    }

    public static void highlightResource(@Nonnull JTree tree, @Nonnull Object resource) {
        Condition<DefaultMutableTreeNode> condition = n -> isInAppCentricView(n) && Objects.equals(n.getUserObject(), resource);
        if (resource instanceof AzService) {
            condition = n -> Objects.equals(n.getUserObject(), resource);
        }
        final DefaultMutableTreeNode node = TreeUtil.findNode((DefaultMutableTreeNode) tree.getModel().getRoot(), condition);
        AzureTaskManager.getInstance().runLater(() -> {
            tree.putClientProperty(HIGHLIGHTED_RESOURCE_KEY, Pair.of(resource, System.currentTimeMillis()));
            Optional.ofNullable(node).ifPresent(n -> TreeUtil.selectPath(tree, new TreePath(node.getPath()), false));
        }, AzureTask.Modality.ANY);
    }

    @Nullable
    public static DefaultMutableTreeNode getExistingResourceParentNode(@Nonnull JTree tree, @Nonnull final AbstractAzResource<?, ?, ?> resource) {
        AbstractAzResource<?, ?, ?> nodeResource = resource;
        DefaultMutableTreeNode node = findResourceTreeNode(tree, nodeResource);
        while (Objects.isNull(node) && Objects.nonNull(nodeResource)) {
            if (nodeResource.getParent() instanceof ResourcesServiceSubscription) {
                return findResourceTreeNode(tree, Azure.az(AzureResources.class));
            }
            nodeResource = nodeResource.getParent() instanceof AbstractAzServiceSubscription ?
                    nodeResource.getResourceGroup() : (AbstractAzResource<?, ?, ?>) nodeResource.getParent();
            node = Objects.isNull(nodeResource) ? null : findResourceTreeNode(tree, nodeResource);
        }
        return node;
    }

    public static void expandResource(@Nonnull final JTree tree, @Nonnull final AbstractAzResource<?, ?, ?> resource) {
        final List<AbstractAzResource<?, ?, ?>> resourcesToShow = getResourcesToShow(tree);
        final DefaultMutableTreeNode node = findResourceTreeNode(tree, resource);
        if (Objects.isNull(node)) {
            tree.getModel().removeTreeModelListener(TreeShowResourceListener.INSTANCE);
            tree.getModel().addTreeModelListener(TreeShowResourceListener.INSTANCE);
            final DefaultMutableTreeNode parentNode = getExistingResourceParentNode(tree, resource);
            Optional.ofNullable(parentNode).ifPresent(n -> {
                resourcesToShow.add(resource);
                expandTreeNode(tree, n);
            });
        } else {
            expandTreeNode(tree, node);
        }
    }

    private static List<AbstractAzResource<?, ?, ?>> getResourcesToShow(@Nonnull final JTree tree) {
        final Object clientProperty = tree.getClientProperty(RESOURCES_TO_SHOW_KEY);
        if (clientProperty instanceof List) {
            return (List<AbstractAzResource<?, ?, ?>>) clientProperty;
        } else {
            final List<AbstractAzResource<?, ?, ?>> result = new ArrayList<>();
            tree.putClientProperty(RESOURCES_TO_SHOW_KEY, result);
            return result;
        }
    }

    public static boolean isParentResource(@Nonnull final Object parent, @Nonnull final AbstractAzResource<?, ?, ?> resource) {
        if (parent instanceof AzureResources) {
            return true;
        }
        if (Objects.equals(parent, resource.getResourceGroup())) {
            return true;
        }
        return (parent instanceof AbstractAzResource<?, ?, ?> && StringUtils.containsIgnoreCase(resource.getId(), ((AbstractAzResource<?, ?, ?>) parent).getId())) ||
                (parent instanceof AbstractAzResourceModule<?, ?, ?> && StringUtils.containsIgnoreCase(resource.getId(),
                        ((AbstractAzResourceModule<?, ?, ?>) parent).toResourceId(resource.getResourceGroupName(), resource.getName())));
    }

    public static void expandTreeNode(@Nonnull JTree tree, @Nonnull DefaultMutableTreeNode node) {
        AzureTaskManager.getInstance().runLater(() -> tree.expandPath(new TreePath(node.getPath())), AzureTask.Modality.ANY);
    }

    @Nullable
    public static DefaultMutableTreeNode findResourceTreeNode(@Nonnull JTree tree, @Nonnull Object resource) {
        final Condition<DefaultMutableTreeNode> condition = n -> (resource instanceof AzService || isInAppCentricView(n)) &&
                Objects.equals(n.getUserObject(), resource);
        return TreeUtil.findNode((DefaultMutableTreeNode) tree.getModel().getRoot(), condition);
    }

    static class TreeShowResourceListener extends TreeModelAdapter {
        static final TreeShowResourceListener INSTANCE = new TreeShowResourceListener();

        @Override
        protected void process(@NotNull TreeModelEvent event, @NotNull EventType type) {
            final Object[] path = event.getPath();
            final Object sourceNode = ArrayUtils.isEmpty(path) ? null : path[path.length - 1];
            if (type == EventType.StructureChanged && sourceNode instanceof Tree.TreeNode<?> && isInAppCentricView((DefaultMutableTreeNode) sourceNode)) {
                final Tree.TreeNode<?> source = (Tree.TreeNode<?>) sourceNode;
                final List<AbstractAzResource<?, ?, ?>> resourcesToShow = getResourcesToShow(source.tree);
                final AbstractAzResource<?, ?, ?> targetResource = resourcesToShow.stream()
                        .filter(resource -> isParentResource(source.getData(), resource)).findFirst().orElse(null);
                if (Objects.isNull(targetResource)) {
                    return;
                }
                final Tree.TreeNode<?> treeNode = Objects.equals(source.getData(), targetResource) ? source :
                        StreamSupport.stream(Spliterators.spliteratorUnknownSize(source.children().asIterator(), Spliterator.ORDERED), false)
                                .filter(node -> node instanceof Tree.TreeNode<?>)
                                .map(node -> (Tree.TreeNode<?>) node)
                                .filter(node -> ((Tree.TreeNode<?>) node).getData() != null && isParentResource(((Tree.TreeNode<?>) node).getData(), targetResource))
                                .findFirst().orElse(null);
                if (Objects.equals(treeNode.getData(), targetResource)) {
                    // remove resource from list if it was founded or its parent was not found
                    resourcesToShow.remove(targetResource);
                    expandTreeNode(source.tree, treeNode);
                } else if (Objects.isNull(treeNode)) {
                    resourcesToShow.remove(targetResource);
                } else {
                    expandTreeNode(source.tree, treeNode);
                }
            }
        }
    }
}
