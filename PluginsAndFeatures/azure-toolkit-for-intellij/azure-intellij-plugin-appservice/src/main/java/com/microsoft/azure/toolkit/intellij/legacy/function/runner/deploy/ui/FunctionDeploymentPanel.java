/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.function.runner.deploy.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.ListCellRendererWrapper;
import com.microsoft.azure.toolkit.ide.appservice.function.FunctionAppConfig;
import com.microsoft.azure.toolkit.intellij.legacy.common.AzureSettingPanel;
import com.microsoft.azure.toolkit.intellij.legacy.function.FunctionAppComboBox;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.component.table.FunctionAppSettingsTable;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.component.table.FunctionAppSettingsTableUtils;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.core.FunctionUtils;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.deploy.FunctionDeployConfiguration;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.function.AzureFunctions;
import com.microsoft.azure.toolkit.lib.appservice.function.FunctionApp;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.microsoft.azure.toolkit.intellij.common.AzureBundle.message;


public class FunctionDeploymentPanel extends AzureSettingPanel<FunctionDeployConfiguration> {

    private JPanel pnlRoot;
    private HyperlinkLabel lblCreateFunctionApp;
    private JPanel pnlAppSettings;
    private JComboBox<Module> cbFunctionModule;
    private FunctionAppComboBox functionAppComboBox;
    private JLabel lblModule;
    private JLabel lblFunction;
    private JLabel lblAppSettings;
    private FunctionAppSettingsTable appSettingsTable;
    private String appSettingsKey;
    private Module previousModule = null;

    public FunctionDeploymentPanel(@NotNull Project project, @NotNull FunctionDeployConfiguration functionDeployConfiguration) {
        super(project);
        this.appSettingsKey = functionDeployConfiguration.getAppSettingsKey();
        $$$setupUI$$$();

        cbFunctionModule.setRenderer(new ListCellRendererWrapper<>() {
            @Override
            public void customize(JList list, Module module, int i, boolean b, boolean b1) {
                if (module != null) {
                    setText(module.getName());
                    setIcon(AllIcons.Nodes.Module);
                }
            }
        });
        lblModule.setLabelFor(cbFunctionModule);
        lblFunction.setLabelFor(functionAppComboBox);
        lblAppSettings.setLabelFor(appSettingsTable);
        fillModules();
    }

    @NotNull
    @Override
    public String getPanelName() {
        return message("function.deploy.title");
    }

    @Override
    public void disposeEditor() {

    }

    @NotNull
    @Override
    public JPanel getMainPanel() {
        return pnlRoot;
    }

    @NotNull
    @Override
    protected JComboBox<Artifact> getCbArtifact() {
        return new ComboBox<>();
    }

    @NotNull
    @Override
    protected JLabel getLblArtifact() {
        return new JLabel();
    }

    @NotNull
    @Override
    protected JComboBox<MavenProject> getCbMavenProject() {
        return new ComboBox<>();
    }

    @NotNull
    @Override
    protected JLabel getLblMavenProject() {
        return new JLabel();
    }

    @Override
    protected void resetFromConfig(@NotNull FunctionDeployConfiguration configuration) {
        if (MapUtils.isNotEmpty(configuration.getAppSettings())) {
            appSettingsTable.setAppSettings(configuration.getAppSettings());
        }
        if (StringUtils.isNotEmpty(configuration.getAppSettingsKey())) {
            this.appSettingsKey = configuration.getAppSettingsKey();
        }
        if (!StringUtils.isAllEmpty(configuration.getFunctionId(), configuration.getAppName())) {
            functionAppComboBox.setValue(configuration.getConfig());
            functionAppComboBox.setConfigModel(configuration.getConfig());
            appSettingsTable.setAppSettings(configuration.getConfig().getAppSettings());
        }
        this.previousModule = configuration.getModule();
        selectModule(previousModule);
    }

    @Override
    protected void apply(@NotNull FunctionDeployConfiguration configuration) {
        configuration.setAppSettingsKey(appSettingsKey);
        Optional.ofNullable((Module) cbFunctionModule.getSelectedItem()).ifPresent(configuration::saveTargetModule);
        Optional.ofNullable(functionAppComboBox.getValue())
                .map(value -> value.toBuilder().appSettings(appSettingsTable.getAppSettings()).build())
                .ifPresent(configuration::saveConfig);
    }

    private void createUIComponents() {
        final String localSettingPath = Paths.get(project.getBasePath(), "local.settings.json").toString();
        appSettingsTable = new FunctionAppSettingsTable(localSettingPath);
        pnlAppSettings = FunctionAppSettingsTableUtils.createAppSettingPanel(appSettingsTable);

        functionAppComboBox = new FunctionAppComboBox(project);
        functionAppComboBox.addValueChangedListener((AzureFormInput.AzureValueChangeBiListener<FunctionAppConfig>) this::onSelectFunctionApp);
        functionAppComboBox.reloadItems();
    }

    private void onSelectFunctionApp(final FunctionAppConfig value, final FunctionAppConfig before) {
        final FunctionAppConfig model = getSelectedFunctionApp();
        if (value == null) {
            return;
        }
        this.loadAppSettings(value, before);
    }

    private synchronized void loadAppSettings(@Nonnull FunctionAppConfig value, @Nullable FunctionAppConfig before) {
        final FunctionAppConfig rawValue = functionAppComboBox.getRawValue() instanceof FunctionAppConfig ? (FunctionAppConfig) functionAppComboBox.getRawValue() : value;
        if (Objects.isNull(before) && value != rawValue) {
            // when reset from configuration, leverage app settings from configuration
            if (StringUtils.isEmpty(rawValue.getResourceId()) && StringUtils.isNotEmpty(value.getResourceId())) {
                // if draft has been created, merge local configuration with remote
                appSettingsTable.loadAppSettings(() -> loadDraftAppSettings(rawValue));
            }
        } else if (!Objects.equals(value, before)) {
            appSettingsTable.loadAppSettings(() -> StringUtils.isEmpty(value.getResourceId()) ?
                    value.getAppSettings() : Objects.requireNonNull(Azure.az(AzureFunctions.class).functionApp(value.getResourceId())).getAppSettings());
        }
    }

    private Map<String, String> loadDraftAppSettings(FunctionAppConfig value) {
        final FunctionApp functionApp = Azure.az(AzureFunctions.class).functionApps(value.getSubscriptionId()).get(value.getName(), value.getResourceGroupName());
        return functionApp != null && functionApp.exists() ? MapUtils.putAll(functionApp.getAppSettings(), value.getAppSettings().entrySet().toArray()) : value.getAppSettings();
    }

    private FunctionAppConfig getSelectedFunctionApp() {
        return functionAppComboBox.getValue();
    }

    private void fillModules() {
        AzureTaskManager.getInstance()
                .runOnPooledThreadAsObservable(new AzureTask<>(() -> FunctionUtils.listFunctionModules(project)))
                .subscribe(modules -> AzureTaskManager.getInstance().runLater(() -> {
                    Arrays.stream(modules).forEach(cbFunctionModule::addItem);
                    selectModule(previousModule);
                }, AzureTask.Modality.ANY));
    }

    // todo: @hanli migrate to use AzureComboBox<Module>
    private void selectModule(final Module target) {
        if (target == null) {
            return;
        }
        for (int i = 0; i < cbFunctionModule.getItemCount(); i++) {
            final Module module = cbFunctionModule.getItemAt(i);
            if (Paths.get(module.getModuleFilePath()).equals(Paths.get(target.getModuleFilePath()))) {
                cbFunctionModule.setSelectedIndex(i);
                break;
            }
        }
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
    }
}
