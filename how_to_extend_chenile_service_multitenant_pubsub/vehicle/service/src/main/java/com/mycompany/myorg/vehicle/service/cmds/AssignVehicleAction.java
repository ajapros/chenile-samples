package com.mycompany.myorg.vehicle.service.cmds;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.model.Transition;
import org.chenile.workflow.service.stmcmds.AbstractSTMTransitionAction;

import com.mycompany.myorg.vehicle.model.AssignVehiclePayload;
import com.mycompany.myorg.vehicle.model.Vehicle;

/**
    This class implements the assign action. It will need to inherit from the AbstractSTMTransitionAction for
    auto wiring into STM to work properly. Be sure to use the PayloadType as the second argument and
    override the transitionTo method accordingly as shown below.
*/
public class AssignVehicleAction extends AbstractSTMTransitionAction<Vehicle,AssignVehiclePayload>{

	@Override
	public void transitionTo(Vehicle vehicle, AssignVehiclePayload payload, State startState, String eventId,
			State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
		vehicle.assignee = payload.assignee;
		vehicle.assignComment = payload.getComment();
	}

}
