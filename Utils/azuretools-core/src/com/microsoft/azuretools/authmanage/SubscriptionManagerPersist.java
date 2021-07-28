/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.authmanage;

import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.Tenant;
import com.microsoft.azuretools.adauth.AuthException;
import com.microsoft.azuretools.adauth.JsonHelper;
import com.microsoft.azuretools.authmanage.models.SubscriptionDetail;
import com.microsoft.azuretools.sdkmanage.AzureManager;
import com.microsoft.azuretools.utils.Pair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionManagerPersist extends SubscriptionManager {

    public SubscriptionManagerPersist(AzureManager azureManager) {
        super(azureManager);
    }

    @Override
    public void setSubscriptionDetails(List<SubscriptionDetail> subscriptionDetails) throws AuthException, IOException {
        System.out.println(Thread.currentThread().getId() + " SubscriptionManagerPersist.setSubscriptionDetails()");
        synchronized (this) {
            String subscriptionsDetailsFileName = azureManager.getSettings().getSubscriptionsDetailsFileName();
            saveSubscriptions(subscriptionDetails, subscriptionsDetailsFileName);
        }
        super.setSubscriptionDetails(subscriptionDetails);
    }

    @Override
    protected List<SubscriptionDetail> updateAccountSubscriptionList() throws IOException {
        System.out.println(Thread.currentThread().getId()
                + "SubscriptionManagerPersist.updateAccountSubscriptionList()");
        List<SubscriptionDetail> sdl = null;
        synchronized (this) {
            String subscriptionsDetailsFileName = azureManager.getSettings().getSubscriptionsDetailsFileName();
            sdl = loadSubscriptions(subscriptionsDetailsFileName);
        }

        if (sdl == null) {
            return super.updateAccountSubscriptionList();
        }

        // Filter available SubscriptionDetail
        Map<String, SubscriptionDetail> sdmap = new HashMap<>();
        for (SubscriptionDetail sd : sdl) {
            sdmap.put(sd.getSubscriptionId(), sd);
        }

        List<SubscriptionDetail> ret = new ArrayList<>();
        subscriptionIdToSubscriptionMap.clear();
        List<Pair<Subscription, Tenant>> stpl = azureManager.getSubscriptionsWithTenant();
        for (Pair<Subscription, Tenant> stp : stpl) {
            String sid = stp.first().subscriptionId();
            boolean isSelected = (sdmap.get(sid) != null && sdmap.get(sid).isSelected());
            ret.add(new SubscriptionDetail(
                    stp.first().subscriptionId(),
                    stp.first().displayName(),
                    stp.second().tenantId(),
                    isSelected));
            subscriptionIdToSubscriptionMap.put(stp.first().subscriptionId(), stp.first());
        }
        return ret;
    }

    @Override
    public synchronized void cleanSubscriptions() throws IOException {
        System.out.println(Thread.currentThread().getId() + " SubscriptionManagerPersist.cleanSubscriptions()");
        String subscriptionsDetailsFileName = azureManager.getSettings().getSubscriptionsDetailsFileName();
        deleteSubscriptions(subscriptionsDetailsFileName);
        super.cleanSubscriptions();
    }

    public static synchronized void deleteSubscriptions(String subscriptionsDetailsFileName) throws IOException {
        System.out.println("cleaning " + subscriptionsDetailsFileName + " file");
        FileStorage fs = new FileStorage(subscriptionsDetailsFileName, CommonSettings.getSettingsBaseDir());
        fs.cleanFile();
    }

    private static List<SubscriptionDetail> loadSubscriptions(String subscriptionsDetailsFileName) throws IOException {
        System.out.println("SubscriptionManagerPersist.loadSubscriptions()");

        //subscriptionDetails.clear();
        FileStorage subscriptionsDetailsFileStorage = new FileStorage(subscriptionsDetailsFileName,
                CommonSettings.getSettingsBaseDir());
        byte[] data = subscriptionsDetailsFileStorage.read();
        String json = new String(data, StandardCharsets.UTF_8);
        if (json.isEmpty()) {
            System.out.println(subscriptionsDetailsFileName + " file is empty");
            return null;
        }
        SubscriptionDetail[] sda = JsonHelper.deserialize(SubscriptionDetail[].class, json);
        List<SubscriptionDetail> sdl = new ArrayList<>();
        for (SubscriptionDetail sd : sda) {
            sdl.add(sd);
        }
        return sdl;
    }

    private static void saveSubscriptions(List<SubscriptionDetail> sdl, String subscriptionsDetailsFileName)
            throws IOException {
        System.out.println("SubscriptionManagerPersist.saveSubscriptions()");
        String sd = JsonHelper.serialize(sdl);
        FileStorage subscriptionsDetailsFileStorage = new FileStorage(subscriptionsDetailsFileName,
                CommonSettings.getSettingsBaseDir());
        subscriptionsDetailsFileStorage.write(sd.getBytes(StandardCharsets.UTF_8));
    }

}
