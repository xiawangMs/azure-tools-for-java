package com.microsoft.azure.toolkit.intellij.function.runner.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.microsoft.azure.toolkit.intellij.function.Constants;
import com.microsoft.azure.toolkit.intellij.function.CreateFunctionForm;
import com.microsoft.azure.toolkit.lib.common.exception.AzureExecutionException;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azuretools.telemetrywrapper.ErrorType;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import com.microsoft.azuretools.telemetrywrapper.Operation;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateFunctionFromFormData {

    private CLIExecutor executor;
    private String funcPath;
    private Project project;

    public CreateFunctionFromFormData(Project proj) {
        project = proj;
        executor = new CLIExecutor(project);
        try {
            funcPath = FuncNameResolver.resolveFunc();
            if (funcPath == null || funcPath.equals("")) {
                throw new Exception("Could not resolve the path to Azure Core Functions");
            }
        } catch (Exception ex) {
            new CLIOutputWriter(project).printLineToConsole(ex.getMessage());
            //TODO: Rather, show instructions on how to install
        }
    }

    public void createFunctionFromFormData(Operation operation, Map<String,String> parameters, String baseDir, String triggerType) {
        try {
            boolean initDirectory = Boolean.parseBoolean(parameters.get("init_function"));
            String functionName = parameters.get("function_name");

            if (initDirectory) {
                runFuncInit(baseDir);
            }
            runFuncNew(parameters, baseDir, triggerType, functionName);
            //TODO: Refresh the file directory?

            JsonObject localSettings = null;
            JsonObject functionJson = null;
            JsonObject localSettingsValues = null;
            JsonArray functionJsonBindings = null;
            String functionDir = baseDir + File.separator + functionName;
            try {
                localSettings = JsonFileManipulator.retrieveLocalSettings(baseDir);
                localSettingsValues = localSettings.get(Constants.LOCAL_SETTINGS_VALUES).getAsJsonObject();

            } catch (IOException e) {
                // TODO: Log this message that it couldn't process the localSettings.
            }
            try {
                functionJson = JsonFileManipulator.retrieveFunctionJson(functionDir);
                functionJsonBindings = functionJson.get(Constants.FUNCTION_JSON_BINDINGS).getAsJsonArray();
            } catch (IOException e) {
                // TODO: Log this message that it couldn't process the function.json.
            }

            if(functionJson == null || functionJsonBindings == null) {
                throw new AzureExecutionException("Could not find or incorrectly formatted function.json for function " + functionName);
            }
            if(localSettings == null || localSettingsValues == null) {
                throw new AzureExecutionException("Could not find or incorrectly formatted local.settings.json");
            }
            String connection;

            JsonObject inBinding = null;
            if(triggerType.equals(CreateFunctionForm.COSMOS_DB_TRIGGER)
                    || triggerType.equals(CreateFunctionForm.EVENT_HUB_TRIGGER)
                    || triggerType.equals(CreateFunctionForm.SERVICEBUS_QUEUE_TRIGGER)
                    || triggerType.equals(CreateFunctionForm.SERVICEBUS_TOPIC_TRIGGER)) {
                connection = parameters.get("connection");
                if(!localSettingsValues.has(connection)) {
                    localSettingsValues.addProperty(connection, Constants.TEMPLATE_CONNECTION_STRING);
                }

                switch (triggerType) {
                    case CreateFunctionForm.COSMOS_DB_TRIGGER:
                        while (functionJsonBindings.iterator().hasNext()) {
                            var binding = functionJsonBindings.iterator().next().getAsJsonObject();
                            if (binding.has("type") && binding.get("type").getAsString().equals("cosmosDBTrigger")
                                    && binding.has("direction") && binding.get("direction").getAsString().equals("in")) {
                                inBinding = binding;
                                break;
                            }
                        }
                        if (inBinding != null) {
                            inBinding.addProperty("databaseName", parameters.get("database"));
                            inBinding.addProperty("collectionName", parameters.get("collection"));
                            inBinding.addProperty("leaseCollectionName", parameters.get("leases"));
                            inBinding.addProperty("createLeaseCollectionIfNotExists", parameters.get("create_leases"));
                        }
                        break;
                    case CreateFunctionForm.EVENT_HUB_TRIGGER:
                        while (functionJsonBindings.iterator().hasNext()) {
                            var binding = functionJsonBindings.iterator().next().getAsJsonObject();
                            if (binding.has("type") && binding.get("type").getAsString().equals("eventHubTrigger")
                                    && binding.has("direction") && binding.get("direction").getAsString().equals("in")) {
                                inBinding = binding;
                                break;
                            }
                        }
                        if (inBinding != null) {
                            inBinding.addProperty("eventHubName", parameters.get("event_hub_name"));
                            inBinding.addProperty("consumerGroup", parameters.get("consumer_group"));
                        }
                        break;
                    case CreateFunctionForm.SERVICEBUS_QUEUE_TRIGGER:
                        while (functionJsonBindings.iterator().hasNext()) {
                            var binding = functionJsonBindings.iterator().next().getAsJsonObject();
                            if (binding.has("type") && binding.get("type").getAsString().equals("serviceBusTrigger")
                                    && binding.has("direction") && binding.get("direction").getAsString().equals("in")) {
                                inBinding = binding;
                                break;
                            }
                        }
                        if (inBinding != null) {
                            inBinding.addProperty("queueName", parameters.get("queue"));
                        }
                        break;
                    case CreateFunctionForm.SERVICEBUS_TOPIC_TRIGGER:
                        while (functionJsonBindings.iterator().hasNext()) {
                            var binding = functionJsonBindings.iterator().next().getAsJsonObject();
                            if (binding.has("type") && binding.get("type").getAsString().equals("serviceBusTrigger")
                                    && binding.has("direction") && binding.get("direction").getAsString().equals("in")) {
                                inBinding = binding;
                                break;
                            }
                        }
                        if (inBinding != null) {
                            inBinding.addProperty("topicName", parameters.get("topic"));
                            inBinding.addProperty("subscriptionName", parameters.get("subscription"));
                        }
                        break;
                    default:
                        break;
                }

                if(inBinding != null) {
                    inBinding.addProperty("connection", connection);
                }
            }
            if(triggerType.equals(CreateFunctionForm.TIMER_TRIGGER)) {
                while(functionJsonBindings.iterator().hasNext()) {
                    var binding = functionJsonBindings.iterator().next().getAsJsonObject();
                    if(binding.has("type") && binding.get("type").getAsString().equals("timerTrigger")) {
                        inBinding = binding;
                        break;
                    }
                }
                if(inBinding != null) {
                    inBinding.addProperty("schedule", parameters.get("schedule"));
                }
            }
            JsonFileManipulator.writeLocalSettings(localSettings, baseDir);
            JsonFileManipulator.writeFunctionJson(functionJson, functionDir);
        } catch (Exception e) {
            EventUtil.logError(operation, ErrorType.systemError, e, null, null);
        }
    }

    private void runFuncNew(Map<String, String> parameters, String baseDir, String triggerType, String functionName) throws AzureExecutionException {
        String[] azCoreToolsCliCommand = new String[]{funcPath,
                "new", "--worker-runtime", "python", "--language", "python", "--template", triggerType, "--name", functionName};
        if(triggerType.equals(CreateFunctionForm.HTTP_TRIGGER)) {
            String authLevel = parameters.get("authLevel");
            azCoreToolsCliCommand = ArrayUtils.addAll(azCoreToolsCliCommand, "--authlevel", authLevel);
        }
        if (!triggerType.equals(CreateFunctionForm.NO_TRIGGER)) {
            String successMessage = "Created Azure Function";
            String errorMessage = "Failed to create Azure Function";
            executor.execCommand(azCoreToolsCliCommand, baseDir, successMessage, errorMessage, true, null);
        }
    }

    public void runFuncInit(String baseDir) {
        try {
            String[] command = new String[]{funcPath, "init", "--worker-runtime", "python"};
            String successMessage = "Successfully created function app";
            String errorMessage = "Failed to create app";
            executor.execCommand(command, baseDir, successMessage, errorMessage, true, null);

            PsiDirectory basePSI = PsiDirectoryFactory.getInstance(project).createDirectory(project.getBaseDir());
            List<String> missingBindings = new ArrayList<>();
            for(PsiDirectory possibleFuncDir : basePSI.getSubdirectories()) {
                List<String> missingFiles = new ArrayList<>();
                for(String funcFileName : Constants.UPLOAD_FUNC_FILES) {
                    if(possibleFuncDir.findFile(funcFileName) == null) {
                        missingFiles.add(funcFileName);
                    }
                }
                if(missingFiles.size() == 0) {
                    try {
                        JsonObject model = JsonFileManipulator.retrieveFunctionJson(project.getBasePath() + File.separator + possibleFuncDir.getName());
                        for(var binding : model.get("bindings").getAsJsonArray()) {
                            if(binding.getAsJsonObject().has("connection")) {
                                missingBindings.add(binding.getAsJsonObject().get("connection").getAsString());
                            }
                        }
                    } catch (Exception ex) {
                        //TODO: Log failiure
                    }
                }
            }
            try {
                JsonObject localSettings = JsonFileManipulator.retrieveLocalSettings(project.getBasePath());
                for (String missingBinding : missingBindings) {
                    if (!localSettings.get("Values").getAsJsonObject().has(missingBinding)) {
                        localSettings.get("Values").getAsJsonObject().addProperty(missingBinding, Constants.TEMPLATE_CONNECTION_STRING);
                    }
                }
                JsonFileManipulator.writeLocalSettings(localSettings, project.getBasePath());
            }
            catch (Exception ex) {
                //TODO: Log failiure
            }

        } catch (Exception ex) {
            throw new AzureToolkitRuntimeException("failed to create azure function", ex);
        }
    }
}
