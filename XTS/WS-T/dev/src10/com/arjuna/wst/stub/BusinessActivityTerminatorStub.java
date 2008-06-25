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

package com.arjuna.wst.stub;

import javax.xml.namespace.QName;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFault10;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices.wsarjtx.NotificationType;
import com.arjuna.webservices.wsarjtx.client.TerminationCoordinatorClient;
import com.arjuna.webservices.wsarjtx.processors.TerminationParticipantCallback;
import com.arjuna.webservices.wsarjtx.processors.TerminationParticipantProcessor;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;

public class BusinessActivityTerminatorStub implements com.arjuna.wst.BusinessActivityTerminator
{
    private EndpointReferenceType          _terminationParticipant         = null;
    private final String _id ;
    
    public BusinessActivityTerminatorStub (final String id, final EndpointReferenceType terminationParticipant)
        throws Exception
    {
        _terminationParticipant         = terminationParticipant;
        _id = id ;
    }

    public void close ()
        throws TransactionRolledBackException, UnknownTransactionException, SystemException
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_terminationParticipant, MessageId.getMessageId()) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final TerminationParticipantProcessor terminatorCoordinatorProcessor = TerminationParticipantProcessor.getProcessor() ;
        terminatorCoordinatorProcessor.registerCallback(_id, callback) ;
        try
        {
            TerminationCoordinatorClient.getClient().sendClose(addressingContext, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            throw new SystemException() ;
        }
        finally
        {
            terminatorCoordinatorProcessor.removeCallback(_id) ;
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
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_terminationParticipant, MessageId.getMessageId()) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final TerminationParticipantProcessor terminatorCoordinatorProcessor = TerminationParticipantProcessor.getProcessor() ;
        terminatorCoordinatorProcessor.registerCallback(_id, callback) ;
        try
        {
            TerminationCoordinatorClient.getClient().sendCancel(addressingContext, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            throw new SystemException() ;
        }
        finally
        {
            terminatorCoordinatorProcessor.removeCallback(_id) ;
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
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_terminationParticipant, MessageId.getMessageId()) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final TerminationParticipantProcessor terminatorCoordinatorProcessor = TerminationParticipantProcessor.getProcessor() ;
        terminatorCoordinatorProcessor.registerCallback(_id, callback) ;
        try
        {
            TerminationCoordinatorClient.getClient().sendComplete(addressingContext, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            throw new SystemException() ;
        }
        finally
        {
            terminatorCoordinatorProcessor.removeCallback(_id) ;
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

    public void unknown ()
        throws SystemException
    {
        error() ;
    }

    /**
     * @message com.arjuna.wst.stub.BusinessActivityTerminatorStub_1 [com.arjuna.wst.stub.BusinessActivityTerminatorStub_1] - Unknown error
     */
    public void error ()
        throws SystemException
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_terminationParticipant, MessageId.getMessageId()) ;
        final SoapFault soapFault = new SoapFault10(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME,
                WSTLogger.log_mesg.getString("com.arjuna.wst.stub.BusinessActivityTerminatorStub_1")) ;
        try
        {
            TerminationCoordinatorClient.getClient().sendSoapFault(addressingContext, soapFault, new InstanceIdentifier(_id)) ;
        }
        catch (final Throwable th)
        {
            throw new SystemException() ;
        }
    }

    private static class RequestCallback extends TerminationParticipantCallback
    {
        /**
         * The addressing context.
         */
        private AddressingContext addressingContext ;
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
        AddressingContext getAddressingContext()
        {
            return addressingContext ;
        }
        
        /**
         * Get the arjuna context.
         * @return The arjuna context.
         */
        ArjunaContext getArjunaContext()
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
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void completed(final NotificationType completed, final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.completed = true ;
            this.addressingContext = addressingContext ;
            this.arjunaContext = arjunaContext ;
        }

        /**
         * A cancelled response.
         * @param cancelled The cancelled notification.
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void cancelled(final NotificationType cancelled, final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.cancelled  = true ;
            this.addressingContext = addressingContext ;
            this.arjunaContext = arjunaContext ;
        }

        /**
         * A closed response.
         * @param closed The closed notification.
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void closed(final NotificationType closed, final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.closed = true ;
            this.addressingContext = addressingContext ;
            this.arjunaContext = arjunaContext ;
        }

        /**
         * A faulted response.
         * @param faulted The faulted notification.
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void faulted(final NotificationType faulted, final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.faulted = true ;
            this.addressingContext = addressingContext ;
            this.arjunaContext = arjunaContext ;
        }
        
        /**
         * A SOAP fault response.
         * @param soapFault The SOAP fault.
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.soapFault = soapFault ;
            this.addressingContext = addressingContext ;
            this.arjunaContext = arjunaContext ;
        }
    }
}
