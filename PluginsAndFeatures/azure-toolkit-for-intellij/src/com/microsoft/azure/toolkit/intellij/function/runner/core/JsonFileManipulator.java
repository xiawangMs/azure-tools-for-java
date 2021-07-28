package com.microsoft.azure.toolkit.intellij.function.runner.core;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.microsoft.azuretools.utils.JsonUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JsonFileManipulator {

    public static ObjectMapper mapper;
    private static final String LOCAL_SETTINGS_VALUES = "Values";
    private static final String FUNCTION_JSON_BINDINGS = "bindings";

    static {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_EMPTY);
    }

    public static JsonObject retrieveLocalSettings(String projectDir) throws IOException {
        //TODO: Change this logic to use HashMap parsing, not class parsing
        try {
            File localSettingsFile = new File(projectDir + File.separator + "local.settings.json");

            final JsonObject jsonObject = JsonUtils.readJsonFile(localSettingsFile);
            if (jsonObject == null) {
                return new JsonObject();
            }
            return jsonObject;
        }
        catch (Exception ex) {
            throw new IOException("Could not retrieve local.settings.json");
        }
    }

    public static void writeLocalSettings(JsonObject newSettings, String projectDir) throws IOException {
        try {
            File localSettingsFile = new File(projectDir + File.separator + "local.settings.json");
            if(localSettingsFile.exists() && localSettingsFile.canWrite()) {
                FileOutputStream fos = new FileOutputStream(localSettingsFile, false);
                String newSettingsJson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(newSettings);
                fos.write(newSettingsJson.getBytes(StandardCharsets.UTF_8));
                fos.close();
            }
        }
        catch (Exception ex) {
            throw new IOException("Could not rewrite local.settings.json");
        }
    }

    public static JsonObject retrieveFunctionJson(String functionDir) throws IOException {
        //TODO: Change this logic to use HashMap parsing, not class parsing
        try {
            File functionJsonFile = new File(functionDir + File.separator + "function.json");

            final JsonObject jsonObject = JsonUtils.readJsonFile(functionJsonFile);
            if (jsonObject == null) {
                return new JsonObject();
            }
            return jsonObject;

        }
        catch (Exception ex) {
            throw new IOException("Could not retrieve function.json");
        }
    }

    public static void writeFunctionJson(JsonObject func, String functionDir) throws IOException {
        try {
            File functionJsonFile = new File(functionDir + File.separator + "function.json");
            if(functionJsonFile.exists() && functionJsonFile.canWrite()) {
                FileOutputStream fos = new FileOutputStream(functionJsonFile, false);
                String newSettingsJson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(func);
                fos.write(newSettingsJson.getBytes(StandardCharsets.UTF_8));
                fos.close();
            }
        }
        catch (Exception ex) {
            throw new IOException("Could not rewrite local.settings.json");
        }
    }
}
