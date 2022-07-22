/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.ui;

import com.azure.core.management.AzureEnvironment;
import com.microsoft.azure.toolkit.eclipse.common.component.AzureComboBox;
import com.microsoft.azure.toolkit.ide.common.store.AzureConfigInitializer;
import com.microsoft.azure.toolkit.ide.common.util.ParserXMLUtility;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.AzureConfiguration;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.auth.AzureCloud;
import com.microsoft.azure.toolkit.lib.auth.AzureEnvironmentUtils;
import com.microsoft.azure.toolkit.lib.common.utils.InstallationIdUtils;
import com.microsoft.azuretools.authmanage.CommonSettings;
import com.microsoft.azuretools.authmanage.IdeAzureAccount;
import com.microsoft.azuretools.azurecommons.xmlhandling.DataOperations;
import com.microsoft.azuretools.core.Activator;
import com.microsoft.azuretools.core.utils.FileUtil;
import com.microsoft.azuretools.core.utils.Messages;
import com.microsoft.azuretools.core.utils.PluginUtil;
import com.microsoft.azuretools.telemetry.AppInsightsClient;
import com.microsoft.azuretools.telemetry.AppInsightsConstants;
import com.microsoft.azuretools.telemetrywrapper.EventType;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URL;
import java.util.Objects;

import static com.microsoft.azuretools.telemetry.TelemetryConstants.*;

/**
 * Class creates azure preference page.
 */
