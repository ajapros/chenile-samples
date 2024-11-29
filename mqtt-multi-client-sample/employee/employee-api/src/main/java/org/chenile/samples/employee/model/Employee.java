package org.chenile.samples.employee.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.chenile.jpautils.entity.BaseJpaEntity;


 @Entity
 @Table(name = "employee")
 public class Employee extends BaseJpaEntity  {
	// Define the model attributes here
	public String firstName;
	public String lastName;
	public String email;
}
