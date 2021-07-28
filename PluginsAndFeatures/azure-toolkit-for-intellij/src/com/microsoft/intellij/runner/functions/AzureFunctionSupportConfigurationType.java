/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner.functions;

import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.icons.AllIcons;
import com.microsoft.intellij.runner.functions.deploy.FunctionDeploymentConfigurationFactory;
import com.microsoft.intellij.runner.functions.localrun.FunctionRunConfigurationFactory;
import com.microsoft.intellij.util.PluginUtil;

import javax.swing.*;

import static com.microsoft.intellij.runner.functions.AzureFunctionsConstants.AZURE_FUNCTIONS_ICON;

public class AzureFunctionSupportConfigurationType extends ConfigurationTypeBase implements ConfigurationType {

    public static final String ICON_PATH = "/icons/" + AZURE_FUNCTIONS_ICON;

    protected AzureFunctionSupportConfigurationType() {
        super("AZURE_FUNCTION_SUPPORT", AzureFunctionsConstants.DISPLAY_NAME, "Execute the azure functions", AllIcons.Actions.Execute);
        addFactory(new FunctionRunConfigurationFactory(this));
        addFactory(new FunctionDeploymentConfigurationFactory(this));
    }

    public static AzureFunctionSupportConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(AzureFunctionSupportConfigurationType.class);
    }

    @Override
    public String getDisplayName() {
        return AzureFunctionsConstants.DISPLAY_NAME;
    }

    @Override
    public Icon getIcon() {
        return PluginUtil.getIcon(ICON_PATH);
    }
}
