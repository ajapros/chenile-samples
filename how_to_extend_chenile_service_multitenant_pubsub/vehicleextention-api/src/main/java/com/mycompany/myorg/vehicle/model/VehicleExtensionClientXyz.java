package com.mycompany.myorg.vehicle.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("client_xyz_ext")
public class VehicleExtensionClientXyz extends VehicleExtension {

    @Column(name = "xyz_branch_code")
    public String xyzBranchCode;

    @Column(name = "xyz_segment")
    public String xyzSegment;

    @Column(name = "xyz_priority")
    public String xyzPriority;
}
