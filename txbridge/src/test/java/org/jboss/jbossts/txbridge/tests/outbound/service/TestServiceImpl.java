/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.outbound.service;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst11.TransactionManagerFactory;

import org.jboss.logging.Logger;

import org.jboss.jbossts.txbridge.tests.outbound.utility.TestDurableParticipant;
import org.jboss.jbossts.txbridge.tests.outbound.utility.TestVolatileParticipant;

import jakarta.jws.HandlerChain;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.servlet.annotation.WebServlet;

/**
 * Implementation of a web service used by txbridge test cases.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-03
 */
@WebService(targetNamespace = "http://client.outbound.tests.txbridge.jbossts.jboss.org/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@HandlerChain(file = "/jaxws-handlers-server.xml") // relative path from the class file
@WebServlet(name="OutboundTestServiceServlet", urlPatterns=TestServiceImpl.URL_PATTERN)
public class TestServiceImpl {
    private static Logger log = Logger.getLogger(TestServiceImpl.class);

    public static final String URL_PATTERN = "/TestServiceImpl/*";

    @WebMethod
    public void doNothing() {
        log.trace("doNothing()");
    }

    public void enlistVolatileParticipant(int count) {
        log.tracef("enlisting %s of %s", count, TestVolatileParticipant.class.getName());
        TransactionManager tm = TransactionManagerFactory.transactionManager();
        try {
            for (int i = 0; i < count; i++) {
                TestVolatileParticipant volatileParticipant = new TestVolatileParticipant();
                tm.enlistForVolatileTwoPhase(volatileParticipant, "org.jboss.jbossts.txbridge.tests.outbound.Volatile:" + new Uid().toString());
            }
        } catch (Exception e) {
            log.error("could not enlist", e);
        }
    }

    public void enlistDurableParticipant(int count) {
        log.tracef("enlisting %s of %s", count, TestDurableParticipant.class.getName());
        TransactionManager tm = TransactionManagerFactory.transactionManager();
        try {
            for (int i = 0; i < count; i++) {
                TestDurableParticipant durableParticipant = new TestDurableParticipant();
                tm.enlistForDurableTwoPhase(durableParticipant, TestDurableParticipant.TYPE_IDENTIFIER + new Uid().toString());
            }
        } catch (Exception e) {
            log.error("could not enlist", e);
        }
    }
}