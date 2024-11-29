package com.walmart.aurora.tut.owiz.commands;

import org.chenile.owiz.Command;

import com.walmart.aurora.tut.owiz.context.BaseContext;

public class SimpleCommand implements Command<BaseContext>{

	private String commandId;
	public SimpleCommand(String commandId) {
		this.commandId = commandId;
	}
	@Override
	public void execute(BaseContext context) throws Exception {
		context.put(commandId,"owiz");
		context.commandInvocationOrder.add(commandId);
	}

}
