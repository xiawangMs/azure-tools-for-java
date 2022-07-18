package com.microsoft.azure.toolkit.intellij.containerservice.creation;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.AzureDialog;
import com.microsoft.azure.toolkit.intellij.common.AzureTextInput;
import com.microsoft.azure.toolkit.intellij.common.component.RegionComboBox;
import com.microsoft.azure.toolkit.intellij.common.component.SubscriptionComboBox;
import com.microsoft.azure.toolkit.intellij.common.component.resourcegroup.ResourceGroupComboBox;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.containerservice.KubernetesClusterDraft;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.List;

public class KubernetesCreationDialog extends AzureDialog<KubernetesClusterDraft.Config> implements AzureForm<KubernetesClusterDraft.Config>  {
    private JPanel pnlRoot;
    private JLabel lblSubscription;
    private SubscriptionComboBox cbSubscription;
    private JLabel lblResourceGroup;
    private ResourceGroupComboBox cbResourceGroup;
    private JComboBox cbKubernetesVersion;
    private RegionComboBox cbRegion;
    private AzureTextInput txtName;
    private JComboBox cbNodeSize;
    private JRadioButton manualRadioButton;
    private JRadioButton autoScaleRadioButton;
    private AzureTextInput txtDnsPrefix;
    private JLabel lblDnsNamePrefix;
    private AzureTextInput txtMaxNodeCount;
    private JLabel lblMaxNodeCount;
    private AzureTextInput txtMinNodeCount;
    private JLabel lblMinNodeCount;
    private JLabel lblNodeCount;
    private AzureTextInput txtNodeCount;
    private JLabel lblScaleMethod;
    private JLabel lblNodeSize;
    private JLabel lblKubernetesVersion;
    private JLabel lblRegion;
    private JLabel lblName;

    private Project project;

    public KubernetesCreationDialog(@Nullable Project project){
        super(project);
        this.project = project;
        $$$setupUI$$$();
        init();
    }

    @Override
    public AzureForm<KubernetesClusterDraft.Config> getForm() {
        return this;
    }

    @Override
    protected String getDialogTitle() {
        return "Create Kubernetes Service";
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return pnlRoot;
    }

    @Override
    public KubernetesClusterDraft.Config getValue() {
        return null;
    }

    @Override
    public void setValue(KubernetesClusterDraft.Config data) {

    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return null;
    }

    @Override
    protected void init() {
        super.init();
        this.cbSubscription.setRequired(true);
        this.cbResourceGroup.setRequired(true);
        this.cbRegion.setRequired(true);
        this.txtName.setRequired(true);
        // todo: add validator for k8s name
        this.cbSubscription.addItemListener(this::onSubscriptionChanged);
    }

    private void onSubscriptionChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Subscription) {
            final Subscription subscription = (Subscription) e.getItem();
            this.cbResourceGroup.setSubscription(subscription);
            this.cbRegion.setSubscription(subscription);
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here

    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    private void $$$setupUI$$$() {
    }
}
