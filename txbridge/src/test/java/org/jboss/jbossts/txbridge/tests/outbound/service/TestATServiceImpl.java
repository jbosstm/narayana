/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.outbound.service;

import java.util.ArrayList;
import java.util.UUID;

import jakarta.jws.HandlerChain;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.servlet.annotation.WebServlet;

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