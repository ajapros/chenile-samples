package org.chenile.samples.employee.service;

import org.chenile.samples.employee.model.Employee;

public interface EmployeeService {
	// Define your interface here
    Employee save(Employee user);
    Employee retrieve(String id);


}
