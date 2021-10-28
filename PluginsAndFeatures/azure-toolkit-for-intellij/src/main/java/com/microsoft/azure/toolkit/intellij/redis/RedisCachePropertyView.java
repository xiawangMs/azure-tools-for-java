/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.redis;

import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.intellij.common.BaseEditor;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.redis.AzureRedis;
import com.microsoft.azuretools.azurecommons.util.Utils;
import com.microsoft.azuretools.core.mvp.ui.rediscache.RedisCacheProperty;
import com.microsoft.tooling.msservices.serviceexplorer.azure.rediscache.RedisPropertyMvpView;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.swing.*;

public class RedisCachePropertyView extends BaseEditor implements RedisPropertyMvpView {

    public static final String ID = "com.microsoft.intellij.helpers.rediscache.RedisCachePropertyView";

    private String primaryKey = "";
    private String secondaryKey = "";

    private static final String COPY_FAIL = "Cannot copy to system clipboard.";

    private JPanel pnlContent;
    private JTextField txtNameValue;
    private JTextField txtTypeValue;
    private JTextField txtResGrpValue;
    private JTextField txtSubscriptionValue;
    private JTextField txtRegionValue;
    private JTextField txtHostNameValue;
    private JTextField txtSslPortValue;
    private JTextField txtNonSslPortValue;
    private JTextField txtVersionValue;
    private JButton btnPrimaryKey;
    private JButton btnSecondaryKey;


    public RedisCachePropertyView(@NotNull final VirtualFile virtualFile) {
        super(virtualFile);
        disableTxtBoard();
        makeTxtOpaque();

        btnPrimaryKey.addActionListener(event -> {
            try {
                Utils.copyToSystemClipboard(primaryKey);
            } catch (Exception e) {
                onError(e.getMessage());
            }
        });

        btnSecondaryKey.addActionListener(event -> {
            try {
                Utils.copyToSystemClipboard(secondaryKey);
            } catch (Exception e) {
                onError(e.getMessage());
            }
        });
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return pnlContent;
    }

    @NotNull
    @Override
    public String getName() {
        return ID;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void onReadProperty(String sid, String id) {
        AzureRedis az = Azure.az(AzureRedis.class).subscription(sid);
        Mono.fromCallable(() -> az.get(id).entity()).map(redis -> {
            RedisCacheProperty property = new RedisCacheProperty(redis.getName(), redis.getType(), redis.getResourceGroupName(),
                    redis.getRegion().getName(), sid, redis.getRedisVersion(), redis.getSSLPort(), redis.getNonSslPortEnabled(),
                    redis.getPrimaryKey(), redis.getSecondaryKey(), redis.getHostName());
            return property;
        }).subscribeOn(Schedulers.boundedElastic()).subscribe(property -> {
            this.showProperty(property);
        });

    }

    @Override
    public void showProperty(RedisCacheProperty property) {
        primaryKey = property.getPrimaryKey();
        secondaryKey = property.getSecondaryKey();

        txtNameValue.setText(property.getName());
        txtTypeValue.setText(property.getType());
        txtResGrpValue.setText(property.getGroupName());
        txtSubscriptionValue.setText(property.getSubscriptionId());
        txtRegionValue.setText(property.getRegionName());
        txtHostNameValue.setText(property.getHostName());
        txtSslPortValue.setText(String.valueOf(property.getSslPort()));
        txtNonSslPortValue.setText(String.valueOf(property.isNonSslPort()));
        txtVersionValue.setText(property.getVersion());
        btnPrimaryKey.setEnabled(true);
        btnSecondaryKey.setEnabled(true);
    }

    private void disableTxtBoard() {
        txtNameValue.setBorder(BorderFactory.createEmptyBorder());
        txtTypeValue.setBorder(BorderFactory.createEmptyBorder());
        txtResGrpValue.setBorder(BorderFactory.createEmptyBorder());
        txtSubscriptionValue.setBorder(BorderFactory.createEmptyBorder());
        txtRegionValue.setBorder(BorderFactory.createEmptyBorder());
        txtHostNameValue.setBorder(BorderFactory.createEmptyBorder());
        txtSslPortValue.setBorder(BorderFactory.createEmptyBorder());
        txtNonSslPortValue.setBorder(BorderFactory.createEmptyBorder());
        txtVersionValue.setBorder(BorderFactory.createEmptyBorder());
    }

    private void makeTxtOpaque() {
        txtNameValue.setBackground(null);
        txtTypeValue.setBackground(null);
        txtResGrpValue.setBackground(null);
        txtSubscriptionValue.setBackground(null);
        txtRegionValue.setBackground(null);
        txtHostNameValue.setBackground(null);
        txtSslPortValue.setBackground(null);
        txtNonSslPortValue.setBackground(null);
        txtVersionValue.setBackground(null);
    }
}
