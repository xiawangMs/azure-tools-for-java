package com.microsoft.azure.toolkit.intellij.function.action;

import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.microsoft.azure.toolkit.intellij.function.Constants;
import com.microsoft.azure.toolkit.intellij.function.runner.core.CLIExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StopFunctionAction extends CreateElementActionBase {
    @Override
    protected PsiElement @NotNull [] create(@NotNull String s, PsiDirectory psiDirectory) throws Exception {
        return new PsiElement[0];
    }

    @Override
    protected void invokeDialog(@NotNull Project project, @NotNull PsiDirectory directory, @NotNull Consumer<PsiElement[]> elementsConsumer) {
        CLIExecutor.cancelProcess(directory.getName());
    }

    @Override
    protected boolean isAvailable(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        List<String> missingFiles = new ArrayList<>();
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if(project == null) {
            return false;
        }
        final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        if(view != null) {
            for(PsiDirectory directory : view.getDirectories())
            {
                for(String fileName : Constants.UPLOAD_FUNC_FILES) {
                    if(directory.findFile(fileName) == null && !missingFiles.contains(fileName)) {
                        missingFiles.add(fileName);
                    }
                }
                if(missingFiles.size() == 0) {
                    if(CLIExecutor.runningProcesses.containsKey(directory.getName())) {
                        return true;
                    }
                    else {
                        e.getPresentation().setEnabled(false);
                        return true;
                    }
                }
            }
        }
        return false;
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
