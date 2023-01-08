package com.microsoft.azure.toolkit.intellij.monitor.view.left;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.microsoft.azure.toolkit.intellij.common.component.SubscriptionComboBox;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class WorkspaceSelectionDialog extends DialogWrapper {
    private JPanel centerPanel;
    private JComboBox subComboBox;
    private JComboBox workspaceComboBox;

    public WorkspaceSelectionDialog(@Nullable final Project project) {
        super(project, false);
    }


    @Override
    protected @Nullable JComponent createCenterPanel() {
        return centerPanel;
    }

    private void createUIComponents() {
        subComboBox = new SubscriptionComboBox();
//        workspaceComboBox = new Work
    }

}
