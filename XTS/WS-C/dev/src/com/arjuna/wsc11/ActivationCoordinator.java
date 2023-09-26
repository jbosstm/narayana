/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.wsc11;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wscoor.client.ActivationCoordinatorClient;
import com.arjuna.wsc.InvalidCreateParametersException;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContext;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CreateCoordinationContextResponseType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.Expires;

import javax.xml.namespace.QName;
import jakarta.xml.soap.Detail;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.soap.SOAPFaultException;
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
                Detail detail = soapFault.getDetail();
                String message = (detail != null ? detail.getTextContent() : soapFault.getFaultString());
                throw new InvalidCreateParametersException(message);
            }
            throw SoapFault11.create(sfe);
        }
    }
}