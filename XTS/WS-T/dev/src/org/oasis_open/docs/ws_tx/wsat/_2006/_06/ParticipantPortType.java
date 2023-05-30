/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package org.oasis_open.docs.ws_tx.wsat._2006._06;

import org.xmlsoap.schemas.soap.envelope.Fault;

import jakarta.jws.Oneway;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.bind.annotation.XmlSeeAlso;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.2-hudson-182-RC1
 * Generated source version: 2.1
 * 
 */
@WebService(name = "ParticipantPortType", targetNamespace = "http://docs.oasis-open.org/ws-tx/wsat/2006/06")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({
    ObjectFactory.class
})
public interface ParticipantPortType {


    /**
     * 
     * @param parameters
     */
    @WebMethod(operationName = "PrepareOperation", action = "http://docs.oasis-open.org/ws-tx/wsat/2006/06/Prepare")
    @Oneway
    public void prepareOperation(
        @WebParam(name = "Prepare", targetNamespace = "http://docs.oasis-open.org/ws-tx/wsat/2006/06", partName = "parameters")
        Notification parameters);

    /**
     * 
     * @param parameters
     */
    @WebMethod(operationName = "CommitOperation", action = "http://docs.oasis-open.org/ws-tx/wsat/2006/06/Commit")
    @Oneway
    public void commitOperation(
        @WebParam(name = "Commit", targetNamespace = "http://docs.oasis-open.org/ws-tx/wsat/2006/06", partName = "parameters")
        Notification parameters);

    /**
     *
     * @param parameters
     */
    @WebMethod(operationName = "RollbackOperation", action = "http://docs.oasis-open.org/ws-tx/wsat/2006/06/Rollback")
    @Oneway
    public void rollbackOperation(
        @WebParam(name = "Rollback", targetNamespace = "http://docs.oasis-open.org/ws-tx/wsat/2006/06", partName = "parameters")
        Notification parameters);

    /**
     *
     * @param parameters
     */
    @WebMethod(operationName = "SoapFault", action = "http://docs.oasis-open.org/ws-tx/wsat/2006/06/fault")
    @Oneway
    public void soapFault(
        @WebParam(name = "Fault", targetNamespace = "http://schemas.xmlsoap.org/soap/envelope/", partName = "parameters")
        Fault parameters);
}
