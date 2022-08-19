package com.microsoft.azure.toolkit.intellij.cosmos.connection;

import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.cosmos.CosmosDBAccount;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class CosmosDBAccountComboBox<T extends CosmosDBAccount> extends AzureComboBox<T> {

    @Getter
    private Subscription subscription;

    public CosmosDBAccountComboBox(@Nonnull Function<Subscription, ? extends List<? extends T>> itemsLoader) {
        super();
        this.setItemsLoader(() -> Objects.isNull(subscription) ? Collections.emptyList() : itemsLoader.apply(this.getSubscription()));
    }

    public void setSubscription(Subscription subscription) {
        if (Objects.equals(subscription, this.subscription)) {
            return;
        }
        this.subscription = subscription;
        if (subscription == null) {
            this.clear();
            return;
        }
        this.refreshItems();
    }

    @Override
    protected String getItemText(Object item) {
        return Objects.nonNull(item) && item instanceof CosmosDBAccount ? ((CosmosDBAccount) item).getName() : super.getItemText(item);
    }

    @Override
    public boolean isRequired() {
        return true;
    }
}
