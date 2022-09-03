package com.arjuna.wstx.tests.common;

import java.util.List;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

/**
 * Web service which understands WS-BA.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@WebService(name = "TestServiceBA", targetNamespace = "http://arjuna.com/wstx/tests/common")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface TestServiceBA {

    @WebMethod
    void increment();

    @WebMethod
    int getCounter();

    @WebMethod
    List<String> getBusinessActivityInvocations();

    @WebMethod
    void reset();

}
