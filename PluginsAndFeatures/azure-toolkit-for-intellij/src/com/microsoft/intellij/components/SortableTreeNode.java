/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.components;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.util.Comparator;

public class SortableTreeNode extends DefaultMutableTreeNode {

    public SortableTreeNode() {
        super();
    }

    public SortableTreeNode(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void add(MutableTreeNode newChild) {
        super.add(newChild);
        this.children.sort(nodeComparator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void insert(MutableTreeNode newChild, int childIndex) {
        super.insert(newChild, childIndex);
        this.children.sort(nodeComparator);
    }

    private static final Comparator nodeComparator =
            (Comparator<SortableTreeNode>) (node1, node2) -> node1.toString().compareToIgnoreCase(node2.toString());
}
