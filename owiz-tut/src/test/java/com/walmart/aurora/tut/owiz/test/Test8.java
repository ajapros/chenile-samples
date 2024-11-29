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
public class Test8 extends BaseTest{
	@Test public void testFilterChain() throws Exception {
		OrchExecutor<BaseContext> oe = obtainOrchExecutor("orch8.xml");
		BaseContext context = new BaseContext();
		oe.execute(context);
		assertInvocationOrder(context,"interceptor1pre", "interceptor2pre", 
				"interceptor3pre", "interceptor3post", "interceptor2post", "interceptor1post");
	}  
}
