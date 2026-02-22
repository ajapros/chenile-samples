package com.mycompany.myorg.vehicle.configuration.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.BodyTypeSelector;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.annotation.ChenileParamType;
import org.chenile.http.handler.ControllerSupport;
import org.springframework.http.ResponseEntity;

import org.chenile.stm.StateEntity;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.chenile.workflow.dto.StateEntityServiceResponse;
import com.mycompany.myorg.vehicle.model.Vehicle;

@RestController
@ChenileController(value = "vehicleService", serviceName = "_vehicleStateEntityService_",
		healthCheckerName = "vehicleHealthChecker")
public class VehicleController extends ControllerSupport{
	
	@GetMapping("/vehicle/{id}")
	public ResponseEntity<GenericResponse<StateEntityServiceResponse<Vehicle>>> retrieve(
			HttpServletRequest httpServletRequest,
			@PathVariable String id){
		return process(httpServletRequest,id);
	}

	@PostMapping("/vehicle")
	public ResponseEntity<GenericResponse<StateEntityServiceResponse<Vehicle>>> create(
			HttpServletRequest httpServletRequest,
			@ChenileParamType(StateEntity.class)
			@RequestBody Vehicle entity){
		return process(httpServletRequest,entity);
	}

	
	@PatchMapping("/vehicle/{id}/{eventID}")
	@BodyTypeSelector("vehicleBodyTypeSelector")
	public ResponseEntity<GenericResponse<StateEntityServiceResponse<Vehicle>>> processById(
			HttpServletRequest httpServletRequest,
			@PathVariable String id,
			@PathVariable String eventID,
			@ChenileParamType(Object.class) 
			@RequestBody String eventPayload){
		return process(httpServletRequest,id,eventID,eventPayload);
	}


}
