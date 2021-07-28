package com.microsoft.azure.toolkit.intellij.function.runner.core;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.exception.AzureExecutionException;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.intellij.RunProcessHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class CLIExecutor {
    public static Map<String, Process> runningProcesses = new HashMap<>();
    public static ProcessBuilder pb = new ProcessBuilder();
    public static Process runningShell;
    public static Process runAllProcess;

    public Project localProject = null;

    private static CLIOutputWriter cliOutputWriter;

    public CLIExecutor(Project project) {
        localProject = project;
        cliOutputWriter = new CLIOutputWriter(project);
    }

    public static Map<String, Process> getRunningProcesses() {
        return runningProcesses;
    }
    public static void setRunningProcesses(Map<String, Process> newRunningProcesses) {
        runningProcesses = newRunningProcesses;
    }

    public void execCommand(String[] command, String execDirectory) throws AzureExecutionException {
        execCommand(command, execDirectory, "Successfully Executed", "Failed to execute", true, null);
    }

    public void execCommand(String[] command, String execDirectory, boolean waitFor) throws AzureExecutionException {
        execCommand(command, execDirectory, "Successfully Executed", "Failed to execute", waitFor, null);
    }

    public void execCommand(String[] command, String execDirectory, String successMessage, String errorMessage, boolean waitFor, @Nullable RunProcessHandler handler) throws AzureExecutionException {
        try {
            pb = new ProcessBuilder();
            pb.directory(new File(execDirectory));
            pb.command(command);

            printLine("Executing command: " + String.join(" ", command), handler);

            Process pr = pb.start();

            if(waitFor) {
                finishProcess(successMessage, errorMessage, pr, handler);
            }
            else {
                if(command.length > 4 && command[3].equals("--functions") && !runningProcesses.containsKey(command[4])) {
                    runningProcesses.put(command[4], pr);
                }
                else {
                    runAllProcess = pr;
                }
                final AzureString title = AzureOperationBundle.title("function.run.state");
                final AzureTask task = new AzureTask(localProject, title, true, () -> {
                    try {
                        finishProcess(successMessage, errorMessage, pr, handler);
                    } catch (Exception ex) {
                        //TODO: log?
                    } finally {
                        if(runningProcesses.containsKey(command[4])) {
                            runningProcesses.remove(command[4]);
                        }
                    }
                }, AzureTask.Modality.ANY);
                AzureTaskManager.getInstance().runInBackground(task);
            }
        }
        catch (Exception ex) {
            throw new AzureToolkitRuntimeException(errorMessage + "\n" + ex.getMessage());
        }
    }

    public static void printLine(String message, RunProcessHandler handler) {
        if(handler == null) {
            cliOutputWriter.printLineToConsole(message);
        }
        else {
            handler.println(message, ProcessOutputTypes.SYSTEM);
        }
    }

    public static void cancelProcess() {
        if(runAllProcess != null && runAllProcess.isAlive()) {
            runAllProcess.destroy();
        }
    }

    public static void cancelProcess(String funcName) {
        if(runningProcesses.containsKey(funcName)) {
            runningProcesses.get(funcName).destroy();
            runningProcesses.remove(funcName);
        }
    }

    private void finishProcess(String successMessage, String errorMessage, Process pr, RunProcessHandler runProcessHandler) throws IOException, InterruptedException {
        BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = null;
        while ((line = input.readLine()) != null) {
            printLine(line, runProcessHandler);
        }
        int exitVal = pr.waitFor();

        if (exitVal == 0) {
            printLine("\n" + successMessage + "\n\n", runProcessHandler);
        } else {
            printLine("\n" + errorMessage + " - Exited with error code " + exitVal + "\n\n", runProcessHandler);
        }
    }
}
