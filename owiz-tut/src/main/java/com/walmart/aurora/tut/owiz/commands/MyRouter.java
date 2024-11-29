package com.walmart.aurora.tut.owiz.commands;

import org.chenile.owiz.impl.Router;

import com.walmart.aurora.tut.owiz.context.BaseContext;

public class MyRouter extends Router<BaseContext>{

	@Override
	protected String computeRoutingString(BaseContext context) throws Exception {
		return (String)context.get("myRoute");
	}

}
