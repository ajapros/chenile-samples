package com.mycompany.myorg.vehicle.service.cmds;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.model.Transition;

import org.chenile.workflow.param.MinimalPayload;
import org.chenile.workflow.service.stmcmds.AbstractSTMTransitionAction;
import com.mycompany.myorg.vehicle.model.Vehicle;

public class ResolveVehicleAction extends AbstractSTMTransitionAction<Vehicle,MinimalPayload>{

	@Override
	public void transitionTo(Vehicle vehicle, MinimalPayload payload, State startState, String eventId,
			State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
		vehicle.resolveComment = payload.getComment();
	}

}
