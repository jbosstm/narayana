/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.outbound.client;

import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

@WebService(name = "TestATServiceImpl", targetNamespace = "http://client.outbound.tests.txbridge.jbossts.jboss.org/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface TestATService extends CommonTestService {

}