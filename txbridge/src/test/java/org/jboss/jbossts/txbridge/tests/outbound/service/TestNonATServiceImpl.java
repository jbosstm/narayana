/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.outbound.service;

import java.util.ArrayList;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.servlet.annotation.WebServlet;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@WebService(targetNamespace = "http://client.outbound.tests.txbridge.jbossts.jboss.org/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebServlet(name = "OutboundTestServiceNonATServlet", urlPatterns = TestNonATServiceImpl.URL_PATTERN)
public class TestNonATServiceImpl {

    public static final String URL_PATTERN = "/TestNonATServiceImpl/*";

    @WebMethod
    public void doNothing() {
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