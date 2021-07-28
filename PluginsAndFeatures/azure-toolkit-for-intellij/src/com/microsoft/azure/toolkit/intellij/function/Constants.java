package com.microsoft.azure.toolkit.intellij.function;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Constants {
    public static final Set<String> TEMPLATE_APP_FILES = new HashSet<>(Arrays.asList("local.settings.json", "host.json", "requirements.txt"));
    public static final Set<String> UPLOAD_APP_FILES = new HashSet<>(Arrays.asList("local.settings.json", "host.json", "requirements.txt"));
    public static final Set<String> TEMPLATE_FUNC_FILES = new HashSet<>(Arrays.asList("function.json", "__init__.py"));
    public static final Set<String> UPLOAD_FUNC_FILES = new HashSet<>(Arrays.asList("function.json"));

    public static final String LOCAL_SETTINGS_VALUES = "Values";
    public static final String FUNCTION_JSON_BINDINGS = "bindings";

    public static final String TEMPLATE_CONNECTION_STRING = "Your connection string here";
}
