package com.microsoft.azure.toolkit.intellij.function.action;

import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.microsoft.azure.toolkit.intellij.function.Constants;
import com.microsoft.azure.toolkit.intellij.function.runner.core.CreateFunctionFromFormData;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ReInitFunctionsAction extends CreateElementActionBase {

    private Set<String> missingFiles = new HashSet<>();

    @Override
    protected PsiElement @NotNull [] create(@NotNull String s, PsiDirectory psiDirectory) throws Exception {
        return new PsiElement[0];
    }

    @Override
    protected void invokeDialog(@NotNull Project project, @NotNull PsiDirectory directory, @NotNull Consumer<PsiElement[]> elementsConsumer) {
        CreateFunctionFromFormData creator = new CreateFunctionFromFormData(project);
        creator.runFuncInit(project.getBasePath());
    }

    @Override
    protected boolean isAvailable(@NotNull AnActionEvent e) {
        //TODO: Grey rather than vanish?
        Project project = e.getProject();
        missingFiles.clear();
        PsiDirectory baseDir = PsiDirectoryFactory.getInstance(project).createDirectory(project.getBaseDir());
        for(String fileName : Constants.TEMPLATE_APP_FILES) {
            if(baseDir.findFile(fileName) == null && !missingFiles.contains(fileName)) {
                missingFiles.add(fileName);
            }
        }
        e.getPresentation().setEnabled(missingFiles.size() > 0);
        return true;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean visible = this.isAvailable(e);
        e.getPresentation().setVisible(visible);
    }

    @Override
    protected @NlsContexts.DialogTitle String getErrorTitle() {
        return null;
    }

    @Override
    protected @NlsContexts.Command String getActionName(PsiDirectory psiDirectory, String s) {
        return null;
    }
}
