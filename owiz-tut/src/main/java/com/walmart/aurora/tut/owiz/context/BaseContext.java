package com.walmart.aurora.tut.owiz.context;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.chenile.owiz.impl.ChainContext;
import org.chenile.owiz.impl.ChainContextContainer;

public class BaseContext extends HashMap<String,Object> implements ChainContextContainer<BaseContext>{
	private static final long serialVersionUID = 8594770021082667161L;
	public List<String> commandInvocationOrder = new ArrayList<>();
	@SuppressWarnings("unchecked")
	@Override
	public ChainContext<BaseContext> getChainContext() {
		return (ChainContext<BaseContext>) get("chainContext");
	}

	@Override
	public void setChainContext(ChainContext<BaseContext> chainContext) {
		put("chainContext", chainContext);
	}
}
