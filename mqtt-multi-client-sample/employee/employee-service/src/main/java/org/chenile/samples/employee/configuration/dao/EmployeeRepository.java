package org.chenile.samples.employee.configuration.dao;

import org.chenile.samples.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository  public interface EmployeeRepository extends JpaRepository<Employee,String> {}
