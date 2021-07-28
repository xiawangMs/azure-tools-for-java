package com.microsoft.azure.toolkit.intellij.function.action;

import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.microsoft.azure.toolkit.intellij.function.Constants;
import com.microsoft.azure.toolkit.intellij.function.CreateFunctionForm;
import com.microsoft.azure.toolkit.intellij.function.InitializeFunctionsForm;
import com.microsoft.azure.toolkit.intellij.function.runner.core.CreateFunctionFromFormData;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.Operation;
import com.microsoft.azuretools.telemetrywrapper.TelemetryManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CreateFunctionAction extends CreateElementActionBase {

//    private static final String DEFAULT_EVENT_HUB_CONNECTION_STRING = "Endpoint=sb://<your-envent-hub-namespace>.servicebus.windows.net/;" +
//            "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=<your-SAS-key>;";

    @Override
    protected PsiElement @NotNull [] create(@NotNull String s, PsiDirectory psiDirectory) throws Exception {
        return new PsiElement[0];
    }
    @Override
    protected void invokeDialog(@NotNull Project project, @NotNull PsiDirectory psiDirectory, @NotNull Consumer<PsiElement[]> elementsConsumer) {
        final Operation operation = TelemetryManager.createOperation(TelemetryConstants.FUNCTION, TelemetryConstants.CREATE_FUNCTION_TRIGGER);
        try {
            operation.start();

            CreateFunctionForm form = new CreateFunctionForm(project);

            PsiDirectory baseDir = psiDirectory;
            while (baseDir != null && !baseDir.getVirtualFile().getPath().equals(project.getBasePath())) {
                baseDir = baseDir.getParentDirectory();
            }

            boolean isValidFunction = true;
            List<String> missingFiles = new ArrayList<>();
            for(String fileName : Constants.TEMPLATE_APP_FILES) {
                if(baseDir.findFile(fileName) == null) {
                    isValidFunction = false;
                    missingFiles.add(fileName);
                }
            }

            if(!isValidFunction && missingFiles.size() != Constants.TEMPLATE_APP_FILES.size()) {
                InitializeFunctionsForm warnForm = new InitializeFunctionsForm(project);
                warnForm.AddMissingFiles(missingFiles);
                isValidFunction = !warnForm.showAndGet();
            }

            PsiElement[] psiElements = new PsiElement[]{};
            if (form.showAndGet()) {
                Map<String, String> parameters = form.getTemplateParameters();
                String triggerType = form.getTriggerType();

                CreateFunctionFromFormData formCreator = new CreateFunctionFromFormData(project);
                parameters.put("init_function", String.valueOf(!isValidFunction));
                formCreator.createFunctionFromFormData(operation, parameters, project.getBasePath(), triggerType);
            }
            elementsConsumer.accept(psiElements);
        } finally {
            operation.complete();
        }
        super.invokeDialog(project, psiDirectory, elementsConsumer);
    }

    @Override
    protected @NlsContexts.DialogTitle String getErrorTitle() {
        return null;
    }

    @Override
    protected @NlsContexts.Command String getActionName(PsiDirectory psiDirectory, String s) {
        return null;
    }

    //TODO: Build connection string finders for all trigger types, once user is logged in
//    private String getEventHubNamespaceConnectionString(EventHubNamespace eventHubNamespace) {
//        Azure azure = AuthMethodManager.getInstance().getAzureClient(eventHubNamespace.id().split("/")[2]);
//        EventHubNamespaceAuthorizationRule eventHubNamespaceAuthorizationRule = azure.eventHubNamespaces().
//                authorizationRules().getByName(eventHubNamespace.resourceGroupName(), eventHubNamespace.name(),
//                "RootManageSharedAccessKey");
//        return eventHubNamespaceAuthorizationRule.getKeys().primaryConnectionString();
//    }
}
