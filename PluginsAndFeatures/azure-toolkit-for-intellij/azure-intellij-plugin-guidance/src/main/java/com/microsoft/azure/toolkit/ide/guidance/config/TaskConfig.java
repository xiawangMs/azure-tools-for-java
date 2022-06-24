package com.microsoft.azure.toolkit.ide.guidance.config;

import lombok.Data;

import java.util.Map;

@Data
public class TaskConfig {
    private String name;
    private String description;
    private Map<String, String> paramMapping;
    private Map<String, String> resultMapping;
}
