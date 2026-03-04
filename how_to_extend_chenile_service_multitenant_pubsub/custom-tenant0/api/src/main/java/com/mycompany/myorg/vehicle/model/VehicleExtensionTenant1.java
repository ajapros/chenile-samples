package com.mycompany.myorg.vehicle.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.chenile.core.context.ContextContainer;
import org.chenile.core.context.HeaderUtils;
import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.model.Transition;
import org.chenile.workflow.param.MinimalPayload;
import org.springframework.beans.factory.annotation.Autowired;

@Entity
@DiscriminatorValue("tenant1_ext")
public class VehicleExtensionTenant1 extends Vehicle {

    @Column(name = "insurance_policy_number")
    public String insurancePolicyNumber;

    @Column(name = "fitness_expiry")
    public String fitnessExpiry;

    @Column(name = "new_column")
    public String newColumn;

    @Column(name = "tenant1_code")
    public String tenant1Code;

    @Column(name = "tenant1_segment")
    public String tenant1Segment;

    @Column(name = "tenant1_priority")
    public String tenant1Priority;

    @Column(name = "tenant1_workflow_note")
    public String tenant1WorkflowNote;
}




