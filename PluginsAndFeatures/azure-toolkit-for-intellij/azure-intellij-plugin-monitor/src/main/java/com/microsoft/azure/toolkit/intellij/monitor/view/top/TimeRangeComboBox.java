package com.microsoft.azure.toolkit.intellij.monitor.view.top;

import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeRangeComboBox extends AzureComboBox<String> {
    @Nonnull
    @Override
    protected List<? extends String> loadItems() {
        final List<String> timeRangeList = new ArrayList<>();
        timeRangeList.add("Last 30 minutes");
        timeRangeList.add("Last hour");
        timeRangeList.add("Last 4 hours");
        timeRangeList.add("Last 12 hours");
        timeRangeList.add("Last 24 hours");
        timeRangeList.add("Last 48 hours");
        timeRangeList.add("Last 3 days");
        timeRangeList.add("Last 7 days");
        return timeRangeList;
    }

    @Nonnull
    @Override
    protected List<ExtendableTextComponent.Extension> getExtensions() {
        return Collections.emptyList();
    }
}
