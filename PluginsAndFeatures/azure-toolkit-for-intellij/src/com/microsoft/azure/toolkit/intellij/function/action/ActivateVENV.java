package com.microsoft.azure.toolkit.intellij.function.action;

import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ActivateVENV extends CreateElementActionBase {
    @Override
    protected PsiElement @NotNull [] create(@NotNull String s, PsiDirectory psiDirectory) throws Exception {
        return new PsiElement[0];
    }

    @Override
    protected @NlsContexts.DialogTitle String getErrorTitle() {
        return null;
    }

    @Override
    protected @NlsContexts.Command String getActionName(PsiDirectory psiDirectory, String s) {
        return null;
    }

    @Override
    protected void invokeDialog(@NotNull Project project, @NotNull PsiDirectory directory, @NotNull Consumer<PsiElement[]> elementsConsumer) {
        super.invokeDialog(project, directory, elementsConsumer);
//        CLIExecutor executor = new CLIExecutor(project);
//
//        String[] command = new String[] {"dir"};
//        try {
//            executor.execCommand(command, project.getBasePath());
//        }
//        catch (AzureExecutionException executionException) {
//
//        }
//
//        command = new String[] {"dir"};
//        try {
//            executor.execCommand(command, project.getBasePath());
//        }
//        catch (AzureExecutionException executionException) {
//
//        }
//
//        command = new String[] {project.getBasePath() + "\\venv\\Scripts\\activate.bat"};
//        try {
//            executor.execCommand(command, project.getBasePath());
//        }
//        catch (AzureExecutionException executionException) {
//
//        }
//
//        command = new String[] {"where", "python"};
//        try {
//            executor.execCommand(command, project.getBasePath());
//        }
//        catch (AzureExecutionException executionException) {
//
//        }
//
//        command = new String[] {"py", "-m", "pip", "install", "-r", "requirements.txt"};
//        try {
//            executor.execCommand(command, project.getBasePath());
//        }
//        catch (AzureExecutionException executionException) {
//
//        }
    }
}
