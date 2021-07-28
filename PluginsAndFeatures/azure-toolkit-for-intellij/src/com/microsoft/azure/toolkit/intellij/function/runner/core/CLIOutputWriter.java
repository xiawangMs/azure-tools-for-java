package com.microsoft.azure.toolkit.intellij.function.runner.core;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.appservice.CLIConsoleViewManager;
import com.microsoft.intellij.RunProcessHandler;

public class CLIOutputWriter {
    public static final RunProcessHandler processHandler = new RunProcessHandler();

    public CLIOutputWriter(Project project) {
        processHandler.addDefaultListener();

        if(!processHandler.isStartNotified()) {
            processHandler.startNotify();
        }
        ConsoleView consoleView = CLIConsoleViewManager.getInstance().getConsoleView(project);
        if(consoleView != null) {
            CLIConsoleViewManager.getInstance().showStreamingLogConsole(project, "CLI Output", "CLI Output", consoleView);
            consoleView.attachToProcess(processHandler);
        }
    }

    public void printLineToConsole(String line) {
        //TODO: Pop the console to the front?
        processHandler.println(line, ProcessOutputTypes.SYSTEM);

    }
}
