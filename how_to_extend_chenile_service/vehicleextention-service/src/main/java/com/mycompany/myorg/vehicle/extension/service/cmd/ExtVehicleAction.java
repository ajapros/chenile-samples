package com.mycompany.myorg.vehicle.extension.service.cmd;

import com.mycompany.myorg.vehicle.model.VehicleExtension;
import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.model.Transition;
import org.chenile.workflow.param.MinimalPayload;
import org.chenile.workflow.service.stmcmds.AbstractSTMTransitionAction;

public class ExtVehicleAction extends AbstractSTMTransitionAction<VehicleExtension, MinimalPayload> {

    @Override
    public void transitionTo(VehicleExtension vehicle, MinimalPayload payload, State startState, String eventId,
                             State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
        vehicle.newColumn = payload.getComment();
    }

}