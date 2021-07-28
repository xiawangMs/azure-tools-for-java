/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner.functions.deploy;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.microsoft.azure.common.exceptions.AzureExecutionException;
import com.microsoft.azure.common.utils.AppServiceUtils;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.appservice.OperatingSystem;
import com.microsoft.azure.management.appservice.WebAppBase;
import com.microsoft.azuretools.core.mvp.model.function.AzureFunctionMvpModel;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.Operation;
import com.microsoft.azuretools.telemetrywrapper.TelemetryManager;
import com.microsoft.intellij.runner.AzureRunProfileState;
import com.microsoft.intellij.runner.RunProcessHandler;
import com.microsoft.intellij.runner.functions.IntelliJFunctionRuntimeConfiguration;
import com.microsoft.intellij.runner.functions.core.FunctionUtils;
import com.microsoft.intellij.runner.functions.library.function.DeployFunctionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class FunctionDeploymentState extends AzureRunProfileState<WebAppBase> {

    private FunctionDeployConfiguration functionDeployConfiguration;
    private final FunctionDeployModel deployModel;
    private File stagingFolder;

    /**
     * Place to execute the Web App deployment task.
     */
    public FunctionDeploymentState(Project project, FunctionDeployConfiguration functionDeployConfiguration) {
        super(project);
        this.functionDeployConfiguration = functionDeployConfiguration;
        this.deployModel = functionDeployConfiguration.getModel();
    }

    @Nullable
    @Override
    public WebAppBase executeSteps(@NotNull RunProcessHandler processHandler
            , @NotNull Map<String, String> telemetryMap) throws Exception {
        updateTelemetryMap(telemetryMap);
        // Update run time information by function app
        final FunctionApp functionApp = AzureFunctionMvpModel.getInstance()
                .getFunctionById(functionDeployConfiguration.getSubscriptionId(), functionDeployConfiguration.getFunctionId());
        final AppServicePlan appServicePlan = AppServiceUtils.getAppServicePlanByAppService(functionApp);
        final IntelliJFunctionRuntimeConfiguration runtimeConfiguration = new IntelliJFunctionRuntimeConfiguration();
        runtimeConfiguration.setOs(appServicePlan.operatingSystem() == OperatingSystem.WINDOWS ? "windows" : "linux");
        functionDeployConfiguration.setRuntime(runtimeConfiguration);
        functionDeployConfiguration.setPricingTier(appServicePlan.pricingTier().toSkuDescription().size());
        // Deploy function to Azure
        stagingFolder = FunctionUtils.getTempStagingFolder();
        deployModel.setDeploymentStagingDirectoryPath(stagingFolder.getPath());
        prepareStagingFolder(stagingFolder, processHandler);
        final DeployFunctionHandler deployFunctionHandler = new DeployFunctionHandler(deployModel, message -> {
            if (processHandler.isProcessRunning()) {
                processHandler.setText(message);
            }
        });
        return deployFunctionHandler.execute();
    }

    private void prepareStagingFolder(File stagingFolder, RunProcessHandler processHandler)
            throws AzureExecutionException {
        ReadAction.run(() -> {
            final Path hostJsonPath = FunctionUtils.getDefaultHostJson(project);
            final PsiMethod[] methods = FunctionUtils.findFunctionsByAnnotation(functionDeployConfiguration.getModule());
            try {
                FunctionUtils.prepareStagingFolder(stagingFolder.toPath(), hostJsonPath, functionDeployConfiguration.getModule(), methods);
            } catch (AzureExecutionException | IOException e) {
                throw new AzureExecutionException("Failed to prepare staging folder");
            }
        });
    }

    @Override
    protected Operation createOperation() {
        return TelemetryManager.createOperation(TelemetryConstants.FUNCTION, TelemetryConstants.DEPLOY_FUNCTION_APP);
    }

    @Override
    protected void onSuccess(WebAppBase result, @NotNull RunProcessHandler processHandler) {
        processHandler.setText("Deploy succeed");
        processHandler.notifyComplete();
        FunctionUtils.cleanUpStagingFolder(stagingFolder);
    }

    @Override
    protected void onFail(String errMsg, @NotNull RunProcessHandler processHandler) {
        processHandler.println(errMsg, ProcessOutputTypes.STDERR);
        processHandler.notifyComplete();
        FunctionUtils.cleanUpStagingFolder(stagingFolder);
    }

    @Override
    protected String getDeployTarget() {
        return "FUNCTION";
    }

    @Override
    protected void updateTelemetryMap(@NotNull Map<String, String> telemetryMap) {
        telemetryMap.putAll(functionDeployConfiguration.getModel().getTelemetryProperties(telemetryMap));
    }
}
