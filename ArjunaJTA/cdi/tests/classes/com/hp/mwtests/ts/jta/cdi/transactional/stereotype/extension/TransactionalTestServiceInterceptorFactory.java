/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.cdi.transactional.stereotype.extension;

import com.arjuna.ats.jta.TransactionManager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InterceptionFactory;

public class TransactionalTestServiceInterceptorFactory {
    public interface TestService {
        int doTransactional() throws Exception;
    }

    @Produces
    @ApplicationScoped
    public TestService testServiceProducer(InterceptionFactory<TestService> interceptionFactory) {
        interceptionFactory.configure().add(new TransactionalLiteral());
        return interceptionFactory.createInterceptedInstance(()
                -> TransactionManager.transactionManager().getStatus());
    }
}