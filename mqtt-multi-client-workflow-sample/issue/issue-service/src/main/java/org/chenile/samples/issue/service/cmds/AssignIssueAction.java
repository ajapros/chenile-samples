package org.chenile.samples.issue.service.cmds;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.model.Transition;

import org.chenile.samples.issue.model.AssignIssuePayload;
import org.chenile.samples.issue.model.Issue;

public class AssignIssueAction implements STMTransitionAction<Issue>{

	public void doTransition(Issue issue, Object transitionParam, State startState, String eventId,
			State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
		AssignIssuePayload payload = (AssignIssuePayload) transitionParam;
		issue.assignee = payload.assignee;
		issue.assignComment = payload.getComment();
	}

}
