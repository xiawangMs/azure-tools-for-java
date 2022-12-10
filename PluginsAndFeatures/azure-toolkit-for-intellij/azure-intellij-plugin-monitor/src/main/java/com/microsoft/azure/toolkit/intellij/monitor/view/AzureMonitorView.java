package com.microsoft.azure.toolkit.intellij.monitor.view;



import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;

import javax.swing.*;

public class AzureMonitorView {
    private JPanel contentPanel;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private JPanel leftPanel;
    private JScrollPane rightPane;
    private MonitorTreePanel monitorTreePanel;
    private MonitorTablePanel monitorTablePanel;

    public AzureMonitorView(Project project) {
        AzureTaskManager.getInstance().runInBackground(AzureString.fromString("loading Azure Monitor data"), () -> this.monitorTreePanel.refresh());
    }

    public JPanel getCenterPanel() {
        return contentPanel;
    }


}
