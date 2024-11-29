package org.chenile.samples.s1.service.impl;

import org.chenile.base.exception.ErrorNumException;

import org.chenile.samples.s1.model.S1Entity;

public class S1T1ServiceImpl extends S1ServiceImpl{
	@Override
	public S1Entity op1(S1Entity s1Entity) {
		// Implement the service here
		if (s1Entity.id != null && s1Entity.id.equals("foo")) {
			throw new ErrorNumException(500,1235,new Object[] {s1Entity.id});
		}
		s1Entity.id = "S1T1ServiceImpl";
		return s1Entity;
	}
}
