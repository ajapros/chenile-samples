package org.chenile.samples.secservice.configuration.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.InterceptedBy;
import org.chenile.samples.secservice.model.SecServiceEntity;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.security.model.SecurityConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ChenileController(value = "secService", serviceName = "_secService_",
		healthCheckerName = "secServiceHealthChecker")
public class SecServiceController extends ControllerSupport{
	
	@PostMapping("/sec/premium")
	@InterceptedBy("securityInterceptor")
	@SecurityConfig(authorities = {"test.premium"})
	public ResponseEntity<GenericResponse<SecServiceEntity>> op1(
			HttpServletRequest httpServletRequest,
			@RequestBody SecServiceEntity entity){
		return process("op1",httpServletRequest,entity);	
	}
}
