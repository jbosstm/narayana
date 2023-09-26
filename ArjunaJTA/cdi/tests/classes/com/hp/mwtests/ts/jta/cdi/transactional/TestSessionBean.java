/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.cdi.transactional;


import jakarta.ejb.Stateful;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

/**
 * @author paul.robinson@redhat.com 28/05/2013
 */
@Stateful
public class TestSessionBean {

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void invokeWithRequired() {

    }

}