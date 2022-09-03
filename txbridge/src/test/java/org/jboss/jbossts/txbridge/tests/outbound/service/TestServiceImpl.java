/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010,
 * @author JBoss, by Red Hat.
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
