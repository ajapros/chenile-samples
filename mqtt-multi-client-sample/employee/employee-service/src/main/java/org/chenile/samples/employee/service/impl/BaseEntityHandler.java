package org.chenile.samples.employee.service.impl;

import org.chenile.samples.employee.model.Employee;
import org.chenile.core.context.ContextContainer;
import org.chenile.core.context.HeaderUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseEntityHandler {

    @Autowired
    ContextContainer contextContainer;
    public void onBeforeSave(Employee entity) {
        if (entity.tenant == null){
            entity.tenant = contextContainer.get(HeaderUtils.TENANT_ID_KEY);
        }
        if (entity.createdBy == null){
            entity.createdBy = contextContainer.get(HeaderUtils.EMPLOYEE_ID_KEY);
        }
    }

}
