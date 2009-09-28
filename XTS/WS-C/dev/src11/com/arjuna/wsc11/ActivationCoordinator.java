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
package com.arjuna.wsc11;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.wsf.common.addressing.MAP;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wscoor.client.ActivationCoordinatorClient;
import com.arjuna.wsc.InvalidCreateParametersException;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContext;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CreateCoordinationContextResponseType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.Expires;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;

/**
 * Wrapper around low level Activation Coordinator messaging.
 * @author kevin
 */
public class ActivationCoordinator
{
    /**
     * Create the coordination context.
     * @param activationCoordinatorURI The URI of the activation coordinator.
     * @param messageID The messageID to use.
     * @param coordinationTypeURI The coordination type.
     * @param expires The expiry time or null.
     * @param currentContext The currnt context or null.
     * @return The coordination context.
     * @throws com.arjuna.wsc.InvalidCreateParametersException if the create parameters are invalid.
     * @throws SoapFault for errors during processing.
     */
    public static CoordinationContextType createCoordinationContext(final String activationCoordinatorURI,
        final String messageID, final String coordinationTypeURI, final Long expires,
        final CoordinationContext currentContext)
        throws InvalidCreateParametersException, SoapFault
    {
        final MAP map = AddressingHelper.createRequestContext(activationCoordinatorURI, messageID) ;
        
        final Expires expiresValue;
        if (expires == null) {
            expiresValue = null;
        } else {
            expiresValue = new Expires();
            expiresValue.setValue(expires.longValue());
        }

        try
        {
            CreateCoordinationContextResponseType response;
            ActivationCoordinatorClient client = ActivationCoordinatorClient.getClient();
            response = client.sendCreateCoordination(map, coordinationTypeURI, expiresValue, currentContext) ;
            return response.getCoordinationContext();
        }
        catch (final IOException ioe)
        {
            throw new SoapFault11(ioe) ;
        } catch (SOAPFaultException sfe) {
            // TODO -- work out which faults we should really throw. in particular do we need to retain SoapFault
            final SOAPFault soapFault = sfe.getFault() ;
            final QName subcode = soapFault.getFaultCodeAsQName() ;
            if (CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME.equals(subcode))
            {
                throw new InvalidCreateParametersException(soapFault.getFaultString());
            }
            throw new SoapFault11(sfe);
        }
    }
}
