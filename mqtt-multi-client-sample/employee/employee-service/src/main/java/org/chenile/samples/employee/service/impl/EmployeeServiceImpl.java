package org.chenile.samples.employee.service.impl;

import org.chenile.samples.employee.configuration.dao.EmployeeRepository;
import org.chenile.samples.employee.model.Employee;
import org.chenile.samples.employee.service.EmployeeService;
import org.chenile.base.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class EmployeeServiceImpl implements EmployeeService {
	@Autowired
	EmployeeRepository employeeRepository;
	@Autowired BaseEntityHandler baseEntityHandler;
	@Override
	public Employee save(Employee employee) {
		baseEntityHandler.onBeforeSave(employee);
		employeeRepository.save(employee);
		return employee;
	}


	@Override
	public Employee retrieve(String id) {
		Optional<Employee> emp = employeeRepository.findById(id);
		if (emp.isPresent()) return emp.get();
		throw new NotFoundException(1500,"Unable to find employee with ID " + id);
	}
}
