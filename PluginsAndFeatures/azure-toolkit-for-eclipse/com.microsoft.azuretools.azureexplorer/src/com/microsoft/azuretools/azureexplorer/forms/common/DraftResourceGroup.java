package com.microsoft.azuretools.azureexplorer.forms.common;


import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import lombok.Getter;
import lombok.Setter;

public class DraftResourceGroup extends ResourceGroup implements Draft {
    @Getter
    @Setter
    private Subscription subscription;

    public DraftResourceGroup(Subscription s, String name) {
        super(builder().subscriptionId(s.getId()).name(name));
        this.subscription = s;
    }
}
