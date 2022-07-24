package com.microsoft.azure.toolkit.intellij.vm.runtarget;

import com.intellij.execution.target.TargetEnvironmentWizard;
import com.intellij.ide.wizard.AbstractWizardStepEx;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.ui.ValidationInfo;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.ConnectionData;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetAuthStep;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetConnectionStep;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetStepBase;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetWizardModel;
import com.microsoft.azure.toolkit.lib.compute.virtualmachine.VirtualMachine;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @see com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetConnectionStep
 */
@Getter
public class AzureVmTargetConnectionStep extends SshTargetStepBase implements TargetEnvironmentWizard.ValidationCallbackConsumer {
    private final Object previousStepId = null;
    private final Object stepId = SshTargetConnectionStep.getID();
    private final Object nextStepId = SshTargetAuthStep.getID();
    private final ArrayList<AbstractWizardStepEx> steps;
    private VirtualMachineConnectionConfigurable configurable;

    public AzureVmTargetConnectionStep(@Nonnull SshTargetWizardModel model, @Nonnull ArrayList<AbstractWizardStepEx> steps) {
        super(model);
        this.steps = steps;
        this.configurable = new VirtualMachineConnectionConfigurable(this.getModel());
    }

    @Override
    public void _init() {
        super._init();
        this.setTitle(AzureVmTargetType.DISPLAY_NAME);
        this.setStepDescription(formatStepLabel(1, 4, "Select an existing Azure Virtual Machine instance."));
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    protected void doCommit(CommitType commitType) throws CommitStepException {
        if (commitType == CommitType.Next) {
            final VirtualMachine vm = this.configurable.apply();
            this.steps.forEach(s -> setTitle(s, vm.getName()));
        }
    }

    @SneakyThrows
    private Object setTitle(AbstractWizardStepEx step, String title) {
        return MethodUtils.invokeMethod(step, true, "setTitle", title);
    }

    @Nonnull
    @Override
    protected JComponent createMainPanel() {
        return configurable.getContentPanel();
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.configurable.getPreferredFocusedComponent();
    }

    @Nonnull
    @Override
    public List<ValidationInfo> doValidateAll() {
        return Collections.emptyList();
    }

    @Override
    public void accept(Function0<? extends Unit> function0) {

    }
}
