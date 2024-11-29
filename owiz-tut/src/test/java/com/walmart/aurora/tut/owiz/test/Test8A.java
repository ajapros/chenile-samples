package com.walmart.aurora.tut.owiz.test;

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
public class Test8A extends BaseTest{
	@Test public void testFilterChain() throws Exception {
		OrchExecutor<BaseContext> oe = obtainOrchExecutor("orch8-a.xml");
		BaseContext context = new BaseContext();
		oe.execute(context);
		assertInvocationOrder(context,"interceptor2pre", "interceptor1pre", 
				"interceptor3pre", "interceptor3post", "interceptor1post", "interceptor2post");
	} 
}
