package com.mycompany.myorg.vehicle.packager.pubsub;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

@Component
public class PubSubSharedData {
    public CountDownLatch latch = new CountDownLatch(1);
    public final List<String> tenantsSeen = new CopyOnWriteArrayList<>();
    public final List<String> messages = new CopyOnWriteArrayList<>();

    public void reset(int count) {
        latch = new CountDownLatch(count);
        tenantsSeen.clear();
        messages.clear();
    }

    public void record(String tenant, String message) {
        tenantsSeen.add(tenant);
        messages.add(message);
        latch.countDown();
    }
}
