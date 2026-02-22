package com.mycompany.myorg.vehicle.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import org.chenile.jpautils.entity.AbstractJpaStateEntity;

@Entity
@Table(name = "vehicle")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "vehicle_type")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "vehicle_type", defaultImpl = Vehicle.class)
public class Vehicle extends AbstractJpaStateEntity
{
	public String assignee;
	public String assignComment;
	public String closeComment;
	public String resolveComment;
	public String description;
	public String openedBy;
}
