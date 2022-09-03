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
