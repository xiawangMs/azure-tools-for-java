/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.actions;

import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.function.runner.localrun.FunctionRunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class RunConfigurationUtils {

    public static RunnerAndConfigurationSettings getOrCreateRunConfigurationSettings(String funcName, Project project, RunManagerEx manager, ConfigurationFactory factory) {
        RunnerAndConfigurationSettings settings = manager.findConfigurationByName(
                String.format("%s: %s:%s", factory.getName(), project.getName(), funcName));
        if (settings == null) {
            settings = manager.createConfiguration(
                    String.format("%s: %s:%s", factory.getName(), project.getName(), funcName),
                    factory);
            if(settings.getConfiguration().getClass() == FunctionRunConfiguration.class) {
                List<String> funcToRun = new ArrayList<>();
                funcToRun.add(funcName);
                ((FunctionRunConfiguration)settings.getConfiguration()).setFunctionsToRun(funcToRun);
            }
        }
        return settings;
    }
}
