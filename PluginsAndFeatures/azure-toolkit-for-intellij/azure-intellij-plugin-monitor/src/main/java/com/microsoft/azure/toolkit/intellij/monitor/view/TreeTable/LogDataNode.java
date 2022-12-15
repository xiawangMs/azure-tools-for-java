package com.microsoft.azure.toolkit.intellij.monitor.view.TreeTable;

import java.util.Collections;
import java.util.List;

public class LogDataNode {

    private final String timeGenerated;
    private final String message;
    private final String severityLevel;
    private final String properties;

    private List<LogDataNode> children;

    public LogDataNode(String name, String message, String severityLevel, String properties, List<LogDataNode> children) {
        this.timeGenerated = name;
        this.message = message;
        this.severityLevel = severityLevel;
        this.properties = properties;
        this.children = children;
        if (this.children == null) {
            this.children = Collections.emptyList();
        }
    }

    public String getName() {
        return timeGenerated;
    }

    public String getMessage() {
        return message;
    }

    public String getSeverityLevel() {
        return severityLevel;
    }

    public String getProperties() {
        return properties;
    }

    public List<LogDataNode> getChildren() {
        return children;
    }

    public String toString() {
        return timeGenerated;
    }
}
