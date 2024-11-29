package org.chenile.configuration.samples.orders;

import org.chenile.samples.order.MockSS;
import org.chenile.stm.impl.BeanFactoryAdapter;
import org.chenile.stm.impl.STMActionsInfoProvider;
import org.chenile.stm.impl.STMFlowStoreImpl;
import org.chenile.stm.impl.XmlFlowReader;
import org.chenile.stm.spring.SpringBeanFactoryAdapter;
import org.chenile.workflow.api.WorkflowRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderConfiguration {
    public static final String FLOW_DEFINITION_FILE = "org/chenile/samples/query/workflow/order-states.xml";

    @Bean BeanFactoryAdapter orderBeanFactoryAdapter() {
        return new SpringBeanFactoryAdapter();
    }
    @Bean STMFlowStoreImpl orderFlowStore(@Qualifier("orderBeanFactoryAdapter") BeanFactoryAdapter orderBeanFactoryAdapter) throws Exception{
        STMFlowStoreImpl stmFlowStore = new STMFlowStoreImpl();
        stmFlowStore.setBeanFactory(orderBeanFactoryAdapter);
        return stmFlowStore;
    }

    @Bean
    XmlFlowReader orderFlowReader(@Qualifier("orderFlowStore") STMFlowStoreImpl flowStore) throws Exception {
        XmlFlowReader flowReader = new XmlFlowReader(flowStore);
        flowReader.setFilename(FLOW_DEFINITION_FILE);
        return flowReader;
    }

    @Bean @Autowired
    STMActionsInfoProvider orderActionsInfoProvider(@Qualifier("orderFlowStore") STMFlowStoreImpl stmFlowStore) {
        STMActionsInfoProvider provider = new STMActionsInfoProvider(stmFlowStore);
        WorkflowRegistry.addSTMActionsInfoProvider("order",provider);
        return provider;
    }

    @Bean
    MockSS mockSS() {
        return new MockSS();
    }


}
