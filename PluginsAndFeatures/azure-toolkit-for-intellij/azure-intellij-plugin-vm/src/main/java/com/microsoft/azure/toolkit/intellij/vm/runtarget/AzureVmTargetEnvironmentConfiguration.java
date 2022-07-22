/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.vm.runtarget;

import com.intellij.openapi.project.Project;
import com.intellij.ssh.config.unified.SshConfig;
import com.jetbrains.plugins.remotesdk.target.ssh.target.SshTargetEnvironmentConfiguration;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.annotation.Nullable;

public class AzureVmTargetEnvironmentConfiguration extends SshTargetEnvironmentConfiguration {

    public AzureVmTargetEnvironmentConfiguration() {
        super();
        this.init();
    }

    @SneakyThrows
    private void init() {
        FieldUtils.writeField(this, "typeId", AzureVmTargetType.TYPE_ID, true);
    }

    @Nullable
    @Override
    public SshConfig findSshConfig(@Nullable Project project) {
        final String caller = StackWalker.getInstance().
            walk(stream -> stream.skip(1).findFirst().get()).
            getMethodName();
        // Display name of this Run Target would be reset to the name of the associated SSHConfig
        // (com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetWizardModel.saveTargetName)
        // when creating this Run Target, this is not the excepted.
        // return null if called from `saveTargetName` to prevent resetting display name of this run target
        // `getDisplayName`/`setDisplayName` and `saveTargetName` are all `final`
        if (StringUtils.containsIgnoreCase(caller, "saveTargetName")) {
            return null;
        }
        return super.findSshConfig(project);
    }
}
