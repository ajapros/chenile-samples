package org.chenile.samples.issue.model;

import org.chenile.workflow.param.MinimalPayload;

import java.io.Serial;

public class CompleteTaskPayload extends MinimalPayload {
    @Serial
    private static final long serialVersionUID = 1L;
    public String taskId;
}