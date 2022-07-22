/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.ui;

import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azuretools.core.Activator;
import com.microsoft.azuretools.core.components.AzureTitleAreaDialogWrapper;
import com.microsoft.azuretools.core.utils.ProgressDialog;
import com.microsoft.azuretools.telemetrywrapper.EventType;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.microsoft.azuretools.telemetry.TelemetryConstants.ACCOUNT;
import static com.microsoft.azuretools.telemetry.TelemetryConstants.SELECT_SUBSCRIPTIONS;

public class SubscriptionsDialog extends AzureTitleAreaDialogWrapper {
    private static final String SUBSCRIPTION_MESSAGE = "Select subscription(s) you want to use.";
    private static final String SUBSCRIPTION_TITLE = "Your Subscriptions";
    private static final String DIALOG_TITLE = "Select Subscriptions";
    private static ILog LOG = Activator.getDefault().getLog();

    private Table table;

    private List<Subscription> sdl;

    /**
     * Create the dialog.
     * @param parentShell
     */
    private SubscriptionsDialog(Shell parentShell) {
        super(parentShell);
        setHelpAvailable(false);
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    public static SubscriptionsDialog go(Shell parentShell) {
        SubscriptionsDialog d = new SubscriptionsDialog(parentShell);
        if (d.open() == Window.OK) {
            return d;
        }
        return null;
    }

    /**
     * Create contents of the dialog.
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        setMessage(SUBSCRIPTION_MESSAGE);
        setTitle(SUBSCRIPTION_TITLE);
        getShell().setText(DIALOG_TITLE);
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        table = new Table(container, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
        GridData gdTable = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gdTable.heightHint = 300;
        table.setLayoutData(gdTable);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
        tblclmnNewColumn.setWidth(300);
        tblclmnNewColumn.setText("Subscription Name");

        TableColumn tblclmnNewColumn1 = new TableColumn(table, SWT.NONE);
        tblclmnNewColumn1.setWidth(270);
        tblclmnNewColumn1.setText("Subscription ID");

        Composite composite = new Composite(container, SWT.NONE);
        composite.setLayout(new RowLayout(SWT.HORIZONTAL));

        Button btnRefresh = new Button(composite, SWT.NONE);
        btnRefresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshSubscriptions();
            }
        });
        btnRefresh.setText("Refresh");

        Button btnSelectAll = new Button(composite, SWT.NONE);
        btnSelectAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Arrays.stream(table.getItems()).forEach(d -> {
                    d.setChecked(true);
                });
            }
        });
        btnSelectAll.setText("Select All");

        Button btnDeselectAll = new Button(composite, SWT.NONE);
        btnDeselectAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Arrays.stream(table.getItems()).forEach(d -> {
                    d.setChecked(false);
                });
            }
        });
        btnDeselectAll.setText("Deselect All");

        return area;
    }

    @Override
    public void create() {
        super.create();
        Display.getDefault().asyncExec(this::setSubscriptions);
    }

    public void refreshSubscriptionsAsync() {
        try {
            ProgressDialog.get(getShell(), "Update Azure Local Cache Progress").run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask("Reading subscriptions...", IProgressMonitor.UNKNOWN);
                    Azure.az(AzureAccount.class).account().reloadSubscriptions();
                    monitor.done();
                }
            });
        } catch (InvocationTargetException | InterruptedException ex) {
            ex.printStackTrace();
            //LOGGER.log(LogService.LOG_ERROR, "run@refreshSubscriptionsAsync@SubscriptionDialog", e);
            LOG.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "run@refreshSubscriptionsAsync@SubscriptionDialog", ex));
        }
    }

    private void setSubscriptions() {
        sdl = Azure.az(AzureAccount.class).account().getSubscriptions();
        for (Subscription sd : sdl) {
            TableItem item = new TableItem(table, SWT.NULL);
            item.setText(new String[] {sd.getName(), sd.getId()});
            item.setChecked(sd.isSelected());
        }
    }

    private void refreshSubscriptions() {
        System.out.println("refreshSubscriptions");
        table.removeAll();
        refreshSubscriptionsAsync();
        setSubscriptions();
    }

    /**
     * Create contents of the button bar.
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        Button okButton = getButton(IDialogConstants.OK_ID);
        okButton.setText("Select");
    }

    @Override
    public void okPressed() {
        EventUtil.logEvent(EventType.info, ACCOUNT, SELECT_SUBSCRIPTIONS, null);
        TableItem[] tia = table.getItems();
        int rc = tia.length;
        int chekedCount = 0;
        for (TableItem ti : tia) {
            if (ti.getChecked()) {
                chekedCount++;
            }
        }

        if (chekedCount == 0) {
            this.setErrorMessage("Select at least one subscription");
            return;
        }

        for (int i = 0; i < tia.length; ++i) {
            this.sdl.get(i).setSelected(tia[i].getChecked());
        }

        List<String> selectedIds = this.sdl.stream().filter(Subscription::isSelected)
                .map(Subscription::getId).collect(Collectors.toList());
        Azure.az(AzureAccount.class).account().setSelectedSubscriptions(selectedIds);
        
        final Map<String, String> properties = new HashMap<>();
        properties.put("subsCount", String.valueOf(rc));
        properties.put("selectedSubsCount", String.valueOf(chekedCount));
        EventUtil.logEvent(EventType.info, ACCOUNT, SELECT_SUBSCRIPTIONS, null);

        super.okPressed();
    }
}
