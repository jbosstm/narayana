package com.hp.mwtests.ts.jta.cdi.transactionScoped;

import javax.inject.Named;
import javax.transaction.TransactionScoped;
import java.io.Serializable;

/**
 * @author paul.robinson@redhat.com 01/05/2013
 */
@Named("TestCDITransactionScopeBean")
@TransactionScoped
public class TestCDITransactionScopeBean implements Serializable {

    private int value = 0;

    public int getValue() {

        return value;
    }

    public void setValue(int value) {

        this.value = value;
    }
}