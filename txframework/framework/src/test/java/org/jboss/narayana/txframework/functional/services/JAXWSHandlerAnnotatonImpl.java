package org.jboss.narayana.txframework.functional.services;

import org.jboss.narayana.txframework.functional.interfaces.JAXWSHandlerAnnotation;

import javax.ejb.Stateless;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * @author paul.robinson@redhat.com, 2012-02-06
 */
@Stateless
@WebService(serviceName = "JAXWSHandlerAnnotatonService", portName = "JAXWSHandlerAnnotatonPort",
        name = "JAXWSHandlerAnnotatonImpl", targetNamespace = "http://www.jboss.com/functional/JAXWSHandlerAnnotatonImpl")
@HandlerChain(file = "/jaxws-handlers-jaxws-service.xml", name = "Context Handlers")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class JAXWSHandlerAnnotatonImpl implements JAXWSHandlerAnnotation {

    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
