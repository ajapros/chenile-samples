package com.walmart.aurora.tut.owiz.commands;

import org.chenile.owiz.AttachableCommand;
import org.chenile.owiz.config.model.AttachmentDescriptor;
import org.chenile.owiz.config.model.CommandDescriptor;

import com.walmart.aurora.tut.owiz.context.BaseContext;

public class TotalCommand implements AttachableCommand<BaseContext>{

	private CommandDescriptor<BaseContext> firstCommand;
	private CommandDescriptor<BaseContext> secondCommand;
	
	@Override
	public void execute(BaseContext context) throws Exception {
		Integer total = (Integer)context.get("total");
		if (total == null) total = 0;
		if (total < 1000) {
			firstCommand.getCommand().execute(context);
		}else
			secondCommand.getCommand().execute(context);
		
	}

	@Override
	public void attachCommand(AttachmentDescriptor<BaseContext> attachmentDescriptor,
			CommandDescriptor<BaseContext> command) {
		// check if the command is a first command or a second command
		if (attachmentDescriptor.get("type").equals("first")) {
			firstCommand = command;
		}else {
			secondCommand = command;
		}
		
	}

}
