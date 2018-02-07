
package org.oasis_open.docs.ws_tx.wscoor._2006._06;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebService(name = "RegistrationResponsePortType", targetNamespace = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({
    org.oasis_open.docs.ws_tx.wscoor._2006._06.ObjectFactory.class
})
public interface RegistrationResponsePortType {


    /**
     * 
     * @param parameters
     */
    @WebMethod(operationName = "RegisterResponseOperation", action = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06/RegisterResponse")
    @Oneway
    public void registerResponseOperation(
        @WebParam(name = "RegisterResponse", targetNamespace = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06", partName = "parameters")
        RegisterResponseType parameters);

}
