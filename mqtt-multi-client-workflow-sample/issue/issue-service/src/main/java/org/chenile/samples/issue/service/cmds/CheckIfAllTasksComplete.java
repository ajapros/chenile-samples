package org.chenile.samples.issue.service.cmds;

import org.chenile.samples.issue.model.Issue;
import org.chenile.samples.issue.model.Task;
import org.chenile.stm.action.STMAutomaticStateComputation;

public class CheckIfAllTasksComplete implements STMAutomaticStateComputation<Issue> {
    @Override
    public String execute(Issue issue) throws Exception {
        if (issue.tasks == null) return "true";
        for(Task task: issue.tasks){
            if (task.stateId.equals(Task.OPENED))
                return "false";
        }
        return "true";
    }
}
