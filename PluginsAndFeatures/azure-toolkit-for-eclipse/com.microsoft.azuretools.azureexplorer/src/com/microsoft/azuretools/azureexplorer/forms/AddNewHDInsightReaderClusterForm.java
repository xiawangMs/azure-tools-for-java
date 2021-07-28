/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.forms;

import org.eclipse.swt.widgets.Shell;

import com.microsoft.azure.hdinsight.common.ClusterManagerEx;
import com.microsoft.azure.hdinsight.sdk.cluster.ClusterDetail;
import com.microsoft.azure.hdinsight.sdk.cluster.HDInsightAdditionalClusterDetail;
import com.microsoft.azure.hdinsight.sdk.cluster.IClusterDetail;
import com.microsoft.azure.hdinsight.serverexplore.hdinsightnode.HDInsightRootModule;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;

public class AddNewHDInsightReaderClusterForm extends AddNewClusterForm {
    private ClusterDetail selectedClusterDetail;
    private String defaultStorageRootPath;

    public AddNewHDInsightReaderClusterForm(Shell parentShell, @Nullable HDInsightRootModule hdinsightRootModule, @NotNull ClusterDetail selectedClusterDetail) {
        super(parentShell, hdinsightRootModule);
        this.selectedClusterDetail = selectedClusterDetail;
        this.defaultStorageRootPath = selectedClusterDetail.getDefaultStorageRootPath();
    }

    @Override
    protected void customizeUI() {
        this.clusterNameField.setText(selectedClusterDetail.getName());
        this.clusterNameField.setEditable(false);
    }

    protected boolean getSelectedLinkedHdiCluster(@NotNull IClusterDetail clusterDetail,
                                                  @NotNull String selectedClusterName) {
        return clusterDetail instanceof HDInsightAdditionalClusterDetail
                && clusterDetail.getName().equals(selectedClusterName);
    }

    @Override
    protected void afterOkActionPerformed() {
        HDInsightAdditionalClusterDetail linkedCluster =
                (HDInsightAdditionalClusterDetail) ClusterManagerEx.getInstance().findClusterDetail(clusterDetail ->
                        getSelectedLinkedHdiCluster(clusterDetail, selectedClusterDetail.getName()), true);
        if (linkedCluster != null) {
            linkedCluster.setDefaultStorageRootPath(defaultStorageRootPath);
            ClusterManagerEx.getInstance().updateHdiAdditionalClusterDetail(linkedCluster);
        }

        super.afterOkActionPerformed();
    }
}
