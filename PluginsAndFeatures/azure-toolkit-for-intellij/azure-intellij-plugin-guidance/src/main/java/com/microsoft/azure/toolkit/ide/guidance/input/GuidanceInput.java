package com.microsoft.azure.toolkit.ide.guidance.input;

import com.microsoft.azure.toolkit.intellij.common.AzureFormJPanel;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface GuidanceInput<T> {
    String getDescription();

    @Nonnull
    abstract AzureFormJPanel<T> getComponent();

    abstract void applyResult();

    default List<AzureValidationInfo> getAllValidationInfos(final boolean revalidateIfNone) {
        return this.getComponent().getInputs().stream()
                .map(input -> {
                    final AzureValidationInfo validationInfo = input.getValidationInfo(revalidateIfNone);
                    if (validationInfo.getType() == AzureValidationInfo.Type.PENDING) {
                        return input.validateValueAsync().block();
                    }
                    return validationInfo;
                })
                .filter(Objects::nonNull).collect(Collectors.toList());
    }
}
