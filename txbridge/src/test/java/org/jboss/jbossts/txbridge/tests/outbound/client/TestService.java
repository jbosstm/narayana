/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.outbound.client;

import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

/**
 * Interface for a web service used by txbridge test cases. Client side version.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-03
 */
@WebService(name = "TestServiceImpl", targetNamespace = "http://client.outbound.tests.txbridge.jbossts.jboss.org/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface TestService {
    public void doNothing();
}