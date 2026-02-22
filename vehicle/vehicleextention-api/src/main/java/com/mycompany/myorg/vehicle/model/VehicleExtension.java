package com.mycompany.myorg.vehicle.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("client_abc_ext")
public class VehicleExtension extends Vehicle {
    @Column(name = "insurance_policy_number")
    public String insurancePolicyNumber;

    @Column(name = "fitness_expiry")
    public String fitnessExpiry;
}
