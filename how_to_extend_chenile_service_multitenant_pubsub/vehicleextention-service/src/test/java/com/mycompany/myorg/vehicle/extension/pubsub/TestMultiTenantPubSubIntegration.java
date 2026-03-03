package com.mycompany.myorg.vehicle.extension.pubsub;

import org.chenile.core.context.HeaderUtils;
import org.chenile.pubsub.ChenilePub;
import org.chenile.pubsub.jvm.storage.JvmPubSubStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = com.mycompany.myorg.vehicle.extension.VehicleExtensionSpringTestConfig.class)
public class TestMultiTenantPubSubIntegration {

    @Autowired
    private ChenilePub chenilePub;

    @Autowired
    private PubSubSharedData sharedData;

    @Autowired
    private JvmPubSubStorage jvmPubSubStorage;

    @BeforeEach
    void reset() {
        sharedData.reset(2);
        jvmPubSubStorage.clear();
    }

    @Test
    void asyncPublishCarriesTenantToSubscriber() throws Exception {
        chenilePub.asyncPublish("vehicle.events.test", "{\"message\":\"m1\"}",
                Map.of(HeaderUtils.TENANT_ID_KEY, "tenant1"));
        chenilePub.asyncPublish("vehicle.events.test", "{\"message\":\"m2\"}",
                Map.of(HeaderUtils.TENANT_ID_KEY, "tenant2"));

        assertTrue(sharedData.latch.await(5, TimeUnit.SECONDS));
        Assertions.assertEquals(2, jvmPubSubStorage.findByTopic("vehicle.events.test").size());
        Assertions.assertTrue(sharedData.tenantsSeen.contains("tenant1"));
        Assertions.assertTrue(sharedData.tenantsSeen.contains("tenant2"));
        Assertions.assertTrue(sharedData.messages.contains("m1"));
        Assertions.assertTrue(sharedData.messages.contains("m2"));
    }
}
