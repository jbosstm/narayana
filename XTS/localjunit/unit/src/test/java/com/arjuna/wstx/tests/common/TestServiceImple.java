package com.arjuna.wstx.tests.common;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@Stateless
@WebService(name = "TestService", targetNamespace = "http://arjuna.com/wstx/tests/common", serviceName = "TestServiceService", portName = "TestService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class TestServiceImple implements TestService, Serializable {

    private static final long serialVersionUID = 1L;

    private static final AtomicInteger counter = new AtomicInteger();

    @Override
    public void increment() {
        counter.incrementAndGet();
    }

    @Override
    public int getCounter() {
        return counter.get();
    }

    @Override
    public void reset() {
        counter.set(0);
    }

}