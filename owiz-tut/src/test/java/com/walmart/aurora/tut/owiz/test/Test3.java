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
public class Test3 extends BaseTest{
	@Test public void testSimple() throws Exception {
		OrchExecutor<BaseContext> oe = obtainOrchExecutor("orch3.xml");
		BaseContext context = new BaseContext();
		oe.execute(context);
		assertEquals("commandId1 key not set correctly",context.get("commandId1"),"owiz");
		assertInvocationOrder(context,"commandId1");
	}   
}
