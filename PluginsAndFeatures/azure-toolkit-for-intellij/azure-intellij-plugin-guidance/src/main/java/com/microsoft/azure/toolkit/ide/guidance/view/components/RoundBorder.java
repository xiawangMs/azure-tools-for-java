/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.guidance.view.components;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.border.Border;
import java.awt.*;

public class RoundBorder implements Border {
    private final Color color;
    private final int radius;

    public RoundBorder() {
        this(JBColor.BLACK);
    }

    public RoundBorder(Color color) {
        this(color, 5);
    }

    public RoundBorder(Color color, int radius) {
        this.color = color;
        this.radius = radius;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.setColor(this.color);
        g.drawRoundRect(0, 0, width, height, height/2, height/2);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return JBUI.insets(0, radius/2, 0, radius/2);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

}
