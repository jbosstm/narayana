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
package com.arjuna.wsc11.tests.junit;

import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wscoor.processors.ActivationCoordinatorProcessor;
import org.jboss.jbossts.xts.wsaddr.map.MAP;
import com.arjuna.wsc.tests.TestUtil;
import com.arjuna.wsc11.tests.TestUtil11;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContext;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CreateCoordinationContextResponseType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CreateCoordinationContextType;

import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.util.HashMap;
import java.util.Map;

public class TestActivationCoordinatorProcessor extends
        ActivationCoordinatorProcessor
{
    private Map messageIdMap = new HashMap() ;

    public CreateCoordinationContextResponseType createCoordinationContext(final CreateCoordinationContextType createCoordinationContext,
        final MAP map, boolean isSecure)
    {
        final String messageId = map.getMessageID() ;
        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, new CreateCoordinationContextDetails(createCoordinationContext, map)) ;
            messageIdMap.notifyAll() ;
        }
        String coordinationType = createCoordinationContext.getCoordinationType();
        if (TestUtil.INVALID_CREATE_PARAMETERS_COORDINATION_TYPE.equals(coordinationType)) {
            try {
                SOAPFactory factory = SOAPFactory.newInstance();
                SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME);
                soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME).addTextNode("Invalid create parameters");
                throw new SOAPFaultException(soapFault);
            } catch (Throwable th) {
                throw new ProtocolException(th);
            }
        }
        
        // we have to return a value so lets cook one up

        CreateCoordinationContextResponseType createCoordinationContextResponseType = new CreateCoordinationContextResponseType();
        CoordinationContext coordinationContext = new CoordinationContext();
        coordinationContext.setCoordinationType(coordinationType);
        coordinationContext.setExpires(createCoordinationContext.getExpires());
        String identifier = nextIdentifier();
        CoordinationContextType.Identifier identifierInstance = new CoordinationContextType.Identifier();
        identifierInstance.setValue(identifier);
        coordinationContext.setIdentifier(identifierInstance);
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.serviceName(CoordinationConstants.REGISTRATION_SERVICE_QNAME);
        builder.endpointName(CoordinationConstants.REGISTRATION_ENDPOINT_QNAME);
        builder.address(TestUtil.PROTOCOL_COORDINATOR_SERVICE);
        W3CEndpointReference registrationService = builder.build();
        coordinationContext.setRegistrationService(TestUtil11.getRegistrationEndpoint(identifier));
        createCoordinationContextResponseType.setCoordinationContext(coordinationContext);

        return createCoordinationContextResponseType;
    }

    public CreateCoordinationContextDetails getCreateCoordinationContextDetails(final String messageId, long timeout)
    {
        final long endTime = System.currentTimeMillis() + timeout ;
        synchronized(messageIdMap)
        {
            long now = System.currentTimeMillis() ;
            while(now < endTime)
            {
                final CreateCoordinationContextDetails details = (CreateCoordinationContextDetails)messageIdMap.remove(messageId) ;
                if (details != null)
                {
                    return details ;
                }
                try
                {
                    messageIdMap.wait(endTime - now) ;
                }
                catch (final InterruptedException ie) {} // ignore
                now = System.currentTimeMillis() ;
            }
            final CreateCoordinationContextDetails details = (CreateCoordinationContextDetails)messageIdMap.remove(messageId) ;
            if (details != null)
            {
                return details ;
            }
        }
        throw new NullPointerException("Timeout occurred waiting for id: " + messageId) ;
    }

    public static class CreateCoordinationContextDetails
    {
        private final CreateCoordinationContextType createCoordinationContext ;
        private final MAP map ;

        CreateCoordinationContextDetails(final CreateCoordinationContextType createCoordinationContext,
            final MAP map)
        {
            this.createCoordinationContext = createCoordinationContext ;
            this.map = map ;
        }

        public CreateCoordinationContextType getCreateCoordinationContext()
        {
            return createCoordinationContext ;
        }

        public MAP getMAP()
        {
            return map ;
        }
    }

    private static int nextIdentifier = 0;

    private synchronized String nextIdentifier()
    {
        int value = nextIdentifier++;

        return Integer.toString(value);
    }
}