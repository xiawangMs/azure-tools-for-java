/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.appservice.platform;

import com.intellij.openapi.ui.popup.ListItemDescriptorAdapter;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.popup.list.GroupedItemsListRenderer;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.appservice.model.Runtime;
import com.microsoft.azure.toolkit.lib.legacy.webapp.WebAppService;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class RuntimeComboBox extends AzureComboBox<Runtime> {

    private final LinkedHashMap<String, List<Runtime>> runtimeSeparatorMap;
    private List<Runtime> platformList;

    public RuntimeComboBox() {
        this(Runtime.getWebappRuntimeMap());
    }

    public RuntimeComboBox(LinkedHashMap<String, List<Runtime>> runtimeSeparatorMap) {
        this.runtimeSeparatorMap = runtimeSeparatorMap;
        setGroupRender();
    }

    public void setPlatformList(final List<Runtime> platformList) {
        this.platformList = Collections.unmodifiableList(platformList);
        this.reloadItems();
    }

    @Override
    protected String getItemText(Object item) {
        return item instanceof Runtime ? WebAppService.getInstance().getRuntimeDisplayName((Runtime) item) : super.getItemText(item);
    }

    @Nonnull
    @Override
    protected List<? extends Runtime> loadItems() {
        if (Objects.nonNull(platformList)) {
            return platformList;
        }
        return runtimeSeparatorMap.entrySet().stream()
                .flatMap(stringListEntry -> stringListEntry.getValue().stream()).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    protected List<ExtendableTextComponent.Extension> getExtensions() {
        return Collections.emptyList();
    }

    private void setGroupRender() {
        this.setRenderer(new GroupedItemsListRenderer<>(new RuntimeItemDescriptor()) {
            @Override
            protected boolean hasSeparator(Runtime value, int index) {
                return Objects.isNull(platformList) && index >= 0 && super.hasSeparator(value, index);
            }
        });
    }

    private String getSeparatorCaption(Runtime item) {
        String key = StringUtils.EMPTY;
        for (final Map.Entry<String, List<Runtime>> entry : runtimeSeparatorMap.entrySet()) {
            key = entry.getKey();
            if (entry.getValue().contains(item)) {
                break;
            }
        }
        return key;
    }

    class RuntimeItemDescriptor extends ListItemDescriptorAdapter<Runtime> {
        @Override
        public String getTextFor(Runtime value) {
            return WebAppService.getInstance().getRuntimeDisplayName(value);
        }

        @Override
        public String getCaptionAboveOf(Runtime value) {
            return getSeparatorCaption(value);
        }

        @Override
        public boolean hasSeparatorAboveOf(Runtime value) {
            return Optional.of(getSeparatorCaption(value))
                    .map(runtimeSeparatorMap::get)
                    .map(list -> Objects.equals(list.get(0), value)).orElse(false);
        }
    }
}
