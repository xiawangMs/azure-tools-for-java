package com.microsoft.azure.toolkit.ide.guidance.input;

import com.microsoft.azure.toolkit.ide.guidance.ComponentContext;
import com.microsoft.azure.toolkit.ide.guidance.config.InputConfig;

import javax.annotation.Nonnull;

public class FileChooserInput implements GuidanceInput<String> {
    public static final String DIRECTORY = "directory";
    public static final String FILE_CHOOSER = "input.common.file-chooser";
    public static final String VALUE = "value";

    private final FileChooserInputPanel inputPanel;
    private final ComponentContext context;
    private final InputConfig config;

    public FileChooserInput(@Nonnull InputConfig config, @Nonnull ComponentContext inputContext) {
        this.context = inputContext;
        this.config = config;
        this.inputPanel = new FileChooserInputPanel();

        this.inputPanel.setValue((String) context.getParameter(VALUE));
        this.context.addPropertyListener(VALUE, value -> inputPanel.setValue((String) value));
    }

    @Override
    public String getDescription() {
        return config.getDescription();
    }

    @Override
    public FileChooserInputPanel getComponent() {
        return inputPanel;
    }

    @Override
    public void applyResult() {
        context.applyResult(DIRECTORY, inputPanel.getValue());
    }
}
