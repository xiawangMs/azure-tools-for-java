/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common.fileexplorer;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public class VirtualFileActions {
    private static final Key<String> FILE_ID = new Key<>("FILE_ID");
    private static final String FILE_EDITING = "Save File";
    private static final String SAVE_CHANGES = "Do you want to save your changes?";

    public static boolean openFileInEditor(final Consumer<? super String> onSave, VirtualFile virtualFile, FileEditorManager manager) {
        final Project project = manager.getProject();
        final FileEditor[] editors = manager.openFile(virtualFile, true, true);
        if (editors.length == 0) {
            return false;
        }
        for (final FileEditor editor : editors) {
            if (editor instanceof TextEditor) {
                final String originContent = getTextEditorContent((TextEditor) editor);
                final MessageBusConnection messageBusConnection = manager.getProject().getMessageBus().connect(editor);
                messageBusConnection.subscribe(FileEditorManagerListener.Before.FILE_EDITOR_MANAGER, new FileEditorManagerListener.Before() {
                    @Override
                    public void beforeFileClosed(FileEditorManager source, VirtualFile file) {
                        try {
                            final String content = getTextEditorContent((TextEditor) editor);
                            if (Objects.equals(file, virtualFile) && !StringUtils.equals(content, originContent)) {
                                final boolean result = AzureMessager.getMessager().confirm(SAVE_CHANGES, FILE_EDITING);
                                if (result) {
                                    onSave.accept(content);
                                }
                            }
                        } catch (final RuntimeException e) {
                            AzureMessager.getMessager().error(e);
                        } finally {
                            messageBusConnection.disconnect();
                        }
                    }
                });

            }
        }
        return true;
    }

    public static VirtualFile getOrCreateVirtualFile(String fileId, String name, FileEditorManager manager) {
        return Arrays.stream(manager.getOpenFiles())
            .filter(f -> StringUtils.equals(f.getUserData(FILE_ID), fileId))
            .findFirst().orElse(createVirtualFile(fileId, name, manager));
    }

    @SneakyThrows
    private static LightVirtualFile createVirtualFile(final String fileId, final String fullName, FileEditorManager manager) {
        final LightVirtualFile virtualFile = new LightVirtualFile(fullName) {
            @Override
            public VirtualFile getParent() {
                return VirtualFileManager.getInstance().findFileByNioPath(FileUtils.getTempDirectory().toPath());
            }
        };
        final FileType type = FileTypeManager.getInstance().getFileTypeByFileName(fullName);
        virtualFile.setFileType(type == FileTypes.UNKNOWN ? FileTypes.PLAIN_TEXT : type);
        virtualFile.setCharset(StandardCharsets.UTF_8);
        virtualFile.putUserData(FILE_ID, fileId);
        virtualFile.setWritable(true);
        return virtualFile;
    }

    private static String getTextEditorContent(TextEditor textEditor) {
        return textEditor.getEditor().getDocument().getText();
    }
}
