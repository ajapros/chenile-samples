package com.walmart.aurora.tut.owiz.commands;

import org.chenile.owiz.Command;

import com.walmart.aurora.tut.owiz.context.BaseContext;

public class InterceptorCommand implements Command<BaseContext>{

	private String commandId;
	public InterceptorCommand(String commandId) {
		this.commandId = commandId;
	}
	@Override
	public void execute(BaseContext context) throws Exception {
		context.commandInvocationOrder.add( commandId + "pre");
		context.getChainContext().doContinue();
		context.commandInvocationOrder.add( commandId + "post");
	}

}
