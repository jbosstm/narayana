/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.jta.cdi.transactionScoped;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.transaction.TransactionScoped;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author paul.robinson@redhat.com 01/05/2013
 */
@Named("TestCDITransactionScopeBean2")
@TransactionScoped
public class TestCDITransactionScopeBean2 implements Serializable {

    private static AtomicInteger preDestroyCnt = new AtomicInteger(0);

    private boolean postConstructCalled;

    private int value = 0;

    @PostConstruct
    public void postConstruct() {
        postConstructCalled = true;
    }
    public int getValue() {

        return value;
    }

    public void setValue(int value) {

        this.value = value;
    }

    public boolean isPostConstructCalled() {
        return postConstructCalled;
    }

    public static int getPreDestroyCnt() {
        return preDestroyCnt.get();
    }

    @PreDestroy
    public void preDestroy() {
        preDestroyCnt.incrementAndGet();
    }
}
