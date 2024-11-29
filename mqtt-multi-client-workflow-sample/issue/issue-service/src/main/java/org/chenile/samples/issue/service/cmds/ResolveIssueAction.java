package org.chenile.samples.issue.service.cmds;

import org.chenile.base.exception.ErrorNumException;
import org.chenile.samples.issue.model.Task;
import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.model.Transition;

import org.chenile.workflow.param.MinimalPayload;
import org.chenile.samples.issue.model.Issue;

public class ResolveIssueAction implements STMTransitionAction<Issue>{

	@Override
	public void doTransition(Issue issue, Object transitionParam, State startState, String eventId,
			State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
		MinimalPayload payload = (MinimalPayload) transitionParam;

		if (issue.tasks == null) return;
		for(Task task: issue.tasks){
			if (task.stateId.equals(Task.OPENED))
				throw new ErrorNumException(422,7003,new Object[]{task.id,issue.id});
		}
		issue.resolveComment = payload.getComment();
	}
}
