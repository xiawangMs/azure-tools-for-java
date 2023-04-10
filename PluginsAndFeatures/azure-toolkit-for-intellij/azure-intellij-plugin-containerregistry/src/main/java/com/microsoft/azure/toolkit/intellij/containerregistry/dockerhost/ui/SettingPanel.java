/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerregistry.dockerhost.ui;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.ui.SimpleListCellRenderer;
import com.microsoft.azure.toolkit.intellij.container.DockerUtil;
import com.microsoft.azure.toolkit.intellij.legacy.common.AzureSettingPanel;
import com.microsoft.azure.toolkit.intellij.containerregistry.dockerhost.DockerHostRunConfiguration;
import icons.MavenIcons;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.idea.maven.project.MavenProject;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingPanel extends AzureSettingPanel<DockerHostRunConfiguration> {
    private static final String IMAGE_NAME_PREFIX = "localimage";
    private static final String DEFAULT_TAG_NAME = "latest";

    private JTextField textDockerHost;
    private JCheckBox comboTlsEnabled;
    private TextFieldWithBrowseButton dockerCertPathTextField;
    private JTextField textImageName;
    private JTextField textTagName;
    private JLabel lblArtifact;
    private JComboBox<Artifact> cbArtifact;
    private JPanel rootPanel;
    private JPanel pnlDockerCertPath;
    private TextFieldWithBrowseButton dockerFilePathTextField;
    private JLabel lblMavenProject;
    private JComboBox<MavenProject> cbMavenProject;

    /**
     * Constructor.
     */
    public SettingPanel(Project project) {
        super(project);

        dockerCertPathTextField.addActionListener(this::onDockerCertPathBrowseButtonClick);
        comboTlsEnabled.addActionListener(event -> updateComponentEnabledState());

        dockerFilePathTextField.addActionListener(e -> {
            String path = dockerFilePathTextField.getText();
            final VirtualFile file = FileChooser.chooseFile(
                new FileChooserDescriptor(
                    true /*chooseFiles*/,
                    false /*chooseFolders*/,
                    false /*chooseJars*/,
                    false /*chooseJarsAsFiles*/,
                    false /*chooseJarContents*/,
                    false /*chooseMultiple*/
                ),
                project,
                StringUtils.isEmpty(path) ? null : LocalFileSystem.getInstance().findFileByPath(path)
            );
            if (file != null) {
                dockerFilePathTextField.setText(file.getPath());
            }
        });

        cbArtifact.addActionListener(e -> artifactActionPerformed((Artifact) cbArtifact.getSelectedItem()));

        cbArtifact.setRenderer(new SimpleListCellRenderer<>() {
            @Override
            public void customize(@Nonnull JList jlist, Artifact artifact, int i, boolean b, boolean b1) {
                if (artifact != null) {
                    setIcon(artifact.getArtifactType().getIcon());
                    setText(artifact.getName());
                }
            }
        });

        cbMavenProject.addActionListener(e -> {
            MavenProject selectedMavenProject = (MavenProject) cbMavenProject.getSelectedItem();
            if (selectedMavenProject != null) {
                dockerFilePathTextField.setText(
                    DockerUtil.getDefaultDockerFilePathIfExist(selectedMavenProject.getDirectory())
                );
            }
        });

        cbMavenProject.setRenderer(new SimpleListCellRenderer<>() {
            @Override
            public void customize(@Nonnull JList list, MavenProject mavenProject, int i, boolean b, boolean b1) {
                if (mavenProject != null) {
                    setIcon(MavenIcons.MavenProject);
                    setText(mavenProject.toString());
                }
            }
        });
    }

    @Override
    @Nonnull
    public String getPanelName() {
        return "Docker Run";
    }

    @Override
    @Nonnull
    public JPanel getMainPanel() {
        return rootPanel;
    }

    @Override
    @Nonnull
    protected JComboBox<Artifact> getCbArtifact() {
        return cbArtifact;
    }

    @Override
    @Nonnull
    protected JLabel getLblArtifact() {
        return lblArtifact;
    }

    @Override
    @Nonnull
    protected JComboBox<MavenProject> getCbMavenProject() {
        return cbMavenProject;
    }

    @Nonnull
    @Override
    protected JLabel getLblMavenProject() {
        return lblMavenProject;
    }

    /**
     * Function triggered in constructing the panel.
     *
     * @param conf configuration instance
     */
    @Override
    public void resetFromConfig(@Nonnull DockerHostRunConfiguration conf) {
        if (!isMavenProject()) {
            dockerFilePathTextField.setText(DockerUtil.getDefaultDockerFilePathIfExist(getProjectBasePath()));
        }

        textDockerHost.setText(conf.getDockerHost());
        comboTlsEnabled.setSelected(conf.isTlsEnabled());
        dockerCertPathTextField.setText(conf.getDockerCertPath());
        textImageName.setText(conf.getImageName());
        textTagName.setText(conf.getTagName());
        updateComponentEnabledState();

        // load dockerFile path from existing configuration.
        if (!StringUtils.isEmpty(conf.getDockerFilePath())) {
            dockerFilePathTextField.setText(conf.getDockerFilePath());
        }

        // default value for new resources
        DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
        String date = df.format(new Date());
        if (StringUtils.isEmpty(textImageName.getText())) {
            textImageName.setText(String.format("%s-%s", IMAGE_NAME_PREFIX, date));
        }
        if (StringUtils.isEmpty(textTagName.getText())) {
            textTagName.setText(DEFAULT_TAG_NAME);
        }
        if (StringUtils.isEmpty(textDockerHost.getText())) {
            final DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
            textDockerHost.setText(config.getDockerHost().toString());
        }
    }

    /**
     * Function triggered by any content change events.
     *
     * @param conf configuration instance
     */
    @Override
    public void apply(DockerHostRunConfiguration conf) {
        conf.setDockerHost(textDockerHost.getText());
        conf.setTlsEnabled(comboTlsEnabled.isSelected());
        conf.setDockerCertPath(dockerCertPathTextField.getText());
        conf.setDockerFilePath(dockerFilePathTextField.getText());
        conf.setImageName(textImageName.getText());
        if (StringUtils.isEmpty(textTagName.getText())) {
            conf.setTagName("latest");
        } else {
            conf.setTagName(textTagName.getText());
        }

        conf.setTargetPath(getTargetPath());
        conf.setTargetName(getTargetName());
    }

    @Override
    public void disposeEditor() {
    }

    private void updateComponentEnabledState() {
        pnlDockerCertPath.setVisible(comboTlsEnabled.isSelected());
    }

    private void onDockerCertPathBrowseButtonClick(ActionEvent event) {
        String path = dockerCertPathTextField.getText();
        final VirtualFile[] files = FileChooser.chooseFiles(
            new FileChooserDescriptor(false, true, true, false, false, false),
            dockerCertPathTextField,
            null,
            StringUtils.isEmpty(path) ? null : LocalFileSystem.getInstance().findFileByPath(path));
        if (files.length > 0) {
            final StringBuilder builder = new StringBuilder();
            for (VirtualFile file : files) {
                if (builder.length() > 0) {
                    builder.append(File.pathSeparator);
                }
                builder.append(FileUtil.toSystemDependentName(file.getPath()));
            }
            path = builder.toString();
            dockerCertPathTextField.setText(path);
        }
    }
}
