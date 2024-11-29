package org.chenile.samples.s1.service.impl;

import org.chenile.base.exception.BadRequestException;
import org.chenile.base.exception.ErrorNumException;
import org.springframework.beans.factory.annotation.Autowired;

import org.chenile.samples.s1.model.S1Entity;
import org.chenile.samples.s1.service.S1Service;

public class S1ServiceImpl implements S1Service{
	@Override
	public S1Entity op1(S1Entity s1Entity) {
		// Implement the service here
		if (s1Entity.id != null && s1Entity.id.equals("foo")) {
			throw new ErrorNumException(500,1234,new Object[] {s1Entity.id});
		}
		s1Entity.id = "S1ServiceImpl";
		return s1Entity;
	}
	
	

}
