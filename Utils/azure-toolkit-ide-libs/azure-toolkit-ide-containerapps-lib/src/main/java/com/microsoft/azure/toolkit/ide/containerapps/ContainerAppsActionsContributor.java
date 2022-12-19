/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.containerapps;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcons;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerApp;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.Revision;

public class ContainerAppsActionsContributor implements IActionsContributor {
    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;

    public static final String SERVICE_ACTIONS = "actions.containerapps.service";
    public static final String ENVIRONMENT_ACTIONS = "actions.containerapps.environment";
    public static final String CONTAINER_APP_ACTIONS = "actions.containerapps.containerapp";
    public static final String REVISION_ACTIONS = "actions.containerapps.revision";

    public static final Action.Id<ContainerApp> OPEN_LATEST_REVISION_IN_BROWSER = Action.Id.of("containerapps.open_in_browser.app");
    public static final Action.Id<ContainerApp> ACTIVATE_LATEST_REVISION = Action.Id.of("containerapps.activate_latest_revision.app");
    public static final Action.Id<ContainerApp> DEACTIVATE_LATEST_REVISION = Action.Id.of("containerapps.deactivate_latest_revision.app");
    public static final Action.Id<ContainerApp> RESTART_LATEST_REVISION = Action.Id.of("containerapps.restart_latest_revision.app");
    public static final Action.Id<ContainerApp> UPDATE_IMAGE = Action.Id.of("containerapps.update_image.app");
    public static final Action.Id<ContainerApp> OPEN_LOG_STREAMS = Action.Id.of("containerapps.open_log_streams.app");
    public static final Action.Id<Revision> ACTIVATE = Action.Id.of("containerapps.activate.revision");
    public static final Action.Id<Revision> DEACTIVATE = Action.Id.of("containerapps.deactivate.revision");
    public static final Action.Id<Revision> RESTART = Action.Id.of("containerapps.restart.revision");
    public static final Action.Id<Revision> OPEN_IN_BROWSER = Action.Id.of("containerapps.open_in_browser.revision");

