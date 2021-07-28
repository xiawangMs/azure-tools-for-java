package com.microsoft.azure.toolkit.intellij.function.runner.core;

import com.azure.core.exception.AzureException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.microsoft.azure.toolkit.intellij.function.Constants;
import com.microsoft.azure.toolkit.lib.common.exception.AzureExecutionException;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.legacy.function.bindings.Binding;
import com.microsoft.azure.toolkit.lib.legacy.function.bindings.BindingEnum;
import com.microsoft.azure.toolkit.lib.legacy.function.configurations.FunctionConfiguration;
import com.microsoft.azuretools.utils.CommandUtils;
import com.microsoft.azuretools.utils.JsonUtils;
import com.microsoft.intellij.secure.IdeaSecureStore;
import com.spotify.docker.client.shaded.com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FunctionUtils {

    public static final String FUNCTION_PYTHON_LIBRARY_ID = "azure-functions-java-library";
    private static final String FUNCTION_JSON = "function.json";
    private static final int MAX_PORT = 65535;
    private static final String DEFAULT_HOST_JSON = "{\n" +
            "  \"version\": \"2.0\",\n" +
            "  \"logging\": {\n" +
            "    \"applicationInsights\": {\n" +
            "      \"samplingSettings\": {\n" +
            "        \"isEnabled\": true,\n" +
            "        \"excludedTypes\": \"Request\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"extensionBundle\": {\n" +
            "    \"id\": \"Microsoft.Azure.Functions.ExtensionBundle\",\n" +
            "    \"version\": \"[2.*, 3.0.0)\"\n" +
            "  }\n" +
            "}\n";

    public static String getFuncPath() throws IOException, InterruptedException {
        return FuncNameResolver.resolveFunc();
    }

    public static Map<String, String> loadAppSettingsFromSecurityStorage(String key) {
        if (StringUtils.isEmpty(key)) {
            return new HashMap<>();
        }
        final String value = IdeaSecureStore.getInstance().loadPassword(key);
        return StringUtils.isEmpty(value) ? new HashMap<>() : JsonUtils.fromJson(value, Map.class);
    }

    public static void saveAppSettingsToSecurityStorage(String key, Map<String, String> appSettings) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        final String appSettingsJsonValue = JsonUtils.toJsonString(appSettings);
        IdeaSecureStore.getInstance().savePassword(key, appSettingsJsonValue);
    }

    public static Module[] listFunctionModules(Project project) {
        final Module[] modules = ModuleManager.getInstance(project).getModules();
        return Arrays.stream(modules).filter(m -> {
            if (isModuleInTestScope(m)) {
                return false;
            }
//            final GlobalSearchScope scope = GlobalSearchScope.moduleWithLibrariesScope(m);
//            final PsiClass ecClass = JavaPsiFacade.getInstance(project).findClass(AZURE_FUNCTION_ANNOTATION_CLASS,
//                    scope);
//            return ecClass != null;
            return true;
        }).toArray(Module[]::new);
    }

    public static Module getFunctionModuleByName(Project project, String name) {
        final Module[] modules = listFunctionModules(project);
        return Arrays.stream(modules)
                .filter(module -> StringUtils.equals(name, module.getName()))
                .findFirst().orElse(null);
    }

    private static boolean isModuleInTestScope(Module module) {
        if (module == null) {
            return false;
        }
        CompilerModuleExtension cme = CompilerModuleExtension.getInstance(module);
        if (cme == null) {
            return false;
        }
        return cme.getCompilerOutputUrl() == null && cme.getCompilerOutputUrlForTests() != null;
    }


    public static int findFreePortForApi(int startPort) {
        ServerSocket socket = null;
        for (int port = startPort; port <= MAX_PORT; port++) {
            try {
                socket = new ServerSocket(port);
                return socket.getLocalPort();
            } catch (IOException e) {
                // swallow this exception
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // swallow this exception
                    }
                }
            }
        }
        return -1;
    }

    public static File getTempStagingFolder() {
        return Files.createTempDir();
    }

    @AzureOperation(
            name = "function.clean_staging_folder",
            params = {"stagingFolder.getName()"},
            type = AzureOperation.Type.TASK
    )
    public static void cleanUpStagingFolder(File stagingFolder) {
        try {
            if (stagingFolder != null) {
                FileUtils.deleteDirectory(stagingFolder);
            }
        } catch (IOException e) {
            // swallow exceptions while clean up
        }
    }

    public static Path getDefaultHostJson(Project project) {
        return Path.of(project.getBasePath() + File.separator + "host.json");
    }

    public static List<String> getFunctionBindingList(Map<String, FunctionConfiguration> configMap) {
        return configMap.values().stream().flatMap(configuration -> configuration.getBindings().stream())
                .map(Binding::getType)
                .sorted()
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<String> findFunctionsByAnnotation(Project project) {

        List<String> functionNames = new ArrayList<>();

        ApplicationManager.getApplication().invokeAndWait(() -> {
            PsiDirectory directory = PsiDirectoryFactory.getInstance(project).createDirectory(project.getBaseDir());
            for (PsiDirectory subDir : directory.getSubdirectories()) {
                List<String> missingFiles = new ArrayList<>();
                for (String funcFileName : Constants.UPLOAD_FUNC_FILES) {
                    if (subDir.findFile(funcFileName) == null) {
                        missingFiles.add(funcFileName);
                    }
                }
                if (missingFiles.size() == 0) {
                    functionNames.add(subDir.getName());
                }
            }
        });

        return functionNames;
    }

    private static Map<String, FunctionConfiguration> generateConfigurations(Project project, final List<String> functions)
            throws AzureExecutionException {
        final Map<String, FunctionConfiguration> configMap = new HashMap<>();
        for (final String functionName : functions) {

            configMap.put(functionName, generateConfiguration(project, functionName));
        }
        return configMap;
    }

    private static FunctionConfiguration generateConfiguration(Project project, String functionName) throws AzureExecutionException {
        final FunctionConfiguration config = new FunctionConfiguration();
        final List<Binding> bindings = new ArrayList<>();
        JsonObject model = new JsonObject();
        try {
             model = JsonFileManipulator.retrieveFunctionJson(project.getBasePath() + File.separator + functionName);
        }
        catch (Exception ex) {
            model.add("bindings", new JsonArray());
            //TODO: Handle this
        }
//        processParameterAnnotations(method, bindings);
//        processMethodAnnotations(method, bindings);
//        patchStorageBinding(method, bindings);
        for(var bm : model.get("bindings").getAsJsonArray()) {
            JsonObject bindingFromJson = bm.getAsJsonObject();
            if(!bindingFromJson.has("type") || !bindingFromJson.has("direction")) {
                continue;
            }
            BindingEnum bEnum = Arrays.stream(BindingEnum.values())
                    .filter(bindingEnum -> bindingEnum.getType().equalsIgnoreCase(bindingFromJson.get("type").getAsString())
                            && bindingFromJson.get("direction").getAsString().equalsIgnoreCase(String.valueOf(bindingEnum.getDirection())))
                    .findFirst().orElse(null);
            if(bEnum != null) {
                Binding binding = new Binding(bEnum);
                bindings.add(binding);
            }
        }
        if(model.has("entryPoint")) {
            config.setEntryPoint(model.get("entryPoint").getAsString());
        }
        // Todo: add set bindings method in tools-common
        config.setBindings(bindings);
        return config;
    }

    private static void copyFilesWithDefaultContent(Path sourcePath, File dest, String defaultContent)
            throws IOException {
        final File src = sourcePath == null ? null : sourcePath.toFile();
        if (src != null && src.exists()) {
            FileUtils.copyFile(src, dest);
        } else {
            FileUtils.write(dest, defaultContent, Charset.defaultCharset());
        }
    }

    public static Map<String, FunctionConfiguration> prepareStagingFolder(Path stagingFolder, Path hostJson, Project project, List<String> methods)
            throws AzureExecutionException, IOException {
        final Map<String, FunctionConfiguration> configMap = generateConfigurations(project, methods);

        ApplicationManager.getApplication().invokeAndWait(() -> {
            try {
                if (stagingFolder.toFile().isDirectory()) {
                    FileUtils.cleanDirectory(stagingFolder.toFile());
                }

                for (String fileName : Constants.UPLOAD_APP_FILES) {
                    final File originalFile = new File(project.getBasePath() + File.separator + fileName);
                    final File newFile = new File(stagingFolder.toFile(), fileName);
                    if (fileName.equalsIgnoreCase("host.json")) {
                        //TODO: Raise an exception rather than use default
                        copyFilesWithDefaultContent(originalFile.toPath(), newFile, DEFAULT_HOST_JSON);
                    } else if (!fileName.equalsIgnoreCase("local.settings.json")) {
                        //TODO: Properly fix this
                        FileUtils.copyFileToDirectory(originalFile, stagingFolder.toFile());
                    }
                }

                PsiDirectory directory = PsiDirectoryFactory.getInstance(project).createDirectory(project.getBaseDir());
                for (String methodName : methods) {
                    File tempMethodLoc = new File(stagingFolder + File.separator + methodName);

                    for (String fileName : Constants.UPLOAD_FUNC_FILES) {
                        File originalFile = new File(project.getBasePath() + File.separator + methodName + File.separator + fileName);
                        if (!originalFile.exists()) {
                            throw new AzureException(String.format("Could not find required file %s in function named %s", fileName, methodName));
                        }
                    }
                    JsonObject functionJson = JsonFileManipulator.retrieveFunctionJson(project.getBasePath() + File.separator + methodName);
                    if(!functionJson.has("scriptFile")) {
                        continue;
                    }
                    File entryPointFile = new File(project.getBasePath() + File.separator + methodName + File.separator + functionJson.get("scriptFile").getAsString());
                    if (!entryPointFile.exists()) {
                        throw new AzureException(String.format("Function %s has no entry point", methodName));
                    }
                    PsiDirectory subDir = Arrays.stream(directory.getSubdirectories()).filter(dir -> dir.getName().equalsIgnoreCase(methodName)).findFirst().orElse(null);
                    if (subDir == null) {
                        throw new AzureException(String.format("Could not find function with name %s", methodName));
                    }
                    copyFilesInDirectoryToLocation(subDir, tempMethodLoc);
                }
            }
            catch (Exception ex) {
                //TODO: Swallowing error for now, please print this somewhere
            }
        });
        return configMap;
    }

    private static void copyFilesInDirectoryToLocation(PsiDirectory directory, File newLocation) throws IOException {

        for(PsiFile anyFile : directory.getFiles()) {
            FileUtils.copyFileToDirectory(new File(anyFile.getVirtualFile().getPath()), newLocation);
        }
        for(PsiDirectory subDir : directory.getSubdirectories()) {
            copyFilesInDirectoryToLocation(subDir, new File(newLocation.getPath() + File.separator + subDir.getName()));
        }
    }

    public static String[] getVENVActivateCommand(Project project) {
        String[] venvName = null;
        PsiDirectory basePSI = PsiDirectoryFactory.getInstance(project).createDirectory(project.getBaseDir());
        for(PsiDirectory subDir : basePSI.getSubdirectories()) {
            if(subDir.getName().contains("venv") && Arrays.stream(subDir.getFiles())
                    .filter(x -> x.getName().equals("pyvenv.cfg")).findFirst().orElse(null) != null) {
                venvName = new String[] {subDir.getName()};
            }
        }
        if(venvName != null) {
            venvName[0] = project.getBasePath() + File.separator + venvName[0] + File.separator + "Scripts" + File.separator + "activate";
            if(CommandUtils.isWindows()) {
                venvName = new String[] {venvName[0] + ".bat"};
            }
            else {
                venvName = new String[] { "source", venvName[0]};
            }

        }
        return venvName;
    }

    public static String[] commandWithVENV(String[] commandToJoin, Project project) {
        String[] activateCommand = FunctionUtils.getVENVActivateCommand(project);

        String shellName = CommandUtils.isWindows() ? "cmd" : "bash";

        String commandJoiner = CommandUtils.isWindows() ? "&&" : ";";

        String switcher = CommandUtils.isWindows() ? "/c" : "-c";

        List<String> commandList = new ArrayList<>();

        commandList.add(shellName);
        commandList.add(switcher);
        commandList.addAll(Arrays.asList(activateCommand));
        commandList.add(commandJoiner);
        commandList.addAll(Arrays.asList(commandToJoin));

        String[] command = commandList.toArray(new String[] {});

        return command;
    }
}
