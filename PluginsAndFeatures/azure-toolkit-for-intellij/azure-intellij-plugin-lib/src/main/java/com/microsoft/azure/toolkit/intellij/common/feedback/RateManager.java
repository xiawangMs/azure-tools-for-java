/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common.feedback;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.registry.Registry;
import com.microsoft.azure.toolkit.ide.common.store.AzureStoreManager;
import com.microsoft.azure.toolkit.ide.common.store.IIdeStore;
import com.microsoft.azure.toolkit.lib.common.operation.Operation;
import com.microsoft.azure.toolkit.lib.common.operation.OperationListener;
import com.microsoft.azure.toolkit.lib.common.operation.OperationManager;
import com.microsoft.azure.toolkit.lib.common.utils.InstallationIdUtils;
import lombok.Data;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class RateManager {
    public static final String SERVICE = "rate";
    public static final String RATED_AT = "rated_at";
    public static final String RATED_SCORE = "rated_score";
    public static final String POPPED_AT = "popped_at";
    public static final String POPPED_TIMES = "popped_times";
    public static final String NEXT_POP_AFTER = "next_pop_after";
    public static final String TOTAL_SCORE = "action_total_score";
    public static final String NEXT_REWIND_DATE = "next_rewind_date";

    private static final String SCORES_YML = "/com/microsoft/azure/toolkit/intellij/common/feedback/action-scores.yml";
    private final Map<String, ScoreConfig> scores;
    private final AtomicInteger score = new AtomicInteger(0);

    private RateManager() {
        scores = loadScores();
        final IIdeStore store = AzureStoreManager.getInstance().getIdeStore();
        final int totalScore = Integer.parseInt(Objects.requireNonNull(store.getProperty(SERVICE, TOTAL_SCORE, "0")));
        score.set(totalScore);
    }

    public static RateManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static Map<String, ScoreConfig> loadScores() {
        final ObjectMapper YML_MAPPER = new YAMLMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try (final InputStream inputStream = RateManager.class.getResourceAsStream(SCORES_YML)) {
            return YML_MAPPER.readValue(inputStream, new TypeReference<>() {
            });
        } catch (final IOException ignored) {
        }
        return Collections.emptyMap();
    }

    public void addScore(int score) {
        final int total = this.score.addAndGet(score);
        final int threshold = Registry.intValue("azure.toolkit.feedback.score.threshold", 15);
        if (total > threshold && RatePopup.tryPopup(null)) {
            this.score.set(threshold / 2);
        }
        this.persistScore();
    }

    public synchronized int getScore() {
        return score.get();
    }

    private void persistScore() {
        final IIdeStore store = AzureStoreManager.getInstance().getIdeStore();
        store.setProperty(SERVICE, TOTAL_SCORE, "" + score.get());
    }

    public static class WhenToPopup implements StartupActivity, OperationListener {
        @Override
        public void runActivity(@Nonnull Project project) {
            final char c = InstallationIdUtils.getHashMac().toLowerCase().charAt(0);
            final boolean testMode = Registry.is("azure.toolkit.test.mode.enabled", false);
            final IIdeStore store = AzureStoreManager.getInstance().getIdeStore();
            final String nextPopAfter = store.getProperty(SERVICE, NEXT_POP_AFTER);
            if (testMode || (!StringUtils.equals(nextPopAfter, "-1") && Character.digit(c, 16) % 4 == 0)) { // enabled for only 1/4
                final String nextRewindDate = store.getProperty(SERVICE, NEXT_REWIND_DATE);
                if (StringUtils.isBlank(nextRewindDate)) {
                    store.setProperty(SERVICE, NEXT_REWIND_DATE, String.valueOf(System.currentTimeMillis() + 14 * DateUtils.MILLIS_PER_DAY));
                } else if (Long.parseLong(nextRewindDate) > System.currentTimeMillis()) {
                    final int totalScore = Integer.parseInt(Objects.requireNonNull(store.getProperty(SERVICE, TOTAL_SCORE, "0")));
                    store.setProperty(SERVICE, TOTAL_SCORE, totalScore / 2 + "");
                    store.setProperty(SERVICE, NEXT_REWIND_DATE, String.valueOf(System.currentTimeMillis() + 14 * DateUtils.MILLIS_PER_DAY));
                }
                OperationManager.getInstance().addListener(this);
            }
        }

        @Override
        public void afterReturning(Operation operation, Object source) {
            final RateManager manager = RateManager.getInstance();
            final ScoreConfig config = manager.scores.get(operation.getId());
            if (config != null) {
                final String actionId = Optional.ofNullable(operation.getActionParent()).map(Operation::getId).orElse(null);
                if (ArrayUtils.isEmpty(config.getActions()) || Arrays.asList(config.getActions()).contains(actionId)) {
                    manager.addScore(config.getSuccess());
                }
            }
        }

        @Override
        public void afterThrowing(Throwable e, Operation operation, Object source) {
            final RateManager manager = RateManager.getInstance();
            final ScoreConfig config = RateManager.getInstance().scores.get(operation.getId());
            if (config != null) {
                final String actionId = Optional.ofNullable(operation.getActionParent()).map(Operation::getId).orElse(null);
                if (ArrayUtils.isEmpty(config.getActions()) || Arrays.asList(config.getActions()).contains(actionId)) {
                    manager.addScore(config.getFailure());
                }
            }
        }
    }

    @Data
    public static class ScoreConfig {
        private String[] actions;
        private int success;
        private int failure;
    }

    private static class SingletonHolder {
        public static final RateManager INSTANCE = new RateManager();
    }
}
