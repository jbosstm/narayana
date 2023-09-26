/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.inbound.service;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.transaction.TransactionManager;

import org.jboss.jbossts.txbridge.tests.inbound.utility.TestSynchronization;
import org.jboss.jbossts.txbridge.tests.inbound.utility.TestXAResource;
import org.jboss.logging.Logger;


/**
 * Implementation of a web service used by txbridge test cases.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-01
 */
@Stateless
@WebService(targetNamespace = "http://client.inbound.tests.txbridge.jbossts.jboss.org/", portName = "TestServiceImpl")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@TransactionAttribute(TransactionAttributeType.MANDATORY) // default is REQUIRED
public class TestServiceImpl {
    private static Logger log = Logger.getLogger(TestServiceImpl.class);

    @Resource(lookup = "java:/TransactionManager")
    private TransactionManager tm;

    @WebMethod
    public void doNothing() {
        log.trace("doNothing()");
    }

    @WebMethod(exclude = true)
    public void enlistSynchronization(int count) {
        try {
            for (int i = 0; i < count; i++) {
                TestSynchronization testSynchronization = new TestSynchronization();
                tm.getTransaction().registerSynchronization(testSynchronization);
            }
        } catch (Exception e) {
            log.error("could not enlist", e);
        }
    }

    @WebMethod(exclude = true)
    public void enlistXAResource(int count) {
        try {
            for (int i = 0; i < count; i++) {
                TestXAResource testXAResource = new TestXAResource();
                tm.getTransaction().enlistResource(testXAResource);
            }
        } catch (Exception e) {
            log.error("could not enlist", e);
        }
    }
}