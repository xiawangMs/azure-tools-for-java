package com.microsoft.azure.toolkit.intellij.monitor.view.right;

import com.azure.monitor.query.models.LogsTable;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.microsoft.azure.toolkit.intellij.monitor.view.right.TreeTable.LogDataNode;
import com.microsoft.azure.toolkit.intellij.monitor.view.right.TreeTable.LogDataModel;
import com.microsoft.azure.toolkit.lib.monitor.LogAnalyticsWorkspace;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class MonitorTablePanel {
    private JPanel contentPanel;
    private JTable treeTableView;
    private JBCefBrowser jbCefBrowser;

    public MonitorTablePanel() {
    }

    public void setTableModel(LogsTable tableModel) {

    }

    public JPanel getContentPanel() {
        return this.contentPanel;
    }

    public void executeQuery(LogAnalyticsWorkspace workspace) {
//        final LogsTable logsTable = workspace.executeQuery();
//        logsTable
    }

    private static LogDataNode createMockDataStructure() {
        final String propertiesJson = "{\"LoggerName\":\"io.undertow.accesslog\",\"LoggingLevel\":\"INFO\",\"SourceType\":\"Logger\",\"ThreadName\":\"default task-1\"}";
        final List<LogDataNode> propertiesDetailsNodes = new ArrayList<>();
        propertiesDetailsNodes.add(new LogDataNode("prop__{OriginalFormat}", "Stopping JobHost", "", "", null));
        propertiesDetailsNodes.add(new LogDataNode("Category", "Microsoft.Azure.WebJobs.Hosting.JobHostService", "", "", null));
        propertiesDetailsNodes.add(new LogDataNode("ProcessId", "6972", "", "", null));
        propertiesDetailsNodes.add(new LogDataNode("LogLevel", "Information", "", "", null));

        final List<LogDataNode> expandNodes = new ArrayList<>();
        expandNodes.add(new LogDataNode("TimeGenerated [UTC]", "12/5/2022, 5:22:23.884 AM",  "", "", null));
        expandNodes.add(new LogDataNode("Message", "Stopping JobHost", "", "", null));
        expandNodes.add(new LogDataNode("SeverityLevel", "1", "", "", null));
        expandNodes.add(new LogDataNode("Properties", propertiesJson, "", "", propertiesDetailsNodes));

        final List<LogDataNode> dataNodes = new ArrayList<>();
        dataNodes.add(new LogDataNode("12/5/2022, 5:22:23.884 AM", "Stopping JobHost", "1", propertiesJson, expandNodes));
        return new LogDataNode("TimeGenerated [UTC]", "Message", "SeverityLevel", "Properties", dataNodes);
    }

    private void createUIComponents() {
        treeTableView = new TreeTable(new LogDataModel(createMockDataStructure()));
    }
}
