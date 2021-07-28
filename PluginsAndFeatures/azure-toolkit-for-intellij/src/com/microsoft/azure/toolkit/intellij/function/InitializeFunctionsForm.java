package com.microsoft.azure.toolkit.intellij.function;

import com.intellij.openapi.project.Project;
import com.microsoft.azuretools.telemetry.TelemetryProperties;
import com.microsoft.intellij.ui.components.AzureDialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitializeFunctionsForm extends AzureDialogWrapper implements TelemetryProperties {
    Project project;
    private JPanel pnlRoot;
    private JLabel txtMissingFiles;

    public InitializeFunctionsForm(Project project) {
        super((Project) null, true);
        setTitle("Initialize Azure Functions project for use with PyCharm");

        this.project = project;

        this.setOKButtonText("Fix");
        this.setCancelButtonText("Ignore");

        init();
    }

    public void AddMissingFiles(List<String> missingFiles) {
        txtMissingFiles.setText(txtMissingFiles.getText() + " " + String.join(", ", missingFiles));
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return pnlRoot;
    }

    @Override
    public Map<String, String> toProperties() {
        return new HashMap<>();
    }
}
