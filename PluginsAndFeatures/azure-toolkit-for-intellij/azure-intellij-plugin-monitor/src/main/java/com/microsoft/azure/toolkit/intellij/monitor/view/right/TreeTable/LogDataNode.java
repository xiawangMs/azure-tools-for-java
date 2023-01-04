package com.microsoft.azure.toolkit.intellij.monitor.view.right.TreeTable;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LogDataNode {

    private List<LogDataNode> children;
    private final List<String> columnValues;

    public LogDataNode(List<String> values, List<LogDataNode> children) {
        this.columnValues = values;
        this.children = children;
        if (this.children == null) {
            this.children = Collections.emptyList();
        }
    }

    public List<LogDataNode> getChildren() {
        return children;
    }

    public List<String> getColumnValues() {
        return this.columnValues;
    }

    @Override
    public String toString() {
        if (Objects.isNull(columnValues) || this.columnValues.size() == 0) {
            return StringUtils.EMPTY;
        }
        return this.columnValues.get(0);
    }
}
