package org.chenile.samples.issue.service.cmds;

import org.chenile.samples.issue.model.Issue;
import org.chenile.samples.issue.model.Task;
import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.model.Transition;

import java.util.ArrayList;

public class AddTask implements STMTransitionAction<Issue> {
    public void doTransition(Issue issue, Object transitionParam, State startState, String eventId,
                             State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
       Task task = (Task) transitionParam;
       if (issue.tasks == null){
           issue.tasks = new ArrayList<Task>();
       }
       issue.tasks.add(task);
    }
}
