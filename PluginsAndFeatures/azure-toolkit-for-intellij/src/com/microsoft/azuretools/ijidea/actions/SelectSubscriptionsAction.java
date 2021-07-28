/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.ijidea.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.authmanage.SubscriptionManager;
import com.microsoft.azuretools.authmanage.models.SubscriptionDetail;
import com.microsoft.azuretools.ijidea.ui.ErrorWindow;
import com.microsoft.azuretools.ijidea.ui.SubscriptionsDialog;
import com.microsoft.azuretools.ijidea.utility.AzureAnAction;
import com.microsoft.azuretools.sdkmanage.AzureManager;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import com.microsoft.azuretools.telemetrywrapper.Operation;
import com.microsoft.intellij.helpers.UIHelperImpl;
import com.microsoft.intellij.serviceexplorer.azure.ManageSubscriptionsAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class SelectSubscriptionsAction extends AzureAnAction {
    private static final Logger LOGGER = Logger.getInstance(SelectSubscriptionsAction.class);

    public SelectSubscriptionsAction() {
    }

    @Override
    public boolean onActionPerformed(@NotNull AnActionEvent e, @Nullable Operation operation) {
        Project project = DataKeys.PROJECT.getData(e.getDataContext());
        onShowSubscriptions(project);
        return true;
    }

    public static void onShowSubscriptions(Project project) {
        JFrame frame = WindowManager.getInstance().getFrame(project);

        EventUtil.executeWithLog(TelemetryConstants.ACCOUNT, TelemetryConstants.GET_SUBSCRIPTIONS, (operation) -> {
            //Project project = ProjectManager.getInstance().getDefaultProject();();

            AzureManager manager = AuthMethodManager.getInstance().getAzureManager();
            if (manager == null) {
                return;
            }

            final SubscriptionManager subscriptionManager = manager.getSubscriptionManager();

            updateSubscriptionWithProgressDialog(subscriptionManager, project);
            List<SubscriptionDetail> sdl = subscriptionManager.getSubscriptionDetails();
            for (SubscriptionDetail sd : sdl) {
                System.out.println(sd.getSubscriptionName());
            }

            //System.out.println("onShowSubscriptions: calling getSubscriptionDetails()");
            SubscriptionsDialog d = SubscriptionsDialog.go(subscriptionManager.getSubscriptionDetails(), project);
            List<SubscriptionDetail> subscriptionDetailsUpdated;
            if (d != null) {
                subscriptionDetailsUpdated = d.getSubscriptionDetails();
                subscriptionManager.setSubscriptionDetails(subscriptionDetailsUpdated);
            }
        }, (ex) -> {
            ex.printStackTrace();
            //LOGGER.error("onShowSubscriptions", ex);
            ErrorWindow.show(project, ex.getMessage(), "Select Subscriptions Action Error");
        });
    }

    @Override
    public void update(AnActionEvent e) {
        try {
            boolean isSignIn = AuthMethodManager.getInstance().isSignedIn();
            e.getPresentation().setEnabled(isSignIn);
            e.getPresentation().setIcon(UIHelperImpl.loadIcon(ManageSubscriptionsAction.getIcon()));
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.error("update", ex);
        }
    }

    public static void updateSubscriptionWithProgressDialog(final SubscriptionManager subscriptionManager, Project project) {
        ProgressManager.getInstance().run(new Task.Modal(project, "Loading Subscriptions...", true) {
            @Override
            public void run(ProgressIndicator progressIndicator) {
                try {
                    progressIndicator.setIndeterminate(true);
                    //System.out.println("updateSubscriptionWithProgressDialog: calling getSubscriptionDetails()");
                    subscriptionManager.getSubscriptionDetails();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    // Don't handle subscription exception there.
                    // LOGGER.error("updateSubscriptionWithProgressDialog", ex);
                }
            }
        });
    }
}
