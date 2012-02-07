package org.jboss.narayana.txframework.functional.interfaces;

import javax.ejb.Remote;
import javax.jws.WebMethod;

/**
 * @author paul.robinson@redhat.com, 2012-02-06
 */
@Remote
public interface JAXWSHandlerAnnotation {

    @WebMethod
    public String sayHello(String arg0);

}