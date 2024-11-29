package org.chenile.samples.employee.configuration.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.samples.employee.model.Employee;
import org.chenile.cloudedgeswitch.CloudEdgeSwitchConfig;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.annotation.InterceptedBy;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.mqtt.model.ChenileMqtt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@ChenileMqtt
@ChenileController(value = "employeeService", serviceName = "_employeeService_",
		healthCheckerName = "employeeHealthChecker")
public class EmployeeController extends ControllerSupport{
	
	@PostMapping("/employee")
	@InterceptedBy("cloudEdgeSwitch")
	@CloudEdgeSwitchConfig
	public ResponseEntity<GenericResponse<Employee>> save(
			HttpServletRequest httpServletRequest,
			@RequestBody Employee entity){
		return process("save",httpServletRequest,entity);
	}

	@GetMapping("/employee/{id}")
	public ResponseEntity<GenericResponse<Employee>> retrieve(
			HttpServletRequest httpServletRequest,
			@PathVariable("id") String id){
		return process("retrieve",httpServletRequest,id);
	}

}
