package com.mycompany.myorg.vehicle.service.cmds;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.model.Transition;
import org.chenile.workflow.param.MinimalPayload;
import org.chenile.workflow.service.stmcmds.AbstractSTMTransitionAction;
import com.mycompany.myorg.vehicle.model.Vehicle;

/**
    Use this class to do generic things that are relevant for all actions in the workflow
*/

public class DefaultSTMTransitionAction<PayloadType extends MinimalPayload>
    extends AbstractSTMTransitionAction<Vehicle, PayloadType> {
    @Override
    public void transitionTo(Vehicle vehicle, PayloadType payload,
                 State startState, String eventId, State endState, STMInternalTransitionInvoker<?> stm,
                 Transition transition) {

    }
}