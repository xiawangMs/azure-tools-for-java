/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */


package com.microsoft.azuretools.azureexplorer.forms;

import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.resource.AzureGroup;
import com.microsoft.azure.toolkit.lib.storage.model.AccessTier;
import com.microsoft.azure.toolkit.lib.storage.model.Kind;
import com.microsoft.azure.toolkit.lib.storage.model.Performance;
import com.microsoft.azure.toolkit.lib.storage.model.Redundancy;
import com.microsoft.azure.toolkit.lib.storage.model.StorageAccountConfig;
import com.microsoft.azure.toolkit.lib.storage.service.AzureStorageAccount;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.azureexplorer.Activator;
import com.microsoft.azuretools.azureexplorer.forms.common.DraftResourceGroup;
import com.microsoft.azuretools.core.components.AzureTitleAreaDialogWrapper;
import com.microsoft.azuretools.core.utils.Messages;
import com.microsoft.azuretools.core.utils.PluginUtil;
import com.microsoft.azuretools.sdkmanage.AzureManager;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.Vector;
import java.util.stream.Collectors;

public class CreateArmStorageAccountForm extends AzureTitleAreaDialogWrapper {
    private static final String PRICING_LINK = "<a href=\"http://go.microsoft.com/fwlink/?LinkID=400838\">Read more about replication services and pricing details</a>";
    private static Map<String, Kind> ACCOUNT_KIND = new TreeMap<>();

    static {
        ACCOUNT_KIND.put("General purpose v1", Kind.STORAGE);
        ACCOUNT_KIND.put("General purpose v2", Kind.STORAGE_V2);
        ACCOUNT_KIND.put("Blob storage", Kind.BLOB_STORAGE);
    }

    private Button buttonOK;
    private Button buttonCancel;

    private Label subscriptionLabel;
    private Combo subscriptionComboBox;
    private Label nameLabel;
    private Text nameTextField;
    private Label resourceGroupLabel;
    private Button createNewRadioButton;
    private Button useExistingRadioButton;
    private Text resourceGrpField;
    private Combo resourceGrpCombo;
    private Label regionLabel;
    private Combo regionComboBox;
    private Label kindLabel;
    private Combo kindCombo;
    private Label performanceLabel;
    private Combo performanceCombo;
    private Label replicationLabel;
    private Combo replicationComboBox;
    private Label accessTierLabel;
    private Combo accessTierComboBox;
    private Link pricingLabel;

    private ComboViewer resourceGroupViewer;

    private Runnable onCreate;
    private Subscription subscription;
    private Region region;
    private StorageAccountConfig newStorageAccount;

    public CreateArmStorageAccountForm(Shell parentShell, Subscription subscription, Region region) {
        super(parentShell);
        this.subscription = subscription;
        this.region = region;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Create Storage Account");
        Image image = PluginUtil.getImage(Messages.strAccDlgImg);
        if (image != null) {
            setTitleImage(image);
        }
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Control ctrl = super.createButtonBar(parent);
        buttonOK = getButton(IDialogConstants.OK_ID);
        buttonOK.setEnabled(false);
        buttonOK.setText("Create");
        buttonCancel = getButton(IDialogConstants.CANCEL_ID);
        buttonCancel.setText("Close");
        return ctrl;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        setTitle("Create New Storage Account");
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.microsoft.azuretools.azureexplorer.storage_account_dialog");

        Composite container = new Composite(parent, SWT.FILL);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginBottom = 10;
        container.setLayout(gridLayout);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
//        gridData.widthHint = 250;
        container.setLayoutData(gridData);

        nameLabel = new Label(container, SWT.LEFT);
        nameLabel.setText("Name:");
        nameTextField = new Text(container, SWT.LEFT | SWT.BORDER);
//        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        nameTextField.setLayoutData(gridDataForText(180));
        subscriptionLabel = new Label(container, SWT.LEFT);
        subscriptionLabel.setText("Subscription:");
        subscriptionComboBox = new Combo(container, SWT.READ_ONLY);
        subscriptionComboBox.setLayoutData(gridDataForText(180));

        resourceGroupLabel = new Label(container, SWT.LEFT);
        resourceGroupLabel.setText("Resource group:");
        gridData = new GridData();
        gridData.verticalAlignment = SWT.TOP;
        resourceGroupLabel.setLayoutData(gridData);

        final Composite composite = new Composite(container, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = GridData.BEGINNING;
        gridData.grabExcessHorizontalSpace = true;
//        gridData.widthHint = 250;
        composite.setLayout(gridLayout);
        composite.setLayoutData(gridData);

        createNewRadioButton = new Button(composite, SWT.RADIO);
        createNewRadioButton.setText("Create new");
        createNewRadioButton.setSelection(true);
        resourceGrpField = new Text(composite, SWT.LEFT | SWT.BORDER);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        resourceGrpField.setLayoutData(gridData);

        useExistingRadioButton = new Button(composite, SWT.RADIO);
        useExistingRadioButton.setText("Use existing");
        resourceGrpCombo = new Combo(composite, SWT.READ_ONLY);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        resourceGrpCombo.setLayoutData(gridData);
        resourceGroupViewer = new ComboViewer(resourceGrpCombo);
        resourceGroupViewer.setContentProvider(ArrayContentProvider.getInstance());

        SelectionListener updateListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                updateResourceGroup();
            }
        };
        createNewRadioButton.addSelectionListener(updateListener);
        useExistingRadioButton.addSelectionListener(updateListener);

        updateResourceGroup();

        regionLabel = new Label(container, SWT.LEFT);
        regionLabel.setText("Region:");
        regionComboBox = new Combo(container, SWT.READ_ONLY);
        regionComboBox.setLayoutData(gridDataForText(180));

        kindLabel = new Label(container, SWT.LEFT);
        kindLabel.setText("Account kind:");
        kindCombo = new Combo(container, SWT.READ_ONLY);
        kindCombo.setLayoutData(gridDataForText(180));

        performanceLabel = new Label(container, SWT.LEFT);
        performanceLabel.setText("Performance:");
        performanceCombo = new Combo(container, SWT.READ_ONLY);
        performanceCombo.setLayoutData(gridDataForText(180));

        replicationLabel = new Label(container, SWT.LEFT);
        replicationLabel.setText("Replication:");
        replicationComboBox = new Combo(container, SWT.READ_ONLY);
        replicationComboBox.setLayoutData(gridDataForText(180));

        if (subscription == null) { // not showing access tier with general purpose storage account which is used when creating vm
            accessTierLabel = new Label(container, SWT.LEFT);
            accessTierLabel.setText("Access Tier:");
            accessTierComboBox = new Combo(container, SWT.READ_ONLY);
            accessTierComboBox.setLayoutData(gridDataForText(180));
            for (AccessTier type : AccessTier.values()) {
                accessTierComboBox.add(type.toString());
                accessTierComboBox.setData(type.toString(), type);
            }
            accessTierComboBox.select(0);
        }

