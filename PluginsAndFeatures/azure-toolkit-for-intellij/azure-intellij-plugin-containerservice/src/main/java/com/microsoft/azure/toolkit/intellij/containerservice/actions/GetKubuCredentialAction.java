/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerservice.actions;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.containerservice.KubernetesCluster;
import io.kubernetes.client.util.FilePersister;
import io.kubernetes.client.util.KubeConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

public class GetKubuCredentialAction {
    public static void getKubuCredential(@Nonnull KubernetesCluster cluster, @Nonnull Project project, boolean isAdmin) {
        try {
            final File file = Files.createTempFile("azure-kubernetes", "yml").toFile();
            final byte[] content = isAdmin ? cluster.getAdminKubeConfig() : cluster.getUserKubeConfig();
            FileUtils.writeByteArrayToFile(file, content);
            mergeConfigToKubConfig(file);
        } catch (final IOException e) {
            AzureMessager.getMessager().error(e);
        }
    }

    private static void mergeConfigToKubConfig(File newConfigFile) throws IOException {
        final File configFile = Path.of(System.getProperty("user.home"), KubeConfig.KUBEDIR, KubeConfig.KUBECONFIG).toFile();
        if (!configFile.exists() || configFile.getTotalSpace() == 0) {
            FileUtils.copyFile(newConfigFile, configFile);
            AzureMessager.getMessager().info(AzureString.format("Kubeconfig has been saved to %s", configFile.getAbsolutePath()));
            return;
        }
        final KubeConfig origin = KubeConfig.loadKubeConfig(new FileReader(configFile));
        final ArrayList<Object> users = origin.getUsers();
        final ArrayList<Object> clusters = origin.getClusters();
        final ArrayList<Object> contexts = origin.getContexts();
        final KubeConfig newConfig = KubeConfig.loadKubeConfig(new FileReader(newConfigFile));
        final ArrayList<Object> newConfigUsers = newConfig.getUsers();
        final ArrayList<Object> newConfigClusters = newConfig.getClusters();
        final ArrayList<Object> newConfigContexts = newConfig.getContexts();
        for (final Object o : newConfigContexts) {
            if (o instanceof Map) {
                final Object context = ((Map<?, ?>) o).get("context");
                if (context instanceof Map) {
                    final String user = ((Map<?, ?>) context).get("user").toString();
                    if (StringUtils.startsWith(user, "clusterAdmin")) {
                        final String newName = ((Map<?, ?>) o).get("name") + "-admin";
                        ((Map) o).put("name", newName);
                        newConfig.setContext(newName);
                    }
                }
            }
        }
        final ArrayList mergedUsers = merge(users, newConfigUsers, false);
        final ArrayList mergedClusters = merge(clusters, newConfigClusters, false);
        final ArrayList mergedContexts = merge(contexts, newConfigContexts, false);
        new FilePersister(configFile)
                .save(mergedContexts, mergedClusters, mergedUsers, origin.getPreferences(), newConfig.getCurrentContext());
        AzureMessager.getMessager().info(AzureString.format("Merged %s as current context in %s", newConfig.getCurrentContext(), configFile.getAbsolutePath()));
    }

    public static ArrayList merge(ArrayList origin, ArrayList newConfig, boolean overwrite) {
        final ArrayList result = new ArrayList(origin);
        for (final Object o : newConfig) {
            if (o instanceof Map) {
                final String name = ((Map<?, ?>) o).get("name").toString();
                final Object existingObject = result.stream().filter(map -> (map instanceof Map) &&
                        StringUtils.equals(((Map<?, ?>) map).get("name").toString(), name)).findFirst().orElse(null);
                if (existingObject != null) {
                    if (overwrite) {
                        result.remove(existingObject);
                        result.add(o);
                    }
                } else {
                    result.add(o);
                }
            }
        }
        return result;
    }
}
