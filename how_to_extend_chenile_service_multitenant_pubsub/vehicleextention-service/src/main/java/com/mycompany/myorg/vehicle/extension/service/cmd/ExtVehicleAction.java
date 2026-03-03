package com.mycompany.myorg.vehicle.extension.service.cmd;

import com.mycompany.myorg.vehicle.model.VehicleExtension;
import org.chenile.core.context.ContextContainer;
import org.chenile.core.context.HeaderUtils;
import org.chenile.pubsub.ChenilePub;
import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.model.Transition;
import org.chenile.workflow.param.MinimalPayload;
import org.chenile.workflow.service.stmcmds.AbstractSTMTransitionAction;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class ExtVehicleAction extends AbstractSTMTransitionAction<VehicleExtension, MinimalPayload> {

    private ChenilePub chenilePub;

    @Autowired(required = false)
    public void setChenilePub(ChenilePub chenilePub) {
        this.chenilePub = chenilePub;
    }

    @Override
    public void transitionTo(VehicleExtension vehicle, MinimalPayload payload, State startState, String eventId,
                             State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
        vehicle.newColumn = payload.getComment();
        publishWorkflowEvent(vehicle, payload);
    }

    private void publishWorkflowEvent(VehicleExtension vehicle, MinimalPayload payload) {
        if (chenilePub == null) {
            return;
        }

        String comment = payload == null || payload.getComment() == null ? "" : payload.getComment();
        String message = "EXT:" + vehicle.getId() + ":" + comment;
        String body = "{\"message\":\"" + escapeJson(message) + "\"}";

        Map<String, Object> headers = new HashMap<>();
        String tenant = ContextContainer.getInstance().getTenant();
        if (tenant != null && !tenant.isBlank()) {
            headers.put(HeaderUtils.TENANT_ID_KEY, tenant);
        }

        chenilePub.asyncPublish("vehicle.events.test", body, headers);
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
