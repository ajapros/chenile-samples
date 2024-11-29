package org.chenile.samples.secservice.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.chenile.samples.secservice.model.SecServiceEntity;
import org.chenile.samples.secservice.service.SecService;

public class SecServiceImpl implements SecService {
    private static final Logger logger = LoggerFactory.getLogger(SecServiceImpl.class);
	@Override
	public SecServiceEntity op1(SecServiceEntity secServiceEntity) {
        logger.debug("At the op1 method in service class");
		secServiceEntity.id = "SecServiceImpl";
		return secServiceEntity;
	}
}