package org.chenile.samples.employee.model;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

public class EmployeeIdGenerator implements IdentifierGenerator, Configurable {
    private static int counter = 1;
    @Override
    public Serializable generate(
            SharedSessionContractImplementor session, Object obj)
            throws HibernateException {

        return counter++ + "";
    }
}
