package com.mycompany.myorg.vehicle.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("tenant0_ext")
public class VehicleExtensionTenant0 extends Vehicle {

    @Column(name = "tenant0_code")
    public String tenant0Code;

    @Column(name = "tenant0_segment")
    public String tenant0Segment;

    @Column(name = "tenant0_priority")
    public String tenant0Priority;

    @Column(name = "tenant0_workflow_note")
    public String tenant0WorkflowNote;

    @Column(name = "new_column")
    public String newColumn;
}