        pricingLabel = new Link(container, SWT.LEFT);
        pricingLabel.setText(PRICING_LINK);
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        pricingLabel.setLayoutData(gridData);
        pricingLabel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(event.text));
                } catch (Exception ex) {
                    /*
                     * only logging the error in log file
                     * not showing anything to end user
                     */
                    Activator.getDefault().log("Error occurred while opening link in default browser.", ex);
                }
            }
        });

        nameTextField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent modifyEvent) {
                validateEmptyFields();
            }
        });

        regionComboBox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                validateEmptyFields();
            }
        });

        resourceGrpField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent modifyEvent) {
                validateEmptyFields();
            }
        });

        resourceGrpCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                validateEmptyFields();
            }
        });

        fillFields();

        return super.createDialogArea(parent);
    }

    private GridData gridDataForText(int width) {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.widthHint = width;
        gridData.verticalIndent = 10;
        gridData.grabExcessHorizontalSpace = true;
        return gridData;
    }

    private void updateResourceGroup() {
        final boolean isNewGroup = createNewRadioButton.getSelection();
        resourceGrpField.setEnabled(isNewGroup);
        resourceGrpCombo.setEnabled(!isNewGroup);
    }

    private void validateEmptyFields() {
        boolean allFieldsCompleted = !(nameTextField.getText().isEmpty() || regionComboBox.getText().isEmpty() ||
            (createNewRadioButton.getSelection() && resourceGrpField.getText().trim().isEmpty()) ||
            (useExistingRadioButton.getSelection() && resourceGrpCombo.getText().isEmpty()));

        buttonOK.setEnabled(allFieldsCompleted);
    }

    @Override
    protected void okPressed() {
        if (nameTextField.getText().length() < 3 || nameTextField.getText().length() > 24 ||
            !nameTextField.getText().matches("[a-z0-9]+")) {
            DefaultLoader.getUIHelper()
                .showError("Invalid storage account name. The name should be between 3 and 24 characters long and "
                    + "can contain only lowercase letters and numbers.", "Azure Explorer");
            return;
        }
        if (subscription == null) {
            subscription = (Subscription) subscriptionComboBox.
                getData(subscriptionComboBox.getText());
        }
        final boolean isNewResourceGroup = createNewRadioButton.getSelection();
        final ResourceGroup resourceGroup = isNewResourceGroup ? new DraftResourceGroup(subscription, resourceGrpField.getText()) : Azure.az(AzureGroup.class)
            .subscription(subscription.getId())
            .getByName(resourceGrpCombo.getText());
        Region region = ((Region) regionComboBox.getData(regionComboBox.getText()));
        Kind kind = (Kind) kindCombo.getData(kindCombo.getText());
        String name = nameTextField.getText();

        newStorageAccount = StorageAccountConfig.builder()
            .name(name)
            .region(region)
            .kind(kind)
            .resourceGroup(resourceGroup)
            .performance((Performance) performanceCombo.getData(performanceCombo.getText()))
            .redundancy((Redundancy) replicationComboBox.getData(replicationComboBox.getText()))
            .subscription(subscription)
            .accessTier(Optional.ofNullable(accessTierComboBox).map(t -> (AccessTier) accessTierComboBox.getData(accessTierComboBox.getText())).orElse(null))
            .build();
        this.onCreate.run();
        super.okPressed();
    }

    public void fillFields() {
        if (subscription == null) {
            try {
                subscriptionComboBox.setEnabled(true);
                AzureManager azureManager = AuthMethodManager.getInstance().getAzureManager();
                // not signed in
                if (azureManager == null) {
                    return;
                }
                List<Subscription> subscriptions = azureManager.getSelectedSubscriptions();
                for (Subscription sub : subscriptions) {
                    if (sub.isSelected()) {
                        subscriptionComboBox.add(sub.getName());
                        subscriptionComboBox.setData(sub.getName(), sub);
                    }
                }
                subscriptionComboBox.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        loadRegionsAndGroups();
                    }
                });

                if (subscriptions.size() > 0) {
                    subscriptionComboBox.select(0);
                    loadRegionsAndGroups();
                }
            } catch (Exception e) {
                PluginUtil.displayErrorDialogWithAzureMsg(PluginUtil.getParentShell(), Messages.err,
                    "An error occurred while loading subscriptions.", e);
            }
            for (Map.Entry<String, Kind> entry : ACCOUNT_KIND.entrySet()) {
                kindCombo.add(entry.getKey());
                kindCombo.setData(entry.getKey(), entry.getValue());
            }
            kindCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    fillPerformanceComboBox();
                    fillReplicationTypes();

                    showAccessTier();
                }
            });
            kindCombo.select(1);
            showAccessTier();
        } else { // create form create VM form
            subscriptionComboBox.setEnabled(false);
            subscriptionComboBox.add(subscription.getName());
            subscriptionComboBox.setData(subscription.getName(), subscription);
            subscriptionComboBox.select(0);
            kindCombo.add("General purpose v1"); // only General purpose accounts supported for VMs
            kindCombo.setData("General purpose v1", Kind.STORAGE);
            kindCombo.setEnabled(false);
            kindCombo.select(0);

            regionComboBox.add(region.getLabel());
            regionComboBox.setData(region.getLabel(), region);
            regionComboBox.setEnabled(false);
            regionComboBox.select(0);
            loadGroups();
            //loadRegions();
        }
        fillPerformanceComboBox();
        //performanceCombo.select(0);
        performanceCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                fillReplicationTypes();
            }
        });
        fillReplicationTypes();
    }

    private void fillPerformanceComboBox() {
        performanceCombo.removeAll();
        if (kindCombo.getData(kindCombo.getText()) == Kind.BLOB_STORAGE) {
            performanceCombo.add(Performance.STANDARD.getName());
            performanceCombo.setData(Performance.STANDARD.getName(), Performance.STANDARD);
        } else {
            for (Performance skuTier : Performance.values()) {
                performanceCombo.setData(skuTier.getName(), skuTier);
                performanceCombo.add(skuTier.getName());
            }
        }
        performanceCombo.select(0);
    }

    private void fillReplicationTypes() {
        replicationComboBox.removeAll();
        Kind kind = (Kind) kindCombo.getData(kindCombo.getText());
        Performance performance = (Performance) performanceCombo.getData(performanceCombo.getText());
        List<Redundancy> redundancies = Objects.isNull(performance) ? Collections.emptyList() :
            Azure.az(AzureStorageAccount.class).listSupportedRedundancies(performance, kind);
        for (Redundancy redundancy : redundancies) {
            replicationComboBox.add(redundancy.getLabel());
            replicationComboBox.setData(redundancy.getLabel(), redundancy);
        }

        if (redundancies.contains(Redundancy.STANDARD_RAGRS)) {
            replicationComboBox.select(redundancies.indexOf(Redundancy.STANDARD_RAGRS));
        } else {
            replicationComboBox.select(0);
        }
    }

    public void setOnCreate(Runnable onCreate) {
        this.onCreate = onCreate;
    }

    public StorageAccountConfig getStorageAccount() {
        return newStorageAccount;
    }

    public void loadRegionsAndGroups() {
        subscriptionComboBox.getData(subscriptionComboBox.getText()).toString();
        Subscription subs = (Subscription) subscriptionComboBox.
            getData(subscriptionComboBox.getText());
        if (subs != null) {
            AzureTaskManager.getInstance().runInBackground("Loading Available Locations...", () -> {
                try {
                    // warm cache
                    Azure.az(AzureAccount.class).listRegions(subs.getId());
                    AzureTaskManager.getInstance().runLater(new Runnable() {
                        @Override
                        public void run() {
                            fillRegions();
                            fillGroups();
                        }
                    });
                } catch (Exception ex) {
                    PluginUtil.displayErrorDialogWithAzureMsg(PluginUtil.getParentShell(), Messages.err, "Error loading locations", ex);
                }
            });
        } else {
            fillRegions();
            fillGroups();
        }
    }

    private void fillRegions() {
        Subscription subs = (Subscription) subscriptionComboBox.
            getData(subscriptionComboBox.getText());
        final List<Region> locations = Azure.az(AzureAccount.class).listRegions(subs.getId());
        AzureTaskManager.getInstance().runLater(() -> {
            for (Region location : locations) {
                regionComboBox.add(location.getLabel());
                regionComboBox.setData(location.getLabel(), location);
            }
            if (locations.size() > 0) {
                regionComboBox.select(0);
            }
        });
    }

    public void loadGroups() {
        resourceGrpCombo.add("<Loading...>");
        Subscription subs = (Subscription) subscriptionComboBox.
            getData(subscriptionComboBox.getText());
        if (subs == null) {
            AzureTaskManager.getInstance().runInBackground("Loading Resource Groups", new Runnable() {
                @Override
                public void run() {
                    try {
                        AzureTaskManager.getInstance().runLater(new Runnable() {
                            @Override
                            public void run() {
                                fillGroups();
                            }
                        });
                    } catch (Exception ex) {
                        PluginUtil.displayErrorDialogWithAzureMsg(PluginUtil.getParentShell(), Messages.err, "Error loading resource groups", ex);
                    }
                }
            });
        } else {
            fillGroups();
        }
    }

    public void fillGroups() {
        final Subscription subs = (Subscription) subscriptionComboBox.getData(subscriptionComboBox.getText());
        List<ResourceGroup> resourceGroups = Azure.az(AzureGroup.class).list(subs.getId(), true);
        List<String> sortedGroups = resourceGroups.stream().map(ResourceGroup::getName).sorted().collect(Collectors.toList());
        AzureTaskManager.getInstance().runLater(new Runnable() {
            @Override
            public void run() {
                final Vector<Object> vector = new Vector<Object>();
                vector.addAll(sortedGroups);
                resourceGroupViewer.setInput(vector);
                if (sortedGroups.size() > 0) {
                    resourceGrpCombo.select(0);
                }
            }
        });
    }

    private void showAccessTier() {
        boolean isBlobKind = (Kind) kindCombo.getData(kindCombo.getText()) == Kind.BLOB_STORAGE;
        accessTierComboBox.setVisible(isBlobKind);
        accessTierLabel.setVisible(isBlobKind);
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    public Map<String, String> toProperties() {
        final Map<String, String> properties = new HashMap<>();

        if (this.getSubscription() != null) {
            if (this.getSubscription().getName() != null) {
                properties.put("SubscriptionName", this.getSubscription().getName());
            }
            if (this.getSubscription().getId() != null) {
                properties.put("SubscriptionId", this.getSubscription().getId());
            }
        }

        return properties;
    }
}
