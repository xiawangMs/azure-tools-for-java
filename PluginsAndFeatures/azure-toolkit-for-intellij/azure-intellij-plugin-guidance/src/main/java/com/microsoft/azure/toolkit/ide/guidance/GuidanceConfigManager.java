package com.microsoft.azure.toolkit.ide.guidance;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.jr.ob.JSON;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.guidance.config.SequenceConfig;
import com.microsoft.azure.toolkit.lib.common.cache.Cacheable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GuidanceConfigManager {
    public static final String GETTING_START_CONFIGURATION_NAME = "azure-getting-started.json";

    private static final GuidanceConfigManager instance = new GuidanceConfigManager();
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
    private static final ObjectMapper JSON_MAPPER = new JsonMapper()
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static GuidanceConfigManager getInstance() {
        return instance;
    }

    @Nullable
    public SequenceConfig getProcessConfigFromWorkspace(@Nonnull Project project) {
        final File file = new File(project.getBasePath(), GETTING_START_CONFIGURATION_NAME);
        if (!file.exists()) {
            return null;
        }
        try (final InputStream inputStream = new FileInputStream(file)) {
            return JSON_MAPPER.readValue(inputStream, SequenceConfig.class);
        } catch (final IOException e) {
            return null;
        }
    }

    @Cacheable(value = "guidance/sequence")
    public List<SequenceConfig> loadSequenceConfig() {
        return Optional.of(new Reflections("guidance", Scanners.Resources))
            .map(reflections -> {
                try {
                    return reflections.getResources(Pattern.compile(".*\\.json"));
                } catch (final Exception exception) {
                    return (Set<String>) Collections.EMPTY_SET;
                }
            })
            .orElse(Collections.emptySet())
            .stream().map(uri -> GuidanceConfigManager.getSequenceConfig("/" + uri))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Nullable
    private static SequenceConfig getSequenceConfig(String uri) {
        try (final InputStream inputStream = GuidanceConfigManager.class.getResourceAsStream(uri)) {
            final SequenceConfig sequenceConfig = JSON_MAPPER.readValue(inputStream, SequenceConfig.class);
            sequenceConfig.setUri(uri);
            return sequenceConfig;
        } catch (final IOException e) {
            // swallow exception for failed convertation
            return null;
        }
    }

//    @Nullable
//    private static SequenceConfig getSequenceConfig(String uri) {
//        try (final InputStream inputStream = GuidanceConfigManager.class.getResourceAsStream(uri)) {
//            final SequenceConfig sequenceConfig = YAML_MAPPER.readValue(inputStream, SequenceConfig.class);
//            sequenceConfig.setUri(uri);
//            return sequenceConfig;
//        } catch (final IOException e) {
//            // swallow exception for failed convertation
//            return null;
//        }
//    }
}
