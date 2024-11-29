package com.walmart.aurora.tut.owiz.commands;

import org.chenile.owiz.impl.CommandBase;

import com.walmart.aurora.tut.owiz.context.BaseContext;

public class OrchAwareCommand extends CommandBase<BaseContext>{
	@Override
	protected void doExecute(BaseContext context) throws Exception {
		String commandId = getConfigValue("commandId");
		context.put(commandId, "owiz");
		context.commandInvocationOrder.add(commandId);
	}

}
