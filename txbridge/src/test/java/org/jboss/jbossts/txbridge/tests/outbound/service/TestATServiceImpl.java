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

import java.util.ArrayList;
import java.util.UUID;

import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.servlet.annotation.WebServlet;

import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst11.TransactionManagerFactory;
import com.arjuna.wst.UnknownTransactionException;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@WebService(targetNamespace = "http://client.outbound.tests.txbridge.jbossts.jboss.org/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@HandlerChain(file = "/jaxws-handlers-server.xml")
@WebServlet(name = "OutboundTestServiceATServlet", urlPatterns = TestATServiceImpl.URL_PATTERN)
public class TestATServiceImpl {

    public static final String URL_PATTERN = "/TestATServiceImpl/*";

    @WebMethod
    public void doNothing() {
        try {
            TransactionManager transactionManager = TransactionManagerFactory.transactionManager();
            transactionManager.enlistForDurableTwoPhase(new TestATServiceParticipant(), "testServiceAT:" + UUID.randomUUID());
        } catch (UnknownTransactionException e) {
            // Ignore if no transaction
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod
    public ArrayList<String> getTwoPhaseCommitInvocations() {
        return TestATServiceParticipant.getTwoPhaseCommitInvocations();
    }

    @WebMethod
    public void reset() {
        TestATServiceParticipant.resetTwoPhaseCommitInvocations();
    }

}
