/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerservice.actions;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.options.ex.ConfigurableExtensionPointUtil;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.FileChooser;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.containerservice.KubernetesCluster;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class DownloadKubuConfigAction {
    public static void downloadKubuConfig(@Nonnull KubernetesCluster cluster, @Nonnull Project project, boolean isAdmin) {
        final File destFile = AzureTaskManager.getInstance().runLaterAsObservable(new AzureTask<>(() ->
                        FileChooser.showFileSaver("Download kubernetes configuration", String.format("%s-%s.yml", cluster.getName(), isAdmin ? "admin" : "user"))))
                .toBlocking().first();
        if (destFile == null) {
            return;
        }
        try {
            final byte[] content = isAdmin ? cluster.getAdminKubeConfig() : cluster.getUserKubeConfig();
            FileUtils.writeByteArrayToFile(destFile, content);
            AzureMessager.getMessager().info(AzureString.format("Save kubernetes configuration file for %s to %s successfully.",
                    cluster.getName(), destFile.getAbsolutePath()), "Azure", getOpenKubernetesAction(project, destFile));
        } catch (final IOException e) {
            AzureMessager.getMessager().error(e);
        }
    }

    @Nullable
    private static Action<?> getOpenKubernetesAction(@Nonnull Project project, @Nonnull File file) {
        final Configurable kubernetes = ConfigurableExtensionPointUtil.getConfigurables(project, true).stream()
                .filter(configurable -> StringUtils.equals(configurable.getDisplayName(), "Kubernetes"))
                .findFirst().orElse(null);
        if (kubernetes == null) {
            return null;
        }
        final Consumer<Void> consumer = ignore -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(file.getAbsolutePath()), null);
            AzureTaskManager.getInstance().runLater(() -> ShowSettingsUtil.getInstance().showSettingsDialog(project, "Kubernetes"));
        };
        final ActionView.Builder view = new ActionView.Builder("Set kubeconfig file for project")
                .title(ignore -> AzureString.fromString("Set kubeconfig")).enabled(ignore -> true);
        final Action.Id<Void> id = Action.Id.of("kubernetes.set_kube_config");
        return new Action<>(id, consumer, view);
    }
}
