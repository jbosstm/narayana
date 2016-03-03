package com.arjuna.wstx.tests.common;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * Web service which does not understand neither WS-AT nor WS-BA
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@WebService(name = "TestService", targetNamespace = "http://arjuna.com/wstx/tests/common")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface TestService {

    @WebMethod
    void increment();

    @WebMethod
    int getCounter();

    @WebMethod
    void reset();

}
