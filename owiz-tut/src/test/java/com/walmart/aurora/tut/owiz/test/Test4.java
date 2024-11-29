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
public class Test4 extends BaseTest{
	@Test public void testSimple() throws Exception {
		OrchExecutor<BaseContext> oe = obtainOrchExecutor("orch4.xml");
		BaseContext context = new BaseContext();
		oe.execute(context);
		System.out.println(context);
		assertEquals("commandId1 key not set correctly",context.get("commandId1"),"owiz");
		assertEquals("commandId2 key not set correctly",context.get("commandId2"),"owiz");
		assertEquals("commandId3 key not set correctly",context.get("commandId3"),"owiz");
		assertInvocationOrder(context,"commandId1","commandId2","commandId3");
	}   
}
