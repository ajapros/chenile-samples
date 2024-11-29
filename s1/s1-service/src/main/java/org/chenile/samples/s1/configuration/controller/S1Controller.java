package org.chenile.samples.s1.configuration.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.samples.s1.model.S1Entity;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.handler.ControllerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ChenileController(value = "s1Service", serviceName = "_s1Service_",
		healthCheckerName = "s1HealthChecker")
public class S1Controller extends ControllerSupport{


	@PostMapping("/s1/op1")
	public ResponseEntity<GenericResponse<S1Entity>> op1(
			HttpServletRequest httpServletRequest,
			@RequestBody S1Entity entity){
		return process("op1",httpServletRequest,entity);	
	}

}
