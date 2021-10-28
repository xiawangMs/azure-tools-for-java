/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.vm;

import com.microsoft.azure.toolkit.ide.common.IExplorerContributor;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.AzureServiceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.compute.vm.AzureVirtualMachine;

public class VirtualMachineExplorerContributor implements IExplorerContributor {
    private static final String NAME = "Virtual Machines";
    private static final String ICON = "/icons/virtualmachine.svg";

    @Override
    public Node<?> getModuleNode() {
        final AzureVirtualMachine service = Azure.az(AzureVirtualMachine.class);
        return new Node<>(service).view(new AzureServiceLabelView<>(service, NAME, ICON))
                .actions(VirtualMachineActionsContributor.SERVICE_ACTIONS)
                .addChildren(AzureVirtualMachine::list, (vm, vmNode) -> new Node<>(vm)
                        .view(new AzureResourceLabelView<>(vm))
                        .actions(VirtualMachineActionsContributor.VM_ACTIONS));
    }
}
