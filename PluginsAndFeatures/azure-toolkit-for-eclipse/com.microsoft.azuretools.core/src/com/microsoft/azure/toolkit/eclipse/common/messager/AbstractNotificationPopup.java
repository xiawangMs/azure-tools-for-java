/*
 * Copyright (c) 2004, 2015 Tasktop Technologies and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copied from org.eclipse.mylyn.commons.ui.dialogs.AbstractNotificationPopup
 *
 *     Benjamin Pasero - initial API and implementation
 *     Tasktop Technologies - initial API and implementation
 */

package com.microsoft.azure.toolkit.eclipse.common.messager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.commons.ui.GradientColors;
import org.eclipse.mylyn.commons.ui.compatibility.CommonFonts;
import org.eclipse.mylyn.internal.commons.ui.AnimationUtil;
import org.eclipse.mylyn.internal.commons.ui.AnimationUtil.FadeJob;
import org.eclipse.mylyn.internal.commons.ui.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractNotificationPopup extends Window {
    private static final String LABEL_NOTIFICATION;
    private static final String LABEL_JOB_CLOSE;
    private long delayClose;
    protected LocalResourceManager resources;
    private GradientColors color;
    private final Display display;
    private Shell shell;
    private final Job closeJob;
    private FadeJob fadeJob;
    private boolean fadingEnabled;

    static {
        LABEL_NOTIFICATION = Messages.AbstractNotificationPopup_Notification;
        LABEL_JOB_CLOSE = Messages.AbstractNotificationPopup_Close_Notification_Job;
    }

    public AbstractNotificationPopup(Display display) {
        this(display, 540684);
    }

    public AbstractNotificationPopup(Display display, int style) {
        super(new Shell(display));
        this.delayClose = 15000L;
        this.closeJob = new Job(LABEL_JOB_CLOSE) {
            protected IStatus run(IProgressMonitor monitor) {
                if (!AbstractNotificationPopup.this.display.isDisposed()) {
                    AbstractNotificationPopup.this.display.asyncExec(() -> {
                        Shell shell = AbstractNotificationPopup.this.getShell();
                        if (shell != null && !shell.isDisposed()) {
                            if (AbstractNotificationPopup.this.isMouseOver(shell)) {
                                AbstractNotificationPopup.this.scheduleAutoClose();
                            } else {
                                AbstractNotificationPopup.this.closeFade();
                            }
                        }
                    });
                }

                return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
            }
        };
        this.setShellStyle(style);
        this.display = display;
        this.resources = new LocalResourceManager(JFaceResources.getResources());
        this.initResources();
        this.closeJob.setSystem(true);
    }

    public boolean isFadingEnabled() {
        return this.fadingEnabled;
    }

    public void setFadingEnabled(boolean fadingEnabled) {
        this.fadingEnabled = fadingEnabled;
    }

    protected String getPopupShellTitle() {
        String productName = CommonUiUtil.getProductName();
        return productName != null ? productName + " " + LABEL_NOTIFICATION : LABEL_NOTIFICATION;
    }

    protected Image getPopupShellImage() {
        return null;
    }

    protected void createContentArea(Composite parent) {
    }

    protected void createTitleArea(Composite parent) {
        ((GridData) parent.getLayoutData()).heightHint = 24;
        Label titleImageLabel = new Label(parent, 0);
        titleImageLabel.setImage(this.getPopupShellImage());
        Label titleTextLabel = new Label(parent, 0);
        titleTextLabel.setText(this.getPopupShellTitle());
        titleTextLabel.setFont(CommonFonts.BOLD);
        titleTextLabel.setForeground(this.getTitleForeground());
        titleTextLabel.setLayoutData(new GridData(SWT.FILL, 16777216, true, true));
        titleTextLabel.setCursor(parent.getDisplay().getSystemCursor(21));
        final Label button = new Label(parent, 0);
        button.setImage(CommonImages.getImage(CommonImages.NOTIFICATION_CLOSE));
        button.addMouseTrackListener(new MouseTrackAdapter() {
            public void mouseEnter(MouseEvent e) {
                button.setImage(CommonImages.getImage(CommonImages.NOTIFICATION_CLOSE_HOVER));
            }

            public void mouseExit(MouseEvent e) {
                button.setImage(CommonImages.getImage(CommonImages.NOTIFICATION_CLOSE));
            }
        });
        button.addMouseListener(new MouseAdapter() {
            public void mouseUp(MouseEvent e) {
                AbstractNotificationPopup.this.close();
                AbstractNotificationPopup.this.setReturnCode(1);
            }
        });
    }

    protected Color getTitleForeground() {
        return this.color.getTitleText();
    }

    private void initResources() {
        this.color = new GradientColors(this.display, this.resources);
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        this.shell = newShell;
        newShell.setBackground(this.color.getBorder());
    }

    public void create() {
        super.create();
    }

    private boolean isMouseOver(Shell shell) {
        return !this.display.isDisposed() && shell.getBounds().contains(this.display.getCursorLocation());
    }

    public int open() {
        if (this.shell == null || this.shell.isDisposed()) {
            this.shell = null;
            this.create();
        }

        this.constrainShellSize();
        this.shell.setLocation(this.fixupDisplayBounds(this.shell.getSize(), this.shell.getLocation()));
        if (this.isFadingEnabled()) {
            this.shell.setAlpha(0);
        }

        this.shell.setVisible(true);
        this.fadeJob = AnimationUtil.fadeIn(this.shell, (shell, alpha) -> {
            if (!shell.isDisposed()) {
                if (alpha == 255) {
                    AbstractNotificationPopup.this.scheduleAutoClose();
                }
            }
        });
        return 0;
    }

    protected void scheduleAutoClose() {
        if (this.delayClose > 0L) {
            this.closeJob.schedule(this.delayClose);
        }
    }

    protected Control createContents(Composite parent) {
        final Color color = this.shell.getDisplay().getSystemColor(1);

        ((GridLayout) parent.getLayout()).marginWidth = 1;
        ((GridLayout) parent.getLayout()).marginHeight = 1;

        final Composite containerPanel = new Composite(parent, SWT.NO_FOCUS);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        containerPanel.setLayout(layout);
        containerPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        containerPanel.setBackgroundMode(SWT.INHERIT_FORCE);

        Composite titlePanel = new Composite(containerPanel, SWT.NO_FOCUS);
        layout = new GridLayout(4, false);
        layout.marginWidth = 3;
        layout.marginHeight = 0;
        layout.verticalSpacing = 3;
        layout.horizontalSpacing = 3;
        titlePanel.setLayout(layout);
        titlePanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        titlePanel.setBackgroundMode(SWT.INHERIT_FORCE);
        titlePanel.setBackground(color);
        this.createTitleArea(titlePanel);

        Composite contentPanel = new Composite(containerPanel, SWT.NONE);
        layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginLeft = 5;
        layout.marginRight = 5;
        contentPanel.setLayout(layout);
        contentPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        contentPanel.setBackgroundMode(SWT.INHERIT_FORCE);
        contentPanel.setBackground(color);
        this.createContentArea(contentPanel);
        return containerPanel;
    }

    protected void initializeBounds() {
        Rectangle clArea = this.getPrimaryClientArea();
        Point initialSize = this.shell.computeSize(-1, -1);
        int height = Math.max(initialSize.y, 100);
        int width = Math.min(initialSize.x, 400);
        Point size = new Point(width, height);
        this.shell.setLocation(clArea.width + clArea.x - size.x - 5, clArea.height + clArea.y - size.y - 5);
        this.shell.setSize(size);
    }

    private Rectangle getPrimaryClientArea() {
        Monitor primaryMonitor = this.shell.getDisplay().getPrimaryMonitor();
        return primaryMonitor != null ? primaryMonitor.getClientArea() : this.shell.getDisplay().getClientArea();
    }

    public void closeFade() {
        if (this.fadeJob != null) {
            this.fadeJob.cancelAndWait(false);
        }

        this.fadeJob = AnimationUtil.fadeOut(this.getShell(), (shell, alpha) -> {
            if (!shell.isDisposed()) {
                if (alpha == 0) {
                    shell.close();
                } else if (AbstractNotificationPopup.this.isMouseOver(shell)) {
                    if (AbstractNotificationPopup.this.fadeJob != null) {
                        AbstractNotificationPopup.this.fadeJob.cancelAndWait(false);
                    }

                    AbstractNotificationPopup.this.fadeJob = AnimationUtil.fastFadeIn(shell, (shell1, alpha1) -> {
                        if (!shell1.isDisposed()) {
                            if (alpha1 == 255) {
                                AbstractNotificationPopup.this.scheduleAutoClose();
                            }
                        }
                    });
                }
            }
        });
    }

    public boolean close() {
        this.resources.dispose();
        return super.close();
    }

    public long getDelayClose() {
        return this.delayClose;
    }

    public void setDelayClose(long delayClose) {
        this.delayClose = delayClose;
    }

    private Point fixupDisplayBounds(Point tipSize, Point location) {
        Point rightBounds = new Point(tipSize.x + location.x, tipSize.y + location.y);
        Rectangle bounds = this.shell.getDisplay().getPrimaryMonitor().getBounds();
        if (!bounds.contains(location) || !bounds.contains(rightBounds)) {
            if (rightBounds.x > bounds.x + bounds.width) {
                location.x -= rightBounds.x - (bounds.x + bounds.width);
            }

            if (rightBounds.y > bounds.y + bounds.height) {
                location.y -= rightBounds.y - (bounds.y + bounds.height);
            }

            if (location.x < bounds.x) {
                location.x = bounds.x;
            }

            if (location.y < bounds.y) {
                location.y = bounds.y;
            }
        }

        return location;
    }
}
