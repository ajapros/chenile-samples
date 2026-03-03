package com.mycompany.myorg.vehicle.configuration;

import org.chenile.core.context.HeaderUtils;
import org.chenile.stm.*;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.impl.*;
import org.chenile.stm.spring.SpringBeanFactoryAdapter;
import org.chenile.workflow.param.MinimalPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.chenile.utils.entity.service.EntityStore;
import org.chenile.workflow.service.impl.StateEntityServiceImpl;
import org.chenile.workflow.service.stmcmds.*;
import com.mycompany.myorg.vehicle.model.Vehicle;
import com.mycompany.myorg.vehicle.service.cmds.AssignVehicleAction;
import com.mycompany.myorg.vehicle.service.cmds.DefaultSTMTransitionAction;
import com.mycompany.myorg.vehicle.service.cmds.CloseVehicleAction;
import com.mycompany.myorg.vehicle.service.cmds.ResolveVehicleAction;
import com.mycompany.myorg.vehicle.service.healthcheck.VehicleHealthChecker;
import com.mycompany.myorg.vehicle.service.store.VehicleEntityStore;
import org.chenile.workflow.api.WorkflowRegistry;

/**
 This is where you will instantiate all the required classes in Spring
*/
@Configuration
public class VehicleConfiguration {
	private static final String FLOW_DEFINITION_FILE = "com/mycompany/myorg/vehicle/vehicle-states.xml";
	
	@Bean BeanFactoryAdapter vehicleBeanFactoryAdapter() {
		return new SpringBeanFactoryAdapter();
	}
	
	@Bean STMFlowStoreImpl vehicleFlowStore(@Qualifier("vehicleBeanFactoryAdapter") BeanFactoryAdapter vehicleBeanFactoryAdapter) throws Exception{
		STMFlowStoreImpl stmFlowStore = new STMFlowStoreImpl();
		stmFlowStore.setBeanFactory(vehicleBeanFactoryAdapter);
		return stmFlowStore;
	}
	
	@Bean  STM<Vehicle> vehicleEntityStm(@Qualifier("vehicleFlowStore") STMFlowStoreImpl stmFlowStore) throws Exception{
		STMImpl<Vehicle> stm = new STMImpl<>();		
		stm.setStmFlowStore(stmFlowStore);
		return stm;
	}
	
	@Bean  STMActionsInfoProvider vehicleActionsInfoProvider(@Qualifier("vehicleFlowStore") STMFlowStoreImpl stmFlowStore) {
		STMActionsInfoProvider provider =  new STMActionsInfoProvider(stmFlowStore);
        WorkflowRegistry.addSTMActionsInfoProvider("vehicle",provider);
        return provider;
	}
	
	@Bean EntityStore<Vehicle> vehicleEntityStore() {
		return new VehicleEntityStore();
	}
	
	@Bean  StateEntityServiceImpl<Vehicle> _vehicleStateEntityService_(
			@Qualifier("vehicleEntityStm") STM<Vehicle> stm,
			@Qualifier("vehicleActionsInfoProvider") STMActionsInfoProvider vehicleInfoProvider,
			@Qualifier("vehicleEntityStore") EntityStore<Vehicle> entityStore){
		return new StateEntityServiceImpl<>(stm, vehicleInfoProvider, entityStore);
	}
	
	// Now we start constructing the STM Components 
	
	@Bean  GenericEntryAction<Vehicle> vehicleEntryAction(@Qualifier("vehicleEntityStore") EntityStore<Vehicle> entityStore,
			@Qualifier("vehicleActionsInfoProvider") STMActionsInfoProvider vehicleInfoProvider){
		return new GenericEntryAction<Vehicle>(entityStore,vehicleInfoProvider);
	}
	
	@Bean GenericExitAction<Vehicle> vehicleExitAction(){
		return new GenericExitAction<Vehicle>();
	}

	@Bean
	XmlFlowReader vehicleFlowReader(@Qualifier("vehicleFlowStore") STMFlowStoreImpl flowStore) throws Exception {
		XmlFlowReader flowReader = new XmlFlowReader(flowStore);
		flowReader.setFilename(FLOW_DEFINITION_FILE);
		return flowReader;
	}
	

	@Bean VehicleHealthChecker vehicleHealthChecker(){
    	return new VehicleHealthChecker();
    }

    @Bean STMTransitionAction<Vehicle> defaultvehicleSTMTransitionAction() {
        return new DefaultSTMTransitionAction<MinimalPayload>();
    }

    @Bean
    STMTransitionActionResolver vehicleTransitionActionResolver(
    @Qualifier("defaultvehicleSTMTransitionAction") STMTransitionAction<Vehicle> defaultSTMTransitionAction){
        return new STMTransitionActionResolver("vehicle",defaultSTMTransitionAction, HeaderUtils.TENANT_ID_KEY.replace("x-",""));
    }

    @Bean  StmBodyTypeSelector vehicleBodyTypeSelector(
    @Qualifier("vehicleActionsInfoProvider") STMActionsInfoProvider vehicleInfoProvider,
    @Qualifier("vehicleTransitionActionResolver") STMTransitionActionResolver stmTransitionActionResolver) {
        return new StmBodyTypeSelector(vehicleInfoProvider,stmTransitionActionResolver);
    }

    @Bean  STMTransitionAction<Vehicle> vehicleBaseTransitionAction(
    @Qualifier("vehicleTransitionActionResolver") STMTransitionActionResolver stmTransitionActionResolver){
        return new BaseTransitionAction<>(stmTransitionActionResolver);
    }


    // Create the specific transition actions here. Make sure that these actions are inheriting from
    // AbstractSTMTransitionMachine (The sample classes provide an example of this). To automatically wire
    // them into the STM use the convention of "vehicle" + eventId for the method name. (vehicle is the
    // prefix passed to the TransitionActionResolver above.)
    // This will ensure that these are detected automatically by the Workflow system.
    // The payload types will be detected as well so that there is no need to introduce an <event-information/>
    // segment in src/main/resources/com/mycompany/vehicle/vehicle-states.xml

    @Bean ResolveVehicleAction vehicleResolve() {
        return new ResolveVehicleAction();
    }

    @Bean CloseVehicleAction vehicleClose() {
        return new CloseVehicleAction();
    }

    @Bean AssignVehicleAction vehicleAssign() {
        return new AssignVehicleAction();
    }


}