public class WindowsAzurePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private AzureComboBox<AzureEnvironment> cbAzureEnv;

    public WindowsAzurePreferencePage() {
    }
    Button btnPreference;
    String pluginInstLoc = String.format("%s%s%s", PluginUtil.pluginFolder, File.separator, Messages.commonPluginID);
    String dataFile = String.format("%s%s%s", pluginInstLoc, File.separator, Messages.dataFileName);

    @Override
    public String getTitle() {
        String prefState = Activator.getPrefState();
        if (prefState.isEmpty()) {
            if (new File(pluginInstLoc).exists() && new File(dataFile).exists()) {
                String prefValue = DataOperations.getProperty(dataFile, Messages.prefVal);
                if (prefValue != null && !prefValue.isEmpty()) {
                    if (prefValue.equals("true")) {
                        btnPreference.setSelection(true);
                    }
                }
            }
        } else {
            // if changes are not saved yet (i.e. just navigated to other preference pages)
            // then populate temporary value
            if (prefState.equalsIgnoreCase("true")) {
                btnPreference.setSelection(true);
            } else {
                btnPreference.setSelection(false);
            }
        }
        return super.getTitle();
    }

    @Override
    public void init(IWorkbench arg0) {
    }

    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        container.setLayout(gridLayout);
        container.setLayoutData(gridData);

        Composite composite = new Composite(container, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        composite.setLayout(new GridLayout(2, false));

        Label lblNewLabel = new Label(composite, SWT.NONE);
        lblNewLabel.setText(Messages.WindowsAzurePreferencePageAzureCloudLabel);

        cbAzureEnv = new AzureComboBox<AzureEnvironment>(composite) {
            protected String getItemText(final Object item) {
                if (Objects.isNull(item)) {
                    return EMPTY_ITEM;
                }
                return azureEnvironmentDisplayString((AzureEnvironment) item);
            }
        };
        cbAzureEnv.setItemsLoader(() -> Azure.az(AzureCloud.class).list());
        final AzureConfiguration config = Azure.az().config();
        cbAzureEnv.setValue(ObjectUtils.firstNonNull(AzureEnvironmentUtils.stringToAzureEnvironment(config.getCloud()),
                AzureEnvironment.AZURE
        ));

        cbAzureEnv.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        btnPreference = new Button(container, SWT.CHECK);
        btnPreference.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnPreference.setText(Messages.preferenceMsg);
        Link urlLink = new Link(container, SWT.LEFT);
        urlLink.setText(Messages.preferenceLinkMsg);
        urlLink.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(event.text));
                } catch (Exception ex) {
                    Activator.getDefault().log(Messages.lnkOpenErrMsg, ex);
                }
            }
        });
        return container;
    }

    private static String azureEnvironmentDisplayString(@Nonnull AzureEnvironment env) {
        return String.format("%s - %s", azureEnvironmentToString(env), env.getActiveDirectoryEndpoint());
    }

    private static String azureEnvironmentToString(@Nonnull AzureEnvironment env) {
        final String name = AzureEnvironmentUtils.getCloudName(env);
        return org.apache.commons.lang3.StringUtils.removeEnd(name, "Cloud");
    }

    @Override
    public boolean okToLeave() {
        if (btnPreference != null) {
            Activator.setPrefState(String.valueOf(btnPreference.getSelection()));
        }
        return super.okToLeave();
    }

    @Override
    public boolean performCancel() {
        Activator.setPrefState("");
        return super.performCancel();
    }

    @Override
    public boolean performOk() {
        boolean isSet = true;
        try {
            if (new File(pluginInstLoc).exists()) {
                if (new File(dataFile).exists()) {
                    Document doc = ParserXMLUtility.parseXMLFile(dataFile);
                    String oldPrefVal = DataOperations.getProperty(dataFile, Messages.prefVal);
                    DataOperations.updatePropertyValue(doc, Messages.prefVal,
                            String.valueOf(btnPreference.getSelection()));

                    final String version = DataOperations.getProperty(dataFile, Messages.version);
                    final String newVersion = Activator.getDefault().getBundle().getVersion().toString();
                    if (version == null || version.isEmpty()) {
                        DataOperations.updatePropertyValue(doc, Messages.version, newVersion);
                    } else if (!newVersion.equalsIgnoreCase(version)) {
                        DataOperations.updatePropertyValue(doc, Messages.version, newVersion);
                        AppInsightsClient.createByType(AppInsightsClient.EventType.Plugin, "", AppInsightsConstants.Upgrade, null, true);
                        EventUtil.logEvent(EventType.info, SYSTEM, PLUGIN_UPGRADE, null, null);
                    }

                    String instID = DataOperations.getProperty(dataFile, Messages.instID);
                    if (instID == null || instID.isEmpty() || !InstallationIdUtils.isValidHashMac(instID)) {
                        DataOperations.updatePropertyValue(doc, Messages.instID, InstallationIdUtils.getHashMac());
                        AppInsightsClient.createByType(AppInsightsClient.EventType.Plugin, "", AppInsightsConstants.Install, null, true);
                        EventUtil.logEvent(EventType.info, SYSTEM, PLUGIN_INSTALL, null, null);
                    }
                    ParserXMLUtility.saveXMLFile(dataFile, doc);
                    // Its necessary to call application insights custom create
                    // event after saving data.xml
                    final boolean acceptTelemetry = btnPreference.getSelection();
                    if (StringUtils.isEmpty(oldPrefVal) || Boolean.valueOf(oldPrefVal) != acceptTelemetry) {
                        // Boolean.valueOf(oldPrefVal) != acceptTelemetry means
                        // user changes his mind.
                        // Either from Agree to Deny, or from Deny to Agree.
                        final String action = acceptTelemetry ? AppInsightsConstants.Allow : AppInsightsConstants.Deny;
                        AppInsightsClient.createByType(AppInsightsClient.EventType.Telemetry, "", action, null, true);
                        EventUtil.logEvent(EventType.info, SYSTEM, acceptTelemetry ? TELEMETRY_ALLOW : TELEMETRY_DENY,
                                null, null);
                    }
                } else {
                    FileUtil.copyResourceFile(Messages.dataFileEntry, dataFile);
                    setValues(dataFile);
                }
            } else {
                new File(pluginInstLoc).mkdir();
                FileUtil.copyResourceFile(Messages.dataFileEntry, dataFile);
                setValues(dataFile);
            }
        } catch (SAXParseException ex2) {
            isSet = false;
            FileUtils.deleteQuietly(new File(this.dataFile));
            Activator.getDefault().log(ex2.getMessage(), ex2);
        } catch (Exception ex) {
            isSet = false;
            Activator.getDefault().log(ex.getMessage(), ex);
        }

        // new logic of persist settings
        final AzureConfiguration config = Azure.az().config();
        config.setCloud(AzureEnvironmentUtils.azureEnvironmentToString(cbAzureEnv.getValue()));
        config.setTelemetryEnabled(btnPreference.getSelection());
        String userAgent = String.format(Activator.USER_AGENT, config.getVersion(),
                Azure.az().config().getMachineId());
        CommonSettings.setUserAgent(userAgent);

        // we need to get rid of AuthMethodManager, using az.azure_account
        if (IdeAzureAccount.getInstance().isLoggedIn()) {
            final AzureEnvironment currentEnv = Azure.az(AzureAccount.class).account().getEnvironment();
            if (!Objects.equals(currentEnv, cbAzureEnv.getValue())) {
                EventUtil.executeWithLog(ACCOUNT, SIGNOUT, (operation) -> {
                    Azure.az(AzureAccount.class).logout();
                });
            }
        }

        AzureConfigInitializer.saveAzConfig();
        if (isSet) {
            // forget temporary values once OK button has been pressed.
            Activator.setPrefState("");
            return super.performOk();
        } else {
            PluginUtil.displayErrorDialog(getShell(), Messages.err, Messages.prefSaveErMsg);
            return false;
        }
    }

    /**
     * Method updates or creates property elements in data.xml
     *
     * @param dataFile
     * @throws Exception
     */
    private void setValues(String dataFile) throws Exception {
        Document doc = ParserXMLUtility.parseXMLFile(dataFile);
        DataOperations.updatePropertyValue(doc, Messages.version,
                Activator.getDefault().getBundle().getVersion().toString());
        DataOperations.updatePropertyValue(doc, Messages.instID, InstallationIdUtils.getHashMac());
        DataOperations.updatePropertyValue(doc, Messages.prefVal, String.valueOf(btnPreference.getSelection()));
        ParserXMLUtility.saveXMLFile(dataFile, doc);
    }
}
