package com.microsoft.azure.toolkit.intellij.appservice.task;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.appservice.function.FunctionAppConfig;
import com.microsoft.azure.toolkit.ide.guidance.ComponentContext;
import com.microsoft.azure.toolkit.ide.guidance.Guidance;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceTask;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.AzureFunctionSupportConfigurationType;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.core.FunctionUtils;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.deploy.FunctionDeployConfiguration;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.deploy.FunctionDeploymentConfigurationFactory;
import com.microsoft.azure.toolkit.intellij.legacy.webapp.runner.WebAppConfigurationType;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.function.AzureFunctions;
import com.microsoft.azure.toolkit.lib.appservice.function.FunctionApp;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.utils.Utils;
import com.microsoft.azure.toolkit.lib.legacy.function.FunctionAppService;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class DeployFunctionAppTask implements GuidanceTask {
    private final AzureFunctionSupportConfigurationType functionType = AzureFunctionSupportConfigurationType.getInstance();

    private final Project project;
    private final Guidance guidance;
    private final ComponentContext context;

    public DeployFunctionAppTask(@Nonnull ComponentContext context) {
        this.context = context;
        this.project = context.getProject();
        this.guidance = context.getGuidance();
    }

    @Override
    public void execute() throws Exception {
        AzureMessager.getMessager().info("Setting up run configuration for function app deployment...");
        final RunManagerEx manager = RunManagerEx.getInstanceEx(project);
        final RunnerAndConfigurationSettings settings = getRunConfigurationSettings((String) context.getParameter("functionId"), manager);
        manager.addConfiguration(settings);
        manager.setSelectedConfiguration(settings);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final ExecutionEnvironmentBuilder executionEnvironmentBuilder = ExecutionEnvironmentBuilder.create(DefaultRunExecutor.getRunExecutorInstance(), settings);
        AzureMessager.getMessager().info(AzureString.format("Executing run configuration %s...", settings.getName()));
        final ExecutionEnvironment build = executionEnvironmentBuilder.contentToReuse(null).dataContext(null).activeTarget().build();
        ProgramRunnerUtil.executeConfigurationAsync(build, true, true, runContentDescriptor ->
                Objects.requireNonNull(runContentDescriptor.getProcessHandler()).addProcessListener(new ProcessAdapter() {
                    @Override
                    public void processTerminated(@NotNull ProcessEvent event) {
                        countDownLatch.countDown();
                    }
                }));
        countDownLatch.await();
    }

    private RunnerAndConfigurationSettings getRunConfigurationSettings(final String appId, final RunManagerEx manager) {
        final ConfigurationFactory factory = new FunctionDeploymentConfigurationFactory(functionType);
        final String runConfigurationName = String.format("Azure Sample: %s-%s", guidance.getName(), Utils.getTimestamp());
        final RunnerAndConfigurationSettings settings = manager.createConfiguration(runConfigurationName, factory);
        final RunConfiguration runConfiguration = settings.getConfiguration();
        if (runConfiguration instanceof FunctionDeployConfiguration) {
            final Module[] functionModules = FunctionUtils.listFunctionModules(project);
            ((FunctionDeployConfiguration) runConfiguration).saveTargetModule(functionModules[0]);
            final FunctionApp functionApp = Azure.az(AzureFunctions.class).functionApp(appId);
            final FunctionAppConfig config = FunctionAppService.getInstance().getFunctionAppConfigFromExistingFunction(Objects.requireNonNull(functionApp));
            ((FunctionDeployConfiguration) runConfiguration).saveConfig(config);
        }
        return settings;
    }

    @Nonnull
    @Override
    public String getName() {
        return "task.function.deploy";
    }
}
