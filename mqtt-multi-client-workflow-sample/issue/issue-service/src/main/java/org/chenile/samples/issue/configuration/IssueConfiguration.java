package org.chenile.samples.issue.configuration;

import org.chenile.samples.issue.model.CompleteTaskPayload;
import org.chenile.samples.issue.service.cmds.*;
import org.chenile.stm.STM;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.impl.BeanFactoryAdapter;
import org.chenile.stm.impl.STMActionsInfoProvider;
import org.chenile.stm.impl.STMFlowStoreImpl;
import org.chenile.stm.impl.STMImpl;
import org.chenile.stm.impl.XmlFlowReader;
import org.chenile.stm.spring.SpringBeanFactoryAdapter;
import org.chenile.workflow.dto.StateEntityServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import org.chenile.utils.entity.service.EntityStore;
import org.chenile.workflow.service.impl.StateEntityServiceImpl;
import org.chenile.workflow.service.stmcmds.BaseTransitionAction;
import org.chenile.workflow.service.stmcmds.GenericEntryAction;
import org.chenile.workflow.service.stmcmds.GenericExitAction;
import org.chenile.workflow.service.stmcmds.StmBodyTypeSelector;
import org.chenile.samples.issue.model.Issue;
import org.chenile.samples.issue.service.healthcheck.IssueHealthChecker;
import org.chenile.samples.issue.service.store.IssueEntityStore;

/**
 This is where you will instantiate all the required classes in Spring

*/
@Configuration
public class IssueConfiguration {
	private static final String FLOW_DEFINITION_FILE = "org/chenile/samples/issue/states.xml";
	
	@Bean BeanFactoryAdapter issueBeanFactoryAdapter() {
		return new SpringBeanFactoryAdapter();
	}
	
	@Bean STMFlowStoreImpl issueFlowStore(@Qualifier("issueBeanFactoryAdapter") BeanFactoryAdapter issueBeanFactoryAdapter) throws Exception{
		STMFlowStoreImpl stmFlowStore = new STMFlowStoreImpl();
		stmFlowStore.setBeanFactory(issueBeanFactoryAdapter);
		return stmFlowStore;
	}
	
	@Bean @Autowired STM<Issue> issueEntityStm(@Qualifier("issueFlowStore") STMFlowStoreImpl stmFlowStore) throws Exception{
		STMImpl<Issue> stm = new STMImpl<>();		
		stm.setStmFlowStore(stmFlowStore);
		return stm;
	}
	
	@Bean @Autowired STMActionsInfoProvider issueActionsInfoProvider(@Qualifier("issueFlowStore") STMFlowStoreImpl stmFlowStore) {
		return new STMActionsInfoProvider(stmFlowStore);
	}
	
	@Bean // @Profile("!unittest")
	EntityStore<Issue> issueEntityStore() {
		return new IssueEntityStore();
	}
	
	@Bean @Autowired StateEntityServiceImpl<Issue> _issueStateEntityService_(
			@Qualifier("issueEntityStm") STM<Issue> stm,
			@Qualifier("issueActionsInfoProvider") STMActionsInfoProvider issueInfoProvider,
			@Qualifier("issueEntityStore") EntityStore<Issue> entityStore){
		return new StateEntityServiceImpl<>(stm, issueInfoProvider, entityStore);
	}
	
	// Now we start constructing the STM Components 
	
	@Bean @Autowired GenericEntryAction<Issue> issueEntryAction(@Qualifier("issueEntityStore") EntityStore<Issue> entityStore,
			@Qualifier("issueActionsInfoProvider") STMActionsInfoProvider issueInfoProvider){
		return new GenericEntryAction<Issue>(entityStore,issueInfoProvider);
	}
	
	@Bean GenericExitAction<Issue> issueExitAction(){
		return new GenericExitAction<Issue>();
	}
	
	@Bean @Autowired StmBodyTypeSelector issueBodyTypeSelector(@Qualifier("issueActionsInfoProvider") STMActionsInfoProvider issueInfoProvider) {
		return new StmBodyTypeSelector(issueInfoProvider);
	}
	
	@Bean @Autowired STMTransitionAction<Issue> issueBaseTransitionAction(){
		return new BaseTransitionAction<>();
	}
	
	@Bean AssignIssueAction assignIssue() {
		return new AssignIssueAction();
	}
	
	@Bean ResolveIssueAction resolveIssue() {
		return new ResolveIssueAction();
	}
	
	@Bean CloseIssueAction closeIssue() {
		return new CloseIssueAction();
	}

	@Bean
	XmlFlowReader issueFlowReader(@Qualifier("issueFlowStore") STMFlowStoreImpl flowStore) throws Exception {
		XmlFlowReader flowReader = new XmlFlowReader(flowStore);
		flowReader.setFilename(FLOW_DEFINITION_FILE);
		return flowReader;
	}
	

	@Bean IssueHealthChecker issueHealthChecker(){
    	return new IssueHealthChecker();
    }
	@Bean
	AddTask addTask(){
		return new AddTask();
	}
	@Bean
	CompleteTask completeTask(){
		return new CompleteTask();
	}
	@Bean
	CheckIfAllTasksComplete checkIfAllTasksComplete(){
		return new CheckIfAllTasksComplete();
	}
}
