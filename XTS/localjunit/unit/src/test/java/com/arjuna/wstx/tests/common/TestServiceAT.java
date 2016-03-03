package com.arjuna.wstx.tests.common;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * Web service which understands WS-AT.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@WebService(name = "TestServiceAT", targetNamespace = "http://arjuna.com/wstx/tests/common")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface TestServiceAT {

    @WebMethod
    void increment();

    @WebMethod
    int getCounter();

    @WebMethod
    List<String> getTwoPhaseCommitInvocations();

    @WebMethod
    void reset();

}
