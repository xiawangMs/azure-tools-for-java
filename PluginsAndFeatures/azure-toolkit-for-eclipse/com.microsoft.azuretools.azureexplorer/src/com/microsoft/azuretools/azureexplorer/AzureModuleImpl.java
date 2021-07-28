/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer;

import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.azurecommons.helpers.AzureCmdException;
import com.microsoft.azuretools.core.handlers.SelectSubsriptionsCommandHandler;
import com.microsoft.azuretools.core.handlers.SignInCommandHandler;
import com.microsoft.azuretools.core.handlers.SignOutCommandHandler;
import com.microsoft.azuretools.core.utils.PluginUtil;
import com.microsoft.tooling.msservices.serviceexplorer.NodeAction;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureModule;

public class AzureModuleImpl extends AzureModule {

    public AzureModuleImpl() {
        super(null);
    }

    @Override
    protected void loadActions() {
        super.loadActions();
        addAction(new SignInOutAction(this));
        addAction(new SelectSubscriptionsAction(this));
    }

    public static class SignInOutAction extends NodeAction {
        private static final String ICON_SIGNIN = "SignInLight_16.png";
        private static final String ICON_SIGNOUT = "SignOutLight_16.png";
        private static final String SIGN_IN = "Sign In";
        private static final String SIGN_OUT = "Sign Out";

        SignInOutAction(AzureModule azureModule) {
            super(azureModule, "Sign In/Out");
            addListener(new NodeActionListener() {
                @Override
                protected void actionPerformed(NodeActionEvent e) throws AzureCmdException {
                    try {
                        AuthMethodManager authMethodManager = AuthMethodManager.getInstance();
                        boolean isSignedIn = authMethodManager.isSignedIn();
                        if (isSignedIn) {
                            SignOutCommandHandler.doSignOut(PluginUtil.getParentShell());
                        } else {
                            SignInCommandHandler.doSignIn(PluginUtil.getParentShell());
                        }
                    } catch (Exception ex) {
                        Activator.getDefault().log(ex.getMessage(), ex);
                    }
                }
            });
        }

        @Override
        public String getName() {
            try {
                return AuthMethodManager.getInstance().isSignedIn() ? "Sign Out" : "Sign In";
            } catch (Exception ex) {
                Activator.getDefault().log(ex.getMessage(), ex);
                return "";
            }
        }

        @Override
        public String getIconPath() {
            return getIcon();
        }

        public static String getIcon() {
            boolean isSignedIn = false;
            try {
                isSignedIn = AuthMethodManager.getInstance().isSignedIn();
            } catch (Exception ex) {}
            return isSignedIn ? ICON_SIGNOUT : ICON_SIGNIN;
        }
    }

    public static class SelectSubscriptionsAction extends NodeAction {
        private static final String ICON = "ConnectAccountsLight_16.png";

        public SelectSubscriptionsAction(AzureModule azureModule) {
            super(azureModule, "Select Subscriptions");
            setIconPath(ICON);
            addListener(new NodeActionListener() {
                @Override
                protected void actionPerformed(NodeActionEvent e) throws AzureCmdException {
                    SelectSubsriptionsCommandHandler.onSelectSubscriptions(PluginUtil.getParentShell());
                    azureModule.load(false);
                }
            });
        }

        @Override
        public boolean isEnabled() {
            try {
                return super.isEnabled() && AuthMethodManager.getInstance().isSignedIn();
            } catch (Exception ex) {
                Activator.getDefault().log(ex.getMessage(), ex);
                return false;
            }
        }
    }
}
