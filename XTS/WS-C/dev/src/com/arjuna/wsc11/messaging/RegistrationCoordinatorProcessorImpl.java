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
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.wsc.*;
import com.arjuna.wsc11.RegistrarMapper;
import com.arjuna.wsc11.Registrar;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterResponseType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterType;

import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFactory;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.ProtocolException;
import jakarta.xml.ws.soap.SOAPFaultException;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;


/**
 * The Registration Coordinator processor.
 * @author kevin
 */
public class RegistrationCoordinatorProcessorImpl extends RegistrationCoordinatorProcessor
{
    /**
     * Register the participant in the protocol.
     * @param register The register request.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public RegisterResponseType register(final RegisterType register, final MAP map,
        final ArjunaContext arjunaContext, final boolean isSecure)
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
                            registrar.register(participantProtocolService, protocolIdentifier, instanceIdentifier, isSecure) ;
                    final RegisterResponseType response = new RegisterResponseType() ;

                    response.setCoordinatorProtocolService(coordinationProtocolService);
                    return response;
                }
                catch (final AlreadyRegisteredException alreadyRegisteredException)
                {
                    if (WSCLogger.logger.isTraceEnabled())
                        WSCLogger.logger.tracev(alreadyRegisteredException, "{0}, of protocol {1}, of instance {2}",
                            WSCLogger.i18NLogger.get_wsc11_messaging_RegistrationCoordinatorProcessorImpl_1(), protocolIdentifier, arjunaContext.getInstanceIdentifier());

                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_CANNOT_REGISTER_QNAME);
                    soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_CANNOT_REGISTER_QNAME).addTextNode(WSCLogger.i18NLogger.get_wsc11_messaging_RegistrationCoordinatorProcessorImpl_1());
                    throw new SOAPFaultException(soapFault);
                }
                catch (final InvalidProtocolException invalidProtocolException)
                {
                    if (WSCLogger.logger.isTraceEnabled())
                        WSCLogger.logger.tracev(invalidProtocolException, "{0}, of protocol {1}, of instance {2}",
                            WSCLogger.i18NLogger.get_wsc11_messaging_RegistrationCoordinatorProcessorImpl_2(), protocolIdentifier, arjunaContext.getInstanceIdentifier());

                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PROTOCOL_QNAME);
                    soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME).addTextNode(WSCLogger.i18NLogger.get_wsc11_messaging_RegistrationCoordinatorProcessorImpl_2());
                    throw new SOAPFaultException(soapFault);
                }
                catch (final InvalidStateException invalidStateException)
                {
                    if (WSCLogger.logger.isTraceEnabled())
                        WSCLogger.logger.tracev(invalidStateException, "{0}, of protocol {1}, of instance {2}",
                            WSCLogger.i18NLogger.get_wsc11_messaging_RegistrationCoordinatorProcessorImpl_3(), protocolIdentifier, arjunaContext.getInstanceIdentifier());

                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME);
                    soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME).addTextNode(WSCLogger.i18NLogger.get_wsc11_messaging_RegistrationCoordinatorProcessorImpl_3());
                    throw new SOAPFaultException(soapFault);
                }
                catch (final NoActivityException noActivityException)
                {
                    if (WSCLogger.logger.isTraceEnabled())
                        WSCLogger.logger.tracev(noActivityException, "{0}, of protocol {1}, of instance {2}",
                            WSCLogger.i18NLogger.get_wsc11_messaging_RegistrationCoordinatorProcessorImpl_4(), protocolIdentifier, arjunaContext.getInstanceIdentifier());

                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_CANNOT_REGISTER_QNAME);
                    soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_CANNOT_REGISTER_QNAME).addTextNode(WSCLogger.i18NLogger.get_wsc11_messaging_RegistrationCoordinatorProcessorImpl_4());
                    throw new SOAPFaultException(soapFault);
                }
                catch (final Throwable th)
                {
                    if (WSCLogger.logger.isTraceEnabled())
                        WSCLogger.logger.tracev(th, "Unexpected exception thrown from create: protocol {1}, identifier {2}",
                            protocolIdentifier, arjunaContext == null ? null : arjunaContext.getInstanceIdentifier());

                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_CANNOT_REGISTER_QNAME);
                    soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_CANNOT_REGISTER_QNAME).addTextNode(th.getMessage());
                    throw new SOAPFaultException(soapFault);
                }
            }
            else
            {
                if (WSCLogger.logger.isTraceEnabled()) {
                    WSCLogger.logger.tracev("Register called for unknown protocol identifier: {0} [{1}]",
                        protocolIdentifier, WSCLogger.i18NLogger.get_wsc11_messaging_RegistrationCoordinatorProcessorImpl_2()) ;
                }

                SOAPFactory factory = SOAPFactory.newInstance();
                SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PROTOCOL_QNAME);
                soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME).addTextNode(WSCLogger.i18NLogger.get_wsc11_messaging_RegistrationCoordinatorProcessorImpl_2());
                throw new SOAPFaultException(soapFault);
            }
        }
        catch (SOAPException se)
        {
            WSCLogger.i18NLogger.error_failure_to_register_protocol(register == null ? null : register.getProtocolIdentifier(),
                arjunaContext == null ? null : arjunaContext.getInstanceIdentifier(), se);
            throw new ProtocolException(se);
        }
    }
}
