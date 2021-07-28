package com.microsoft.azure.toolkit.lib.appservice.model;

import java.util.List;

public class PythonRuntime extends Runtime{

    public static final Runtime FUNCTION_LINUX_PYTHON36 = new Runtime(OperatingSystem.LINUX, WebContainer.JAVA_OFF, PythonVersion.PYTHON_3_6);
    public static final Runtime FUNCTION_LINUX_PYTHON37 = new Runtime(OperatingSystem.LINUX, WebContainer.JAVA_OFF, PythonVersion.PYTHON_3_7);
    public static final Runtime FUNCTION_LINUX_PYTHON38 = new Runtime(OperatingSystem.LINUX, WebContainer.JAVA_OFF, PythonVersion.PYTHON_3_8);
    public static final Runtime FUNCTION_LINUX_PYTHON39 = new Runtime(OperatingSystem.LINUX, WebContainer.JAVA_OFF, PythonVersion.PYTHON_3_9);

    public static final List<Runtime> PYTHON_FUNCTION_APP_RUNTIME = List.of(FUNCTION_LINUX_PYTHON36, FUNCTION_LINUX_PYTHON37, FUNCTION_LINUX_PYTHON38, FUNCTION_LINUX_PYTHON39);

}
