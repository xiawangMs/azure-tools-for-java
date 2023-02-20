package com.microsoft.azure.toolkit.intellij.eventhubs;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.eventhubs.EventHubsActionsContributor;
import com.microsoft.azure.toolkit.intellij.common.AzureBundle;
import com.microsoft.azure.toolkit.intellij.common.messager.IntellijAzureMessager;
import com.microsoft.azure.toolkit.intellij.eventhubs.view.EventHubsMessageDialog;
import com.microsoft.azure.toolkit.intellij.eventhubs.view.EventHubsToolWindowManager;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessage;
import com.microsoft.azure.toolkit.lib.common.operation.OperationContext;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.eventhubs.EventHubsInstance;
import com.microsoft.intellij.RunProcessHandler;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;


public class IntellijEventHubsActionsContributor implements IActionsContributor {
    @Override
    public void registerHandlers(AzureActionManager am) {
        registerActiveActionHandler(am);
        registerDisabledActionHandler(am);
        registerSendDisabledActionHandler(am);
        registerSendMessageActionHandler(am);
        registerStartListeningActionHandler(am);
        registerStopListeningActionHandler(am);
    }

    private void registerActiveActionHandler(AzureActionManager am) {
        final BiPredicate<EventHubsInstance, AnActionEvent> condition = (r, e) -> true;
        final BiConsumer<EventHubsInstance, AnActionEvent> handler = (c, e) -> c.active();
        am.registerHandler(EventHubsActionsContributor.ACTIVE_INSTANCE, condition, handler);
    }

    private void registerDisabledActionHandler(AzureActionManager am) {
        final BiPredicate<EventHubsInstance, AnActionEvent> condition = (r, e) -> true;
        final BiConsumer<EventHubsInstance, AnActionEvent> handler = (c, e) -> c.disable();
        am.registerHandler(EventHubsActionsContributor.DISABLE_INSTANCE, condition, handler);
    }

    private void registerSendDisabledActionHandler(AzureActionManager am) {
        final BiPredicate<EventHubsInstance, AnActionEvent> condition = (r, e) -> true;
        final BiConsumer<EventHubsInstance, AnActionEvent> handler = (c, e) -> c.disableSending();
        am.registerHandler(EventHubsActionsContributor.SEND_DISABLE_INSTANCE, condition, handler);
    }

    private void registerSendMessageActionHandler(AzureActionManager am) {
        final BiPredicate<EventHubsInstance, AnActionEvent> condition = (r, e) -> true;
        final BiConsumer<EventHubsInstance, AnActionEvent> handler = (c, e) ->
                AzureTaskManager.getInstance().runLater(() -> {
                    final EventHubsMessageDialog dialog = new EventHubsMessageDialog(e.getProject(), c);
                    dialog.showAndGet();
                });
        am.registerHandler(EventHubsActionsContributor.SEND_MESSAGE_INSTANCE, condition, handler);
    }

    private void registerStartListeningActionHandler(AzureActionManager am) {
        final BiPredicate<EventHubsInstance, AnActionEvent> condition = (r, e) -> true;
        final BiConsumer<EventHubsInstance, AnActionEvent> handler = (c, e) -> {
            final ConsoleView consoleView = createConsoleView(e.getProject(), c);
            AzureTaskManager.getInstance().runLater(() ->
                    EventHubsToolWindowManager.getInstance().showEventHubsConsole(e.getProject(), c.getId(), c.getName(), consoleView));
        };
        am.registerHandler(EventHubsActionsContributor.START_LISTENING_INSTANCE, condition, handler);
    }

    private void registerStopListeningActionHandler(AzureActionManager am) {
        final BiPredicate<EventHubsInstance, AnActionEvent> condition = (r, e) -> true;
        final BiConsumer<EventHubsInstance, AnActionEvent> handler = (c, e) -> c.stopListening();
        am.registerHandler(EventHubsActionsContributor.STOP_LISTENING_INSTANCE, condition, handler);
    }

    private void registerCopyConnectionStringActionHandler(AzureActionManager am) {
        final BiPredicate<EventHubsInstance, AnActionEvent> condition = (r, e) -> true;
        final BiConsumer<EventHubsInstance, AnActionEvent> handler = (c, e) -> {
            final String connectionString = c.getOrCreateListenConnectionString();
            am.getAction(ResourceCommonActionsContributor.COPY_STRING).handle(connectionString);
            AzureMessager.getMessager().info(AzureBundle.message("azure.eventhubs.info.copyConnectionString"));
        };
        am.registerHandler(EventHubsActionsContributor.COPY_CONNECTION_STRING, condition, handler);
    }

    private ConsoleView createConsoleView(Project project, EventHubsInstance instance) {
        final RunProcessHandler processHandler = new RunProcessHandler();
        processHandler.addDefaultListener();
        processHandler.startNotify();
        final ConsoleMessager messager = new ConsoleMessager(processHandler);
        final ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        consoleView.attachToProcess(processHandler);
        final Runnable execute = () -> {
            OperationContext.current().setMessager(messager);
            instance.startListening();
        };
        final Disposable subscribe = Mono.fromRunnable(execute)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
        processHandler.addProcessListener(new ProcessAdapter() {
            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                subscribe.dispose();
            }
        });
        return consoleView;
    }

    @Override
    public int getOrder() {
        return EventHubsActionsContributor.INITIALIZE_ORDER + 1;
    }

    @RequiredArgsConstructor
    private static class ConsoleMessager extends IntellijAzureMessager {
        private final RunProcessHandler handler;

        @Override
        public boolean show(IAzureMessage raw) {
            if (raw.getType() == IAzureMessage.Type.INFO) {
                handler.setText(raw.getMessage().toString());
                return true;
            } else if (raw.getType() == IAzureMessage.Type.SUCCESS) {
                handler.println(raw.getMessage().toString(), ProcessOutputType.STDOUT);
            } else if (raw.getType() == IAzureMessage.Type.WARNING) {
                handler.println(raw.getMessage().toString(), ProcessOutputType.STDOUT);
            } else if (raw.getType() == IAzureMessage.Type.ERROR) {
                handler.println(raw.getContent(), ProcessOutputType.STDERR);
            }
            return super.show(raw);
        }
    }
}
