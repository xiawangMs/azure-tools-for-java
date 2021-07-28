/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.mvp.model.webapp;

import com.microsoft.azure.management.appservice.CsmPublishingProfileOptions;
import com.microsoft.azure.management.appservice.PublishingProfileFormat;
import com.microsoft.azure.management.appservice.WebAppBase;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

public class AppServiceUtils {
    public static boolean getPublishingProfileXmlWithSecrets(WebAppBase webAppBase, String filePath) throws IOException {
        final File file = new File(Paths.get(filePath, String.format("%s_%s.PublishSettings",
                webAppBase.name(), System.currentTimeMillis())).toString());
        file.createNewFile();
        try (InputStream inputStream = webAppBase.manager().inner().webApps()
                .listPublishingProfileXmlWithSecrets(webAppBase.resourceGroupName(), webAppBase.name(),
                        new CsmPublishingProfileOptions().withFormat(PublishingProfileFormat.FTP));
             OutputStream outputStream = new FileOutputStream(file);
        ) {
            IOUtils.copy(inputStream, outputStream);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
