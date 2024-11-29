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
public class Test7 extends BaseTest{
	
	@Test public void testRoute1() throws Exception {
		OrchExecutor<BaseContext> oe = obtainOrchExecutor("orch7.xml");
		BaseContext context = new BaseContext();
		context.put("myRoute", "route1");
		oe.execute(context);
		System.out.println(context);
		assertEquals("commandId1 key not set correctly",context.get("commandId1"),"owiz");
		assertInvocationOrder(context,"commandId1");
	}  
	@Test public void testRoute2() throws Exception {
		OrchExecutor<BaseContext> oe = obtainOrchExecutor("orch5.xml");
		BaseContext context = new BaseContext();
		context.put("myRoute", "route2");
		oe.execute(context);
		System.out.println(context);
		assertEquals("commandId2 key not set correctly",context.get("commandId2"),"owiz");
		assertInvocationOrder(context,"commandId2");
	}   
	@Test public void testRoute3() throws Exception {
		OrchExecutor<BaseContext> oe = obtainOrchExecutor("orch5.xml");
		BaseContext context = new BaseContext();
		context.put("myRoute", "route3");
		oe.execute(context);
		System.out.println(context);
		assertEquals("commandId3 key not set correctly",context.get("commandId3"),"owiz");
		assertInvocationOrder(context,"commandId3");
	}   
}
