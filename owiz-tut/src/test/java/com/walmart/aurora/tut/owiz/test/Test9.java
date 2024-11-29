package com.walmart.aurora.tut.owiz.test;

import static org.junit.Assert.assertEquals;

import org.chenile.owiz.OrchExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.walmart.aurora.tut.owiz.context.BaseContext;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringConfig.class)
@ActiveProfiles("unittest")
public class Test9 extends BaseTest{
	@Test public void testFirst() throws Exception {
		OrchExecutor<BaseContext> oe = obtainOrchExecutor("orch9.xml");
		BaseContext context = new BaseContext();
		context.put("total", 800);
		oe.execute(context);
		assertEquals("commandId1 key not set correctly",context.get("commandId1"),"owiz");
		assertInvocationOrder(context,"commandId1");
	}  
	
	@Test public void testSecond() throws Exception {
		OrchExecutor<BaseContext> oe = obtainOrchExecutor("orch9.xml");
		BaseContext context = new BaseContext();
		context.put("total", 1500);
		oe.execute(context);
		assertEquals("commandId2 key not set correctly",context.get("commandId2"),"owiz");
		assertInvocationOrder(context,"commandId2");
	} 
}
