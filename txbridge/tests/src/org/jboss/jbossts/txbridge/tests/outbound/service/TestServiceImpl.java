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
import com.arjuna.mw.wst11.UserTransactionFactory;

import org.apache.log4j.Logger;

import org.jboss.jbossts.txbridge.tests.outbound.utility.TestDurableParticipant;
import org.jboss.jbossts.txbridge.tests.outbound.utility.TestVolatileParticipant;

import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * Implementation of a web service used by txbridge test cases.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-03
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
@HandlerChain(file = "jaxws-handlers-server.xml") // relative path from the class file
public class TestServiceImpl
{
    private static Logger log = Logger.getLogger(TestServiceImpl.class);

    @WebMethod
    public void doNothing()
    {
        log.trace("doNothing()");
    }

    public void enlistVolatileParticipant(int count) {
        TransactionManager tm = TransactionManagerFactory.transactionManager();
        try {
            for(int i = 0; i < count; i++) {
                TestVolatileParticipant volatileParticipant = new TestVolatileParticipant();
                tm.enlistForVolatileTwoPhase(volatileParticipant, "org.jboss.jbossts.txbridge.tests.outbound.Volatile:" + new Uid().toString());
            }
        } catch(Exception e) {
            log.error("could not enlist", e);
        }
    }

    public void enlistDurableParticipant(int count) {
        TransactionManager tm = TransactionManagerFactory.transactionManager();
        try {
            for(int i = 0; i < count; i++) {
                TestDurableParticipant durableParticipant = new TestDurableParticipant();
                tm.enlistForDurableTwoPhase(durableParticipant, TestDurableParticipant.TYPE_IDENTIFIER + new Uid().toString());
            }
        } catch(Exception e) {
            log.error("could not enlist", e);
        }
    }
}
