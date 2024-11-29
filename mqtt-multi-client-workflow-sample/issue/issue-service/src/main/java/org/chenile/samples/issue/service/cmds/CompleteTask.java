package org.chenile.samples.issue.service.cmds;

import org.chenile.base.exception.ErrorNumException;
import org.chenile.samples.issue.model.CompleteTaskPayload;
import org.chenile.samples.issue.model.Issue;
import org.chenile.samples.issue.model.Task;
import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.model.Transition;

import java.util.ArrayList;

public class CompleteTask implements STMTransitionAction<Issue> {
    public void doTransition(Issue issue, Object transitionParam, State startState, String eventId,
                             State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
        CompleteTaskPayload payload = (CompleteTaskPayload) transitionParam;
        if (payload.taskId == null){
            throw new ErrorNumException(400,7000,new Object[] {});
        }
        for (Task task:issue.tasks){
            if (task.id.equals(payload.taskId)){
                completeTask(task,payload);
                return;
            }
        }
        throw new ErrorNumException(400,7002,new Object[] {payload.taskId, issue.id});
    }

    private void completeTask(Task task, CompleteTaskPayload payload){
        if (task.stateId.equals(Task.COMPLETED)){
            throw new ErrorNumException(400,7001,new Object[]{payload.taskId});
        }
        if (payload.getComment() != null){
            task.completedComment = payload.getComment();
        }
        task.stateId = Task.COMPLETED;
    }
}
