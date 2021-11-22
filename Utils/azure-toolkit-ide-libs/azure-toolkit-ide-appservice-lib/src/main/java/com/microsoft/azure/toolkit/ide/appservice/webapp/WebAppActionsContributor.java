/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.webapp;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.appservice.service.IAppService;
import com.microsoft.azure.toolkit.lib.appservice.service.IWebApp;
import com.microsoft.azure.toolkit.lib.appservice.service.IWebAppDeploymentSlot;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.entity.IAzureBaseResource;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle.title;

public class WebAppActionsContributor implements IActionsContributor {
    public static final String SERVICE_ACTIONS = "actions.webapp.service";
    public static final String WEBAPP_ACTIONS = "actions.webapp.management";
    public static final String DEPLOYMENT_SLOTS_ACTIONS = "actions.webapp.deployments";
    public static final String DEPLOYMENT_SLOT_ACTIONS = "actions.webapp.deployments.slot";

    public static final Action.Id<IAppService<?>> OPEN_IN_BROWSER = Action.Id.of("actions.webapp.open_in_browser");
    public static final Action.Id<IAppService<?>> START_STREAM_LOG = Action.Id.of("actions.webapp.stream_log.start");
    public static final Action.Id<IAppService<?>> STOP_STREAM_LOG = Action.Id.of("actions.webapp.stream_log.stop");
    public static final Action.Id<IWebApp> REFRESH_DEPLOYMENT_SLOTS = Action.Id.of("actions.webapp.deployments.refresh");
    public static final Action.Id<IWebAppDeploymentSlot> SWAP_DEPLOYMENT_SLOT = Action.Id.of("action.webapp.slot.swap");

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup serviceActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.SERVICE_REFRESH,
                ResourceCommonActionsContributor.CREATE
        );
        am.registerGroup(SERVICE_ACTIONS, serviceActionGroup);

        final ActionGroup webAppActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.REFRESH,
                ResourceCommonActionsContributor.OPEN_PORTAL_URL,
                WebAppActionsContributor.OPEN_IN_BROWSER,
                ResourceCommonActionsContributor.DEPLOY,
                ResourceCommonActionsContributor.SHOW_PROPERTIES,
                "---",
                ResourceCommonActionsContributor.START,
                ResourceCommonActionsContributor.STOP,
                ResourceCommonActionsContributor.RESTART,
                ResourceCommonActionsContributor.DELETE,
                "___",
                WebAppActionsContributor.START_STREAM_LOG,
                WebAppActionsContributor.STOP_STREAM_LOG
                // todo: add profile actions like log streaming
        );
        am.registerGroup(WEBAPP_ACTIONS, webAppActionGroup);

        final ActionGroup deploymentSlotActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.REFRESH,
                ResourceCommonActionsContributor.OPEN_PORTAL_URL,
                WebAppActionsContributor.OPEN_IN_BROWSER,
                ResourceCommonActionsContributor.SHOW_PROPERTIES,
                SWAP_DEPLOYMENT_SLOT,
                "---",
                ResourceCommonActionsContributor.START,
                ResourceCommonActionsContributor.STOP,
                ResourceCommonActionsContributor.RESTART,
                ResourceCommonActionsContributor.DELETE,
                "___",
                WebAppActionsContributor.START_STREAM_LOG,
                WebAppActionsContributor.STOP_STREAM_LOG
        );
        am.registerGroup(DEPLOYMENT_SLOT_ACTIONS, deploymentSlotActionGroup);

        am.registerGroup(DEPLOYMENT_SLOTS_ACTIONS, new ActionGroup(REFRESH_DEPLOYMENT_SLOTS));
    }

    @Override
    public void registerActions(AzureActionManager am) {
        final Consumer<IWebApp> refresh = webApp -> AzureEventBus.emit("appservice|webapp.slot.refresh", webApp);
        final ActionView.Builder refreshView = new ActionView.Builder("Refresh", "/icons/action/refresh.svg")
                .title(s -> Optional.ofNullable(s).map(r -> title("appservice|webapp.slot.refresh", ((IWebApp) r).name())).orElse(null))
                .enabled(s -> s instanceof IWebApp);
        am.registerAction(REFRESH_DEPLOYMENT_SLOTS, new Action<>(refresh, refreshView));

        final Consumer<IWebAppDeploymentSlot> swap = slot -> slot.webApp().swap(slot.name());
        final ActionView.Builder swapView = new ActionView.Builder("Swap With Production", "/icons/action/refresh.svg")
                .title(s -> Optional.ofNullable(s).map(r -> title("webapp|deployment.swap",
                        ((IWebAppDeploymentSlot) s).name(), ((IWebAppDeploymentSlot) s).webApp().name())).orElse(null))
                .enabled(s -> s instanceof IWebAppDeploymentSlot && StringUtils.equals(((IWebAppDeploymentSlot) s).status(), IAzureBaseResource.Status.RUNNING));
        am.registerAction(SWAP_DEPLOYMENT_SLOT, new Action<>(swap, swapView));

        final Consumer<IAppService<?>> openInBrowser = appService -> am.getAction(ResourceCommonActionsContributor.OPEN_URL)
                .handle("https://" + appService.hostName());
        final ActionView.Builder openInBrowserView = new ActionView.Builder("Open In Browser", "/icons/action/refresh.svg")
                .title(s -> Optional.ofNullable(s).map(r -> title("webapp.open_browser")).orElse(null))
                .enabled(s -> s instanceof IAppService);
        am.registerAction(OPEN_IN_BROWSER, new Action<>(openInBrowser, openInBrowserView));

        final ActionView.Builder startStreamLogView = new ActionView.Builder("Start Streaming Logs", "/icons/action/log.svg")
                .title(s -> Optional.ofNullable(s).map(r -> title("webapp|app.start_stream_log", ((IAppService) r).name())).orElse(null))
                .enabled(s -> s instanceof IAppService);
        am.registerAction(START_STREAM_LOG, new Action<>(startStreamLogView));

        final ActionView.Builder stopStreamLogView = new ActionView.Builder("Stop Streaming Logs", "/icons/action/log.svg")
                .title(s -> Optional.ofNullable(s).map(r -> title("webapp|app.stop_stream_log", ((IAppService) r).name())).orElse(null))
                .enabled(s -> s instanceof IAppService);
        am.registerAction(STOP_STREAM_LOG, new Action<>(stopStreamLogView));
    }

    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiPredicate<IAzureBaseResource<?, ?>, Object> startCondition = (r, e) -> r instanceof IAppService &&
                StringUtils.equals(r.status(), IAzureBaseResource.Status.STOPPED);
        final BiConsumer<IAzureBaseResource<?, ?>, Object> startHandler = (c, e) -> ((IAppService) c).start();
        am.registerHandler(ResourceCommonActionsContributor.START, startCondition, startHandler);

        final BiPredicate<IAzureBaseResource<?, ?>, Object> stopCondition = (r, e) -> r instanceof IAppService &&
                StringUtils.equals(r.status(), IAzureBaseResource.Status.RUNNING);
        final BiConsumer<IAzureBaseResource<?, ?>, Object> stopHandler = (c, e) -> ((IAppService) c).stop();
        am.registerHandler(ResourceCommonActionsContributor.STOP, stopCondition, stopHandler);

        final BiPredicate<IAzureBaseResource<?, ?>, Object> restartCondition = (r, e) -> r instanceof IAppService &&
                StringUtils.equals(r.status(), IAzureBaseResource.Status.RUNNING);
        final BiConsumer<IAzureBaseResource<?, ?>, Object> restartHandler = (c, e) -> ((IAppService) c).restart();
        am.registerHandler(ResourceCommonActionsContributor.RESTART, restartCondition, restartHandler);
    }

    @Override
    public int getOrder() {
        return 1; //after azure resource common actions registered
    }
}
