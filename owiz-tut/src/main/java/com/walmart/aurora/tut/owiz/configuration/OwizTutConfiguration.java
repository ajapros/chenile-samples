package com.walmart.aurora.tut.owiz.configuration;

import org.chenile.owiz.impl.FilterChain;
import org.chenile.owiz.impl.OwizSpringFactoryAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.walmart.aurora.tut.owiz.commands.InterceptorCommand;
import com.walmart.aurora.tut.owiz.commands.MyRouter;
import com.walmart.aurora.tut.owiz.commands.OrchAwareCommand;
import com.walmart.aurora.tut.owiz.commands.SimpleCommand;
import com.walmart.aurora.tut.owiz.commands.TotalCommand;
import com.walmart.aurora.tut.owiz.context.BaseContext;


@Configuration
public class OwizTutConfiguration {
	@Autowired ApplicationContext applicationContext;
	
	@Bean public SimpleCommand simpleCommand1(){
		return new SimpleCommand("commandId1");
	}
	@Bean public SimpleCommand simpleCommand2(){
		return new SimpleCommand("commandId2");
	}
	@Bean public SimpleCommand simpleCommand3(){
		return new SimpleCommand("commandId3");
	}
	
	@Bean public OwizSpringFactoryAdapter owizSpringFactoryAdapter(){
		return new OwizSpringFactoryAdapter();
	}
	
	@Bean public MyRouter myRouter() {
		return new MyRouter();
	}
	
	@Bean public FilterChain<BaseContext> filterChain(){
		return new FilterChain<>();
	}
	
	@Bean public InterceptorCommand interceptor1(){
		return new InterceptorCommand("interceptor1");
	}
	@Bean public InterceptorCommand interceptor2(){
		return new InterceptorCommand("interceptor2");
	}
	@Bean public InterceptorCommand interceptor3(){
		return new InterceptorCommand("interceptor3");
	}
	
	@Bean public TotalCommand total() {
		return new TotalCommand();
	}
	
	@Bean public OrchAwareCommand orchAwareCommand() {
		return new OrchAwareCommand();
	}
	
}
