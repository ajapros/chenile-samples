package com.walmart.aurora.tut.owiz.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.chenile.owiz.OrchExecutor;
import org.chenile.owiz.config.impl.XmlOrchConfigurator;
import org.chenile.owiz.impl.OrchExecutorImpl;
import org.chenile.owiz.impl.OwizSpringFactoryAdapter;
import org.springframework.beans.factory.annotation.Autowired;

import com.walmart.aurora.tut.owiz.context.BaseContext;

public class BaseTest {
	@Autowired OwizSpringFactoryAdapter owizSpringFactoryAdapter;
	 public OrchExecutor<BaseContext> obtainOrchExecutor(String ...files){
		// Initialize for a context called BaseContext which is shown below
		XmlOrchConfigurator<BaseContext> xoc = new XmlOrchConfigurator<BaseContext>();
		xoc.setBeanFactoryAdapter(owizSpringFactoryAdapter);
		for (String file: files) {
			xoc.setFilename(file);
		}

		OrchExecutorImpl<BaseContext> orchExecutor = new OrchExecutorImpl<BaseContext>();
		orchExecutor.setOrchConfigurator(xoc);
		return orchExecutor;
	}
	// asserts if the commands have been invoked in the same order as expected
	public void assertInvocationOrder(BaseContext context,String ...args) {
		List<String> list = Arrays.asList(args);
		assertEquals("Invocation order does not match expected invocation order",
				list,context.commandInvocationOrder);
	}
}
