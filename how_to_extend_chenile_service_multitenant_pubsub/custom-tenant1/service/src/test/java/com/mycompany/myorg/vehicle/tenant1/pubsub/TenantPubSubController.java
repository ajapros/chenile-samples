package com.mycompany.myorg.vehicle.tenant1.pubsub;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.annotation.EventsSubscribedTo;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.pubsub.model.ChenilePubSub;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@ChenilePubSub
@ChenileController(value = "tenantPubSubService", serviceName = "tenantPubSubService")
public class TenantPubSubController extends ControllerSupport {

    @PostMapping("/test/pubsub/consume")
    @EventsSubscribedTo({"vehicle.events.test"})
    ResponseEntity<GenericResponse<Map<String, Object>>> consume(HttpServletRequest request,
                                                                 @RequestBody PubSubPayload payload) {
        return process("consume", request, payload);
    }
}
