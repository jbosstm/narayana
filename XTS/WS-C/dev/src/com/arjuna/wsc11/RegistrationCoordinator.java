/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.wsc11;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wscoor.client.WSCOORClient;
import com.arjuna.wsc.*;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterResponseType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegistrationPortType;

import javax.xml.namespace.QName;
import jakarta.xml.soap.Detail;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.soap.SOAPFaultException;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.jboss.jbossts.xts.environment.WSCEnvironmentBean;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;
import org.xmlsoap.schemas.soap.envelope.Fault;

/**
 * Wrapper around low level Registration Coordinator messaging.
 * @author kevin
 */
public class RegistrationCoordinator
{
    /**
     * Register the participant in the protocol.
     * @param coordinationContext The current coordination context
     * @param messageID The messageID to use.
     * @param participantProtocolService The participant protocol service.
     * @param protocolIdentifier The protocol identifier.
     * @return The endpoint reference of the coordinator protocol service.
     * @throws com.arjuna.wsc.InvalidProtocolException If the protocol is unsupported.
     * @throws com.arjuna.wsc.InvalidStateException If the state is invalid
     * @throws com.arjuna.webservices.SoapFault for errors during processing.
     */
    public static W3CEndpointReference register(final CoordinationContextType coordinationContext,
            final String messageID, final W3CEndpointReference participantProtocolService,
            final String protocolIdentifier)
            throws CannotRegisterException, InvalidProtocolException,
            InvalidStateException, SoapFault
    {
        final W3CEndpointReference endpointReference = coordinationContext.getRegistrationService() ;

        try
        {
            final RegisterType registerType = new RegisterType();
            registerType.setProtocolIdentifier(protocolIdentifier);
            registerType.setParticipantProtocolService(participantProtocolService);
            final RegistrationPortType port = WSCOORClient.getRegistrationPort(endpointReference, CoordinationConstants.WSCOOR_ACTION_REGISTER, messageID);
            final RegisterResponseType response = registerOperation(messageID, port, registerType);
            return response.getCoordinatorProtocolService();
        } catch (SOAPFaultException sfe) {
            final SOAPFault soapFault = sfe.getFault();
            final Detail detail = soapFault.getDetail();
            String message = (detail != null ? detail.getTextContent() : soapFault.getFaultString());
            throwException(soapFault.getFaultCodeAsQName(), message, sfe, null);
            // impossible reach here
            return null;
        }
    }

    private static void throwException(QName subcode, String detail, SOAPFaultException sfe, Fault fault) 
            throws CannotRegisterException, InvalidProtocolException, InvalidStateException, SoapFault {
        if (CoordinationConstants.WSCOOR_ERROR_CODE_CANNOT_REGISTER_QNAME.equals(subcode)) {
            throw new CannotRegisterException(detail);
        } else if (CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PROTOCOL_QNAME.equals(subcode)) {
            throw new InvalidProtocolException(detail);
        } else if (CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME.equals(subcode)) {
            throw new InvalidStateException(detail);
        }
        if (sfe != null) {
            throw SoapFault11.create(sfe);
        } else {
            throw SoapFault11.fromFault(fault);
        }
    }

    private static RegisterResponseType registerOperation(String messageID, final RegistrationPortType port,
            final RegisterType registerType) throws CannotRegisterException, InvalidProtocolException,
            InvalidStateException, SoapFault 
    {
        final WSCEnvironmentBean wscEnvironmentBean = XTSPropertyManager.getWSCEnvironmentBean();
        if (!WSCEnvironmentBean.NO_ASYNC_REQUEST.equals(wscEnvironmentBean.getUseAsynchronousRequest())) {
            // using asynchronous request, we have to wait for the response
            AsynchronousRegistrationMapper.getInstance().addClientMessage(messageID);
        }

        RegisterResponseType response;
        if (System.getSecurityManager() == null) {
            response = port.registerOperation(registerType);
        } else {
            response = AccessController.doPrivileged(new PrivilegedAction<RegisterResponseType>() {
                @Override
                public RegisterResponseType run() {
                    return port.registerOperation(registerType);
                }
            });
        }

        if (!WSCEnvironmentBean.NO_ASYNC_REQUEST.equals(wscEnvironmentBean.getUseAsynchronousRequest())) {
            // wait until the response arrives
            FaultOrResponse res = AsynchronousRegistrationMapper.getInstance().waitForResponse(messageID, wscEnvironmentBean.getAsyncRequestWait());
            if (res.isFault()) {
                String detail = res.getFault().getFaultstring();
                if (res.getFault().getDetail()!= null && 
                        res.getFault().getDetail().getAny() != null && 
                        !res.getFault().getDetail().getAny().isEmpty()) {
                    detail = res.getFault().getDetail().getAny().get(0).toString();
                }
                throwException(res.getFault().getFaultcode(), detail, null, res.getFault());
            } else if (res.isEmpty()) {
                throw new InvalidStateException("No response assigned to messageID " + messageID);
            }
            response = res.getResponse();
        }

        return response;
    }
}
