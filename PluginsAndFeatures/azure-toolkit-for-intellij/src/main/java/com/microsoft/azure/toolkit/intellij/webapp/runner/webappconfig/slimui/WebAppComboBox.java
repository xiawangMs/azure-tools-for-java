/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.webapp.runner.webappconfig.slimui;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.appservice.webapp.model.WebAppConfig;
import com.microsoft.azure.toolkit.intellij.webapp.WebAppCreationDialog;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.AzureWebApp;
import com.microsoft.azure.toolkit.lib.appservice.service.IWebApp;
import com.microsoft.tooling.msservices.components.DefaultLoader;

import java.util.List;
import java.util.stream.Collectors;

public class WebAppComboBox extends AppServiceComboBox<WebAppConfig> {
    public WebAppComboBox(Project project) {
        super(project);
    }

    @Override
    protected List<WebAppConfig> loadAppServiceModels() {
        final List<IWebApp> webApps = Azure.az(AzureWebApp.class).list();
        return webApps.stream().parallel()
                .filter(this::isJavaAppService)
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .map(WebAppConfig::fromRemote)
                .collect(Collectors.toList());
    }

    @Override
    protected void createResource() {
        // todo: hide deployment part in creation dialog
        WebAppCreationDialog webAppCreationDialog = new WebAppCreationDialog(project);
        webAppCreationDialog.setDeploymentVisible(false);
        webAppCreationDialog.setOkActionListener(webAppConfig -> {
            WebAppComboBox.this.addItem(webAppConfig);
            WebAppComboBox.this.setSelectedItem(webAppConfig);
            DefaultLoader.getIdeHelper().invokeLater(webAppCreationDialog::close);
        });
        webAppCreationDialog.show();
    }
}
