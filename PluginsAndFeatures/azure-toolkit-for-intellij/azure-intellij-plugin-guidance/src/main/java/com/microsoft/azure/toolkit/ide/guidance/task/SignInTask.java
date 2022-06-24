package com.microsoft.azure.toolkit.ide.guidance.task;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.microsoft.azure.toolkit.ide.guidance.ComponentContext;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceTask;
import com.microsoft.azure.toolkit.intellij.common.action.IntellijAccountActionsContributor;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azuretools.authmanage.models.AuthMethodDetails;
import com.microsoft.azuretools.sdkmanage.IdentityAzureManager;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.Objects;

import static com.microsoft.azure.toolkit.lib.auth.model.AuthType.AZURE_CLI;

public class SignInTask implements GuidanceTask {

    public static final String SUBSCRIPTION_ID = "subscriptionId";
    public static final String AZURE_EXPLORER_ID = "Azure Explorer";
    private final ComponentContext taskContext;

    public SignInTask(@Nonnull final ComponentContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public void execute() {
        final AzureAccount az = Azure.az(AzureAccount.class);
        if (az.isSignedIn()) {
            // if already signed in, finish directly
            AzureMessager.getMessager().info(AzureString.format("Sign in successfully"));
            return;
        }
        final AuthMethodDetails methodDetails;
        if (isAzureCliAuthenticated()) {
            AzureMessager.getMessager().info("Signing in with Azure Cli...");
            methodDetails = IdentityAzureManager.getInstance().signInAzureCli().block();
        } else {
            AzureMessager.getMessager().info("Signing in with OAuth...");
            methodDetails = IdentityAzureManager.getInstance().signInOAuth().block();
        }
        if (!az.isSignedIn() || CollectionUtils.isEmpty(az.getSubscriptions())) {
            final Action<Object> signInAction = AzureActionManager.getInstance().getAction(Action.AUTHENTICATE);
            final Action<Object> tryAzureAction = AzureActionManager.getInstance().getAction(IntellijAccountActionsContributor.TRY_AZURE);
            throw new AzureToolkitRuntimeException("Failed to sign in or there is no subscription in your account", signInAction, tryAzureAction);
        } else {
            AzureMessager.getMessager().info(AzureString.format("Sign in successfully with %s", Objects.requireNonNull(methodDetails).getAccountEmail()));
        }
        openAzureExplorer();
    }

    private void openAzureExplorer() {
        final ToolWindow toolWindow = ToolWindowManager.getInstance(taskContext.getProject()).getToolWindow(AZURE_EXPLORER_ID);
        if (Objects.nonNull(toolWindow) && !toolWindow.isVisible()) {
            AzureTaskManager.getInstance().runLater(toolWindow::show);
        }
    }

    private boolean isAzureCliAuthenticated() {
        return Azure.az(AzureAccount.class).accounts().stream()
                .filter(a -> a.getAuthType() == AZURE_CLI)
                .findFirst()
                .map(account -> {
                    try {
                        return account.checkAvailable().block();
                    } catch (final Exception e) {
                        return false;
                    }
                })
                .orElse(false);
    }

    @Nonnull
    @Override
    public String getName() {
        return "task.auth.signin";
    }
}
