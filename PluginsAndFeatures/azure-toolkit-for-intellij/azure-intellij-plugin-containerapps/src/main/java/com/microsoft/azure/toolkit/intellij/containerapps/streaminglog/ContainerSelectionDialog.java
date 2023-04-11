package com.microsoft.azure.toolkit.intellij.containerapps.streaminglog;

import com.azure.resourcemanager.appcontainers.models.ReplicaContainer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerApp;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.Replica;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.Revision;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ContainerSelectionDialog extends DialogWrapper {
    private JPanel rootPanel;
    private AzureComboBox<Revision> revisionComboBox;
    private AzureComboBox<Replica> replicaComboBox;
    private AzureComboBox<String> containerComboBox;
    private final ContainerApp containerApp;
    private final Project project;
    private final String GO_TO_REVISION_MANAGEMENT = "You need at least one instance to view logs. To change settings, go to Revision management.";

    public ContainerSelectionDialog(@Nullable final Project project, ContainerApp containerApp) {
        super(project, false);
        this.project = project;
        this.containerApp = containerApp;
        setTitle("Select Revision");
        init();
        initListeners();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return this.rootPanel;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        final String revisionName = Objects.requireNonNull(revisionComboBox.getValue()).getName();
        final String replicaName = Objects.requireNonNull(replicaComboBox.getValue()).getName();
        AzureTaskManager.getInstance().runInBackground("Start Streaming Log", () ->
                ContainerAppStreamingLogManager.getInstance().showConsoleStreamingLog(project, containerApp, revisionName, replicaName, containerComboBox.getValue()));
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        if (revisionComboBox.getItems().size() == 0) {
            return new ValidationInfo(GO_TO_REVISION_MANAGEMENT, revisionComboBox);
        }
        if (Objects.isNull(revisionComboBox.getValue())) {
            return new ValidationInfo("Revision is required", revisionComboBox);
        }
        if (replicaComboBox.getItems().size() == 0) {
            return new ValidationInfo(GO_TO_REVISION_MANAGEMENT, replicaComboBox);
        }
        if (Objects.isNull(replicaComboBox.getValue())) {
            return new ValidationInfo("Replica is required", replicaComboBox);
        }
        if (containerComboBox.getItems().size() == 0) {
            return new ValidationInfo(GO_TO_REVISION_MANAGEMENT, containerComboBox);
        }
        if (Objects.isNull(containerComboBox.getValue())) {
            return new ValidationInfo("Container is required", containerComboBox);
        }
        return null;
    }

    private void initListeners() {
        revisionComboBox.setItemsLoader(containerApp::getRevisionList);
        revisionComboBox.addItemListener(e -> replicaComboBox.reloadItems());
        replicaComboBox.setItemsLoader(() -> {
            final Revision revision = revisionComboBox.getValue();
            return Optional.ofNullable(revision).map(Revision::getReplicaList).orElse(Collections.emptyList());
        });
        replicaComboBox.addItemListener(e -> containerComboBox.reloadItems());
        containerComboBox.setItemsLoader(() -> {
            final Replica replica = replicaComboBox.getValue();
            return Optional.ofNullable(replica)
                    .map(r -> r.getContainerList().stream().map(ReplicaContainer::name).collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
        });
    }

    private void createUIComponents() {
        this.revisionComboBox = new AzureComboBox<>() {
            @Override
            protected String getItemText(Object item) {
                if (item == null) {
                    return StringUtils.EMPTY;
                }
                return ((Revision) item).getName();
            }
        };
        this.replicaComboBox = new AzureComboBox<>() {
            @Override
            protected String getItemText(Object item) {
                if (item == null) {
                    return StringUtils.EMPTY;
                }
                return ((Replica) item).getName();
            }
        };
    }
}
