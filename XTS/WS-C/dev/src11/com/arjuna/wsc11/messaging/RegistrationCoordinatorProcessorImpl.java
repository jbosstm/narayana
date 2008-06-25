/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.wsc11.messaging;

import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wscoor.processors.RegistrationCoordinatorProcessor;
import com.arjuna.wsc.*;
import com.arjuna.wsc11.RegistrarMapper;
import com.arjuna.wsc11.Registrar;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterResponseType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterType;

import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.wsaddressing.W3CEndpointReference;


/**
 * The Registration Coordinator processor.
 * @author kevin
 * @message com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_1 [com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_1] - Participant already registered
 * @message com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_2 [com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_2] - Invalid protocol identifier
 * @message com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_3 [com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_3] - Invalid coordination context state
 * @message com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_4 [com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_4] - Unknown activity identifier
 * @message com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_5 [com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_5] - Unexpected exception thrown from create:
 * @message com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_6 [com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_6] - Register called for unknown protocol identifier: {0}
 */
public class RegistrationCoordinatorProcessorImpl extends RegistrationCoordinatorProcessor
{
    /**
     * Register the participant in the protocol.
     * @param register The register request.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public RegisterResponseType register(final RegisterType register, final AddressingProperties addressingContext,
        final ArjunaContext arjunaContext)
    {
        final com.arjuna.wsc11.RegistrarMapper registrarMapper = RegistrarMapper.getFactory() ;
        try
        {
            final String protocolIdentifier = register.getProtocolIdentifier() ;
            final Registrar registrar = registrarMapper.getRegistrar(protocolIdentifier) ;
            
            if (registrar != null)
            {
                try
                {
                    final W3CEndpointReference participantProtocolService = register.getParticipantProtocolService() ;
                    final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier();
                    final W3CEndpointReference coordinationProtocolService =
                            registrar.register(participantProtocolService, protocolIdentifier, instanceIdentifier) ;
                    final RegisterResponseType response = new RegisterResponseType() ;

                    response.setCoordinatorProtocolService(coordinationProtocolService);
                    return response;
                }
                catch (final AlreadyRegisteredException alreadyRegisteredException)
                {
                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_ALREADY_REGISTERED_QNAME);
                    soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_ALREADY_REGISTERED_QNAME).addTextNode(WSCLogger.log_mesg.getString("com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_1"));
                    throw new SOAPFaultException(soapFault);
                }
                catch (final InvalidProtocolException invalidProtocolException)
                {
                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PROTOCOL_QNAME);
                    soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME).addTextNode(WSCLogger.log_mesg.getString("com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_2"));
                    throw new SOAPFaultException(soapFault);
                }
                catch (final InvalidStateException InvalidStateException)
                {
                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME);
                    soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME).addTextNode(WSCLogger.log_mesg.getString("com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_3"));
                    throw new SOAPFaultException(soapFault);
                }
                catch (final NoActivityException noActivityException)
                {
                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_NO_ACTIVITY_QNAME);
                    soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_NO_ACTIVITY_QNAME).addTextNode(WSCLogger.log_mesg.getString("com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_4"));
                    throw new SOAPFaultException(soapFault);
                }
                catch (final Throwable th)
                {
                    if (WSCLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSCLogger.arjLoggerI18N.debug("com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_5", th) ;
                    }
                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault();
                    soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_EXCEPTION_QNAME).addTextNode(th.getMessage());
                    throw new SOAPFaultException(soapFault);
                }
            }
            else
            {
                if (WSCLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSCLogger.arjLoggerI18N.debug("com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_6", new Object[] {protocolIdentifier}) ;
                }
                SOAPFactory factory = SOAPFactory.newInstance();
                SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PROTOCOL_QNAME);
                WSCLogger.log_mesg.getString("com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl_2") ;
                throw new SOAPFaultException(soapFault);
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace(System.err);
            throw new ProtocolException(throwable);
        }
    }
}
