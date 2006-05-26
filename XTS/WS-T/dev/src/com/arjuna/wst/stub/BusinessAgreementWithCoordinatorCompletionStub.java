/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (c) 2003, Arjuna Technologies Limited.
 *
 * $Id: BusinessAgreementWithCoordinatorCompletionStub.java,v 1.1.2.2 2004/06/18 15:06:09 nmcl Exp $
 */

package com.arjuna.wst.stub;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices.wsba.ExceptionType;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.StatusType;
import com.arjuna.webservices.wsba.client.CoordinatorCompletionParticipantClient;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionCoordinatorCallback;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionCoordinatorProcessor;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;

public class BusinessAgreementWithCoordinatorCompletionStub implements com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant
{
    private EndpointReferenceType _businessAgreementWithCoordinatorCompletionParticipant = null;
    private final String _id ;

    public BusinessAgreementWithCoordinatorCompletionStub (final String id, final EndpointReferenceType businessAgreementWithCoordinatorCompletionParticipant)
        throws Exception
    {
        _businessAgreementWithCoordinatorCompletionParticipant         = businessAgreementWithCoordinatorCompletionParticipant;
        _id = id ;
    }

    public void close ()
        throws WrongStateException, SystemException
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_businessAgreementWithCoordinatorCompletionParticipant, MessageId.getMessageId()) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final CoordinatorCompletionCoordinatorProcessor coordinatorCompletionCoordinator = CoordinatorCompletionCoordinatorProcessor.getCoordinator() ;
        coordinatorCompletionCoordinator.registerCallback(_id, callback) ;
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendClose(addressingContext, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
        finally
        {
            coordinatorCompletionCoordinator.removeCallback(_id) ;
        }
        
        if (callback.hasTriggered())
        {
            if (callback.receivedClosed())
            {
                return ;
            }
            final SoapFault soapFault = callback.getSoapFault() ;
            if ((soapFault != null) && ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
            {
                throw new WrongStateException();
            }
        }
        
        throw new SystemException() ;
    }

    public void cancel ()
        throws WrongStateException, SystemException
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_businessAgreementWithCoordinatorCompletionParticipant, MessageId.getMessageId()) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final CoordinatorCompletionCoordinatorProcessor coordinatorCompletionCoordinator = CoordinatorCompletionCoordinatorProcessor.getCoordinator() ;
        coordinatorCompletionCoordinator.registerCallback(_id, callback) ;
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendCancel(addressingContext, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
        finally
        {
            coordinatorCompletionCoordinator.removeCallback(_id) ;
        }
        
        if (callback.hasTriggered())
        {
            if (callback.receivedCancelled())
            {
                return ;
            }
            final SoapFault soapFault = callback.getSoapFault() ;
            if ((soapFault != null) && ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
            {
                throw new WrongStateException();
            }
        }
        
        throw new SystemException() ;
    }

    public void compensate ()
        throws FaultedException, WrongStateException, SystemException
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_businessAgreementWithCoordinatorCompletionParticipant, MessageId.getMessageId()) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final CoordinatorCompletionCoordinatorProcessor coordinatorCompletionCoordinator = CoordinatorCompletionCoordinatorProcessor.getCoordinator() ;
        coordinatorCompletionCoordinator.registerCallback(_id, callback) ;
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendCompensate(addressingContext, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
        finally
        {
            coordinatorCompletionCoordinator.removeCallback(_id) ;
        }
        
        if (callback.hasTriggered())
        {
            if (callback.receivedCompensated())
            {
                return ;
            }
            final ExceptionType fault = callback.getFault() ;
            if (fault != null)
            {
                final AddressingContext responseContext = AddressingContext.createNotificationContext(callback.getAddressingContext(),
                    MessageId.getMessageId()) ;
                try
                {
                    CoordinatorCompletionParticipantClient.getClient().sendFaulted(responseContext, new InstanceIdentifier(_id)) ;
                }
                catch (final Throwable th) {} // ignore this as we are faulting
                throw new FaultedException() ;
            }
            final SoapFault soapFault = callback.getSoapFault() ;
            if ((soapFault != null) && ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
            {
                throw new WrongStateException();
            }
        }
        
        throw new SystemException() ;
    }

    public void complete ()
        throws WrongStateException, SystemException
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_businessAgreementWithCoordinatorCompletionParticipant, MessageId.getMessageId()) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final CoordinatorCompletionCoordinatorProcessor coordinatorCompletionCoordinator = CoordinatorCompletionCoordinatorProcessor.getCoordinator() ;
        coordinatorCompletionCoordinator.registerCallback(_id, callback) ;
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendComplete(addressingContext, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
        finally
        {
            coordinatorCompletionCoordinator.removeCallback(_id) ;
        }
        
        if (callback.hasTriggered())
        {
            if (callback.receivedCompleted())
            {
                return ;
            }
            final SoapFault soapFault = callback.getSoapFault() ;
            if ((soapFault != null) && ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
            {
                throw new WrongStateException();
            }
        }
        
        throw new SystemException() ;
    }

    public String status ()
        throws SystemException
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_businessAgreementWithCoordinatorCompletionParticipant, MessageId.getMessageId()) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final CoordinatorCompletionCoordinatorProcessor coordinatorCompletionCoordinator = CoordinatorCompletionCoordinatorProcessor.getCoordinator() ;
        coordinatorCompletionCoordinator.registerCallback(_id, callback) ;
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendGetStatus(addressingContext, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
        finally
        {
            coordinatorCompletionCoordinator.removeCallback(_id) ;
        }
        
        if (callback.hasTriggered())
        {
            final StatusType status = callback.getStatus() ;
            if (status != null)
            {
                return status.getState().toString() ;
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
     * @message com.arjuna.wst.stub.BusinessAgreementWithCoordinatorCompletionStub_1 [com.arjuna.wst.stub.BusinessAgreementWithCoordinatorCompletionStub_1] - Unknown error
     */
    public void error ()
        throws SystemException
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_businessAgreementWithCoordinatorCompletionParticipant, MessageId.getMessageId()) ;
        final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME,
                WSTLogger.log_mesg.getString("com.arjuna.wst.stub.BusinessAgreementWithCoordinatorCompletionStub_1")) ;
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendSoapFault(addressingContext, soapFault, new InstanceIdentifier(_id)) ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
    }

    private static class RequestCallback extends CoordinatorCompletionCoordinatorCallback
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
         * The fault.
         */
        private ExceptionType fault ;
        /**
         * The status.
         */
        private StatusType status ;
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
         * The compensated notification flag.
         */
        private boolean compensated ;
        
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
         * Get the fault.
         * @return The fault.
         */
        ExceptionType getFault()
        {
            return fault ;
        }
        
        /**
         * Get the status.
         * @return The status.
         */
        StatusType getStatus()
        {
            return status ;
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
         * Did we receive a compensated notification?
         * @return True if compensated, false otherwise.
         */
        boolean receivedCompensated()
        {
            return compensated ;
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
         * A compensated response.
         * @param compensated The compensated notification.
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void compensated(final NotificationType compensated, final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.compensated = true ;
            this.addressingContext = addressingContext ;
            this.arjunaContext = arjunaContext ;
        }

        /**
         * A fault response.
         * @param fault The fault notification.
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void fault(final ExceptionType fault, final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.fault = fault ;
            this.addressingContext = addressingContext ;
            this.arjunaContext = arjunaContext ;
        }

        /**
         * A status response.
         * @param status The status notification.
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void status(final StatusType status, final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.status = status ;
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