    @Override
    public void registerActions(AzureActionManager am) {
        new Action<>(OPEN_LATEST_REVISION_IN_BROWSER)
            .withLabel("Open In Browser")
            .withIcon(AzureIcons.Action.BROWSER.getIconPath())
            .withIdParam(AbstractAzResource::getName)
            .enableWhen(s -> s instanceof ContainerApp && ((ContainerApp) s).getFormalStatus().isConnected())
            .withHandler(s -> am.getAction(ResourceCommonActionsContributor.OPEN_URL).handle("https://" + s.getLatestRevisionFqdn()))
            .register(am);

        new Action<>(ACTIVATE_LATEST_REVISION)
            .withLabel("Activate Latest Revision")
            .withIcon(AzureIcons.Action.START.getIconPath())
            .withIdParam(AbstractAzResource::getName)
            .enableWhen(s -> s instanceof ContainerApp && ((ContainerApp) s).getFormalStatus().isConnected())
            .withHandler(ContainerApp::activate)
            .register(am);

        new Action<>(DEACTIVATE_LATEST_REVISION)
            .withLabel("Deactivate Latest Revision")
            .withIcon(AzureIcons.Action.STOP.getIconPath())
            .withIdParam(AbstractAzResource::getName)
            .enableWhen(s -> s instanceof ContainerApp && ((ContainerApp) s).getFormalStatus().isConnected())
            .withHandler(ContainerApp::deactivate)
            .register(am);

        new Action<>(RESTART_LATEST_REVISION)
            .withLabel("Restart Latest Revision")
            .withIcon(AzureIcons.Action.RESTART.getIconPath())
            .withIdParam(AbstractAzResource::getName)
            .enableWhen(s -> s instanceof ContainerApp && ((ContainerApp) s).getFormalStatus().isConnected())
            .withHandler(ContainerApp::restart)
            .register(am);

        new Action<>(UPDATE_IMAGE)
            .withLabel("Update Image")
            .withIcon(AzureIcons.Action.UPLOAD.getIconPath())
            .withIdParam(AbstractAzResource::getName)
            .enableWhen(s -> s instanceof ContainerApp && ((ContainerApp) s).getFormalStatus().isConnected())
            .register(am);

        new Action<>(OPEN_LOG_STREAMS)
            .withLabel("Open Log Streams")
            .withIcon(AzureIcons.Action.LOG.getIconPath())
            .withIdParam(AbstractAzResource::getName)
            .enableWhen(s -> s instanceof ContainerApp && ((ContainerApp) s).getFormalStatus().isConnected())
            .withHandler(s -> am.getAction(ResourceCommonActionsContributor.OPEN_URL).handle(s.getPortalUrl() + "/logstream"))
            .register(am);

        new Action<>(ACTIVATE)
            .withLabel("Activate")
            .withIcon(AzureIcons.Action.START.getIconPath())
            .withIdParam(AbstractAzResource::getName)
            .enableWhen(s -> s instanceof Revision && ((Revision) s).getFormalStatus().isConnected())
            .withHandler(Revision::activate)
            .register(am);

        new Action<>(DEACTIVATE)
            .withLabel("Deactivate")
            .withIcon(AzureIcons.Action.STOP.getIconPath())
            .withIdParam(AbstractAzResource::getName)
            .enableWhen(s -> s instanceof Revision && ((Revision) s).getFormalStatus().isConnected())
            .withHandler(Revision::deactivate)
            .register(am);

        new Action<>(RESTART)
            .withLabel("Restart")
            .withIcon(AzureIcons.Action.RESTART.getIconPath())
            .withIdParam(AbstractAzResource::getName)
            .enableWhen(s -> s instanceof Revision && ((Revision) s).getFormalStatus().isConnected())
            .withHandler(Revision::restart)
            .register(am);

        new Action<>(OPEN_IN_BROWSER)
            .withLabel("Open In Browser")
            .withIcon(AzureIcons.Action.RESTART.getIconPath())
            .withIdParam(AbstractAzResource::getName)
            .enableWhen(s -> s instanceof Revision && ((Revision) s).getFormalStatus().isConnected())
            .withHandler(s -> am.getAction(ResourceCommonActionsContributor.OPEN_URL).handle("https://" + s.getFqdn()))
            .register(am);
    }

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup serviceActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.REFRESH,
            ResourceCommonActionsContributor.OPEN_AZURE_REFERENCE_BOOK,
            "---",
            ResourceCommonActionsContributor.CREATE
        );
        am.registerGroup(SERVICE_ACTIONS, serviceActionGroup);

        final ActionGroup environmentActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.PIN,
            "---",
            ResourceCommonActionsContributor.REFRESH,
            ResourceCommonActionsContributor.OPEN_AZURE_REFERENCE_BOOK,
            ResourceCommonActionsContributor.OPEN_PORTAL_URL,
            "---",
            ResourceCommonActionsContributor.CREATE,
            ResourceCommonActionsContributor.DELETE
        );
        am.registerGroup(ENVIRONMENT_ACTIONS, environmentActionGroup);

        final ActionGroup containerAppActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.PIN,
            "---",
            ResourceCommonActionsContributor.REFRESH,
            ResourceCommonActionsContributor.OPEN_AZURE_REFERENCE_BOOK,
            ResourceCommonActionsContributor.OPEN_PORTAL_URL,
            ContainerAppsActionsContributor.OPEN_LATEST_REVISION_IN_BROWSER,
            "---",
            ContainerAppsActionsContributor.ACTIVATE_LATEST_REVISION,
            ContainerAppsActionsContributor.DEACTIVATE_LATEST_REVISION,
            ContainerAppsActionsContributor.RESTART_LATEST_REVISION,
            ContainerAppsActionsContributor.UPDATE_IMAGE,
            ResourceCommonActionsContributor.DELETE,
            "---",
            ContainerAppsActionsContributor.OPEN_LOG_STREAMS
        );
        am.registerGroup(CONTAINER_APP_ACTIONS, containerAppActionGroup);

        final ActionGroup revisionActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.PIN,
            "---",
            ResourceCommonActionsContributor.REFRESH,
            ContainerAppsActionsContributor.OPEN_IN_BROWSER,
            "---",
            ContainerAppsActionsContributor.ACTIVATE,
            ContainerAppsActionsContributor.DEACTIVATE,
            ContainerAppsActionsContributor.RESTART,
            ResourceCommonActionsContributor.DELETE
        );
        am.registerGroup(REVISION_ACTIONS, revisionActionGroup);
    }

    @Override
    public int getOrder() {
        return INITIALIZE_ORDER;
    }
}
