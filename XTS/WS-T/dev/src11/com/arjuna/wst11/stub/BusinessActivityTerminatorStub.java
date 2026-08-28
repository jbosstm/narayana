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
/*
 * Copyright (c) 2003, Arjuna Technologies Limited.
 *
 * $Id: BusinessActivityTerminatorStub.java,v 1.7.6.1 2005/11/22 10:35:29 kconner Exp $
 */

package com.arjuna.wst11.stub;

import com.arjuna.schemas.ws._2005._10.wsarjtx.NotificationType;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsarjtx.client.TerminationCoordinatorClient;
import com.arjuna.webservices11.wsarjtx.processors.TerminationParticipantCallback;
import com.arjuna.webservices11.wsarjtx.processors.TerminationParticipantProcessor;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst11.BusinessActivityTerminator;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

public class BusinessActivityTerminatorStub implements BusinessActivityTerminator
{
    private W3CEndpointReference _terminationCoordinator = null;
    private final String _id ;

    public BusinessActivityTerminatorStub(final String id, final W3CEndpointReference terminationCoordinator)
        throws Exception
    {
        _terminationCoordinator = terminationCoordinator;
        _id = id ;
    }

    public void close ()
        throws TransactionRolledBackException, UnknownTransactionException, SystemException
    {
        final MAP map = AddressingHelper.createNotificationContext(MessageId.getMessageId()) ;

        final RequestCallback callback = new RequestCallback() ;
        final TerminationParticipantProcessor terminationParticipantProcessor = TerminationParticipantProcessor.getProcessor() ;
        terminationParticipantProcessor.registerCallback(_id, callback) ;
        try
        {
            TerminationCoordinatorClient.getClient().sendClose(_terminationCoordinator, map, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            throw new SystemException() ;
        }
        finally
        {
            terminationParticipantProcessor.removeCallback(_id) ;
        }

        if (callback.hasTriggered())
        {
            if (callback.receivedClosed())
            {
                return ;
            }
            final SoapFault soapFault = callback.getSoapFault() ;
            if (soapFault != null)
            {
                final QName subcode = soapFault.getSubcode() ;
                if (ArjunaTXConstants.TRANSACTIONROLLEDBACK_ERROR_CODE_QNAME.equals(subcode))
                {
                    throw new TransactionRolledBackException();
                }
                else if (ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME.equals(subcode))
                {
                    throw new UnknownTransactionException() ;
                }
            }
        }
        throw new SystemException() ;
    }

    public void cancel ()
        throws FaultedException, UnknownTransactionException, SystemException
    {
        final MAP map = AddressingHelper.createNotificationContext(MessageId.getMessageId()) ;

        final RequestCallback callback = new RequestCallback() ;
        final TerminationParticipantProcessor terminationParticipantProcessor = TerminationParticipantProcessor.getProcessor() ;
        terminationParticipantProcessor.registerCallback(_id, callback) ;
        try
        {
            TerminationCoordinatorClient.getClient().sendCancel(_terminationCoordinator, map, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            throw new SystemException() ;
        }
        finally
        {
            terminationParticipantProcessor.removeCallback(_id) ;
        }

        if (callback.hasTriggered())
        {
            if (callback.receivedCancelled())
            {
                return ;
            }
            else if (callback.receivedFaulted())
            {
                throw new FaultedException() ;
            }
            final SoapFault soapFault = callback.getSoapFault() ;
            if (soapFault != null)
            {
                final QName subcode = soapFault.getSubcode() ;
                if (ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME.equals(subcode))
                {
                    throw new UnknownTransactionException() ;
                }
            }
        }
        throw new SystemException() ;
    }

    public void complete ()
        throws FaultedException, UnknownTransactionException, SystemException
    {
        final MAP map = AddressingHelper.createNotificationContext(MessageId.getMessageId()) ;

        final RequestCallback callback = new RequestCallback() ;
        final TerminationParticipantProcessor terminationParticipantProcessor = TerminationParticipantProcessor.getProcessor() ;
        terminationParticipantProcessor.registerCallback(_id, callback) ;
        try
        {
            TerminationCoordinatorClient.getClient().sendComplete(_terminationCoordinator, map, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            throw new SystemException() ;
        }
        finally
        {
            terminationParticipantProcessor.removeCallback(_id) ;
        }

        if (callback.hasTriggered())
        {
            if (callback.receivedCompleted())
            {
                return ;
            }
            else if (callback.receivedFaulted())
            {
                throw new FaultedException() ;
            }
            final SoapFault soapFault = callback.getSoapFault() ;
            if (soapFault != null)
            {
                final QName subcode = soapFault.getSubcode() ;
                if (ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME.equals(subcode))
                {
                    throw new UnknownTransactionException() ;
                }
            }
        }
        throw new SystemException() ;
    }

    public W3CEndpointReference getEndpoint()
    {
        return _terminationCoordinator;
    }
    /*
     * this never gets called
     */
    public void unknown ()
        throws SystemException
    {
        error() ;
    }

    /*
     * this never gets called
     */
    public void error ()
        throws SystemException
    {
/*
 * Since it is never used this has been decommissioned due to problems with using the soap fault service to
  * send a fault via a W3C endpoint. the latter is broken now that CXF helpfully puts metadata into the endpoint.
        final MAP map = AddressingHelper.createNotificationContext(MessageId.getMessageId()) ;
        final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME,
                WSTLogger.i18NLogger.get_wst11_stub_BusinessActivityTerminatorStub_1()) ;
        try
        {
            TerminationCoordinatorClient.getClient().sendSoapFault(_terminationCoordinator, map, soapFault, new InstanceIdentifier(_id)) ;
        }
        catch (final Throwable th)
        {
            throw new SystemException() ;
        }
*/
    }

    private static class RequestCallback extends TerminationParticipantCallback
    {
        /**
         * The addressing context.
         */
        private MAP map ;
        /**
         * The arjuna context.
         */
        private ArjunaContext arjunaContext ;
        /**
         * The SOAP fault.
         */
        private SoapFault soapFault ;
        /**
         * The completed notification flag.
         */
        private boolean completed ;
        /**
         * The cancelled notification flag.
         */
        private boolean cancelled ;
        /**
         * The closed notification flag.
         */
        private boolean closed ;
        /**
         * The faulted notification flag.
         */
        private boolean faulted ;

        /**
         * Get the addressing context.
         * @return The addressing context.
         */
        MAP getAddressingProperties()
        {
            return map ;
        }

        /**
         * Get the arjuna context.
         * @return The arjuna context.
         */
        ArjunaContext getArjunaContextWS()
        {
            return arjunaContext ;
        }

        /**
         * Get the SOAP fault.
         * @return The SOAP fault.
         */
        SoapFault getSoapFault()
        {
            return soapFault ;
        }

        /**
         * Did we receive a completed notification?
         * @return True if completed, false otherwise.
         */
        boolean receivedCompleted()
        {
            return completed ;
        }

        /**
         * Did we receive a cancelled notification?
         * @return True if cancelled, false otherwise.
         */
        boolean receivedCancelled()
        {
            return cancelled ;
        }

        /**
         * Did we receive a closed notification?
         * @return True if closed, false otherwise.
         */
        boolean receivedClosed()
        {
            return closed ;
        }

        /**
         * Did we receive a faulted notification?
         * @return True if faulted, false otherwise.
         */
        boolean receivedFaulted()
        {
            return faulted ;
        }

        /**
         * A completed response.
         * @param completed The completed notification.
         * @param map The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void completed(final NotificationType completed, final MAP map,
            final ArjunaContext arjunaContext)
        {
            this.completed = true ;
            this.map = map ;
            this.arjunaContext = arjunaContext ;
        }

        /**
         * A cancelled response.
         * @param cancelled The cancelled notification.
         * @param map The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void cancelled(final NotificationType cancelled, final MAP map,
            final ArjunaContext arjunaContext)
        {
            this.cancelled  = true ;
            this.map = map ;
            this.arjunaContext = arjunaContext ;
        }

        /**
         * A closed response.
         * @param closed The closed notification.
         * @param map The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void closed(final NotificationType closed, final MAP map,
            final ArjunaContext arjunaContext)
        {
            this.closed = true ;
            this.map = map ;
            this.arjunaContext = arjunaContext ;
        }

        /**
         * A faulted response.
         * @param faulted The faulted notification.
         * @param map The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void faulted(final NotificationType faulted, final MAP map,
            final ArjunaContext arjunaContext)
        {
            // TODO - convert wsdl to use NotificationType instead of ExceptionType
            this.faulted = true ;
            this.map = map ;
            this.arjunaContext = arjunaContext ;
        }

        /**
         * A SOAP fault response.
         * @param soapFault The SOAP fault.
         * @param map The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void soapFault(final SoapFault soapFault, final MAP map,
            final ArjunaContext arjunaContext)
        {
            // TODO - pass soap fault to this callback
            this.soapFault = soapFault ;
            this.map = map ;
            this.arjunaContext = arjunaContext ;
        }
    }
}