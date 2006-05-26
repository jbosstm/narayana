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
package com.arjuna.wst.stub;

import javax.xml.namespace.QName;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.StatusType;
import com.arjuna.webservices.wsba.client.ParticipantCompletionCoordinatorClient;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionParticipantCallback;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;

public class BAParticipantCompletionParticipantManagerStub implements com.arjuna.wst.BAParticipantManager
{
    private final EndpointReferenceType _baParticipantManagerParticipant ;
    private final String _id ;
    
    public BAParticipantCompletionParticipantManagerStub (final String id, final EndpointReferenceType baParticipantManagerParticipant)
        throws Exception
    {
        _baParticipantManagerParticipant         = baParticipantManagerParticipant;
        _id = id ;
    }

    public void exit ()
        throws WrongStateException, UnknownTransactionException, SystemException
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_baParticipantManagerParticipant, MessageId.getMessageId()) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final ParticipantCompletionParticipantProcessor participantProcessor = ParticipantCompletionParticipantProcessor.getParticipant() ;
        participantProcessor.registerCallback(_id, callback) ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendExit(addressingContext, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
        finally
        {
            participantProcessor.removeCallback(_id) ;
        }
        
        if (callback.hasTriggered())
        {
            if (callback.receivedExited())
            {
                return ;
            }
            final SoapFault soapFault = callback.getSoapFault() ;
            if (soapFault != null)
            {
                final QName subcode = soapFault.getSubcode() ;
                if (ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
                {
                    throw new WrongStateException();
                }
                else if (ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME.equals(subcode))
                {
                    throw new UnknownTransactionException() ;
                }
            }
        }
        
        throw new SystemException() ;
    }

    public void completed ()
        throws WrongStateException, UnknownTransactionException, SystemException
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_baParticipantManagerParticipant, MessageId.getMessageId()) ;
        
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendCompleted(addressingContext, new InstanceIdentifier(_id)) ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
    }

    public void fault ()
        throws SystemException
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_baParticipantManagerParticipant, MessageId.getMessageId()) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final ParticipantCompletionParticipantProcessor participantProcessor = ParticipantCompletionParticipantProcessor.getParticipant() ;
        participantProcessor.registerCallback(_id, callback) ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendFault(addressingContext, new InstanceIdentifier(_id), null) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
        finally
        {
            participantProcessor.removeCallback(_id) ;
        }
        
        if (callback.hasTriggered() && callback.receivedFaulted())
        {
            return ;
        }
        
        throw new SystemException() ;
    }

    public void unknown()
        throws SystemException
    {
        error() ;
    }

    /**
     * @message com.arjuna.wst.stub.BAParticipantCompletionParticipantManagerStub_1 [com.arjuna.wst.stub.BAParticipantCompletionParticipantManagerStub_1] - Unknown error
     */
    public void error()
        throws SystemException
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_baParticipantManagerParticipant, MessageId.getMessageId()) ;
        final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME,
                WSTLogger.log_mesg.getString("com.arjuna.wst.stub.BAParticipantCompletionParticipantManagerStub_1")) ;
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(addressingContext, soapFault, new InstanceIdentifier(_id)) ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
    }

    private static class RequestCallback extends ParticipantCompletionParticipantCallback
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
         * The complete notification flag.
         */
        private boolean complete ;
        /**
         * The exited notification flag.
         */
        private boolean exited ;
        /**
         * The faulted notification flag.
         */
        private boolean faulted ;
        /**
         * The status.
         */
        private StatusType status ;
        
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
         * Did we receive a complete notification?
         * @return True if complete, false otherwise.
         */
        boolean receivedComplete()
        {
            return complete ;
        }
        
        /**
         * Did we receive a exited notification?
         * @return True if exited, false otherwise.
         */
        boolean receivedExited()
        {
            return exited ;
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
         * Get the status response.
         * @return the status response.
         */
        StatusType getStatus()
        {
            return status ;
        }
        
        /**
         * A complete response.
         * @param complete The complete notification.
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void complete(final NotificationType complete, final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.complete = true ;
            this.addressingContext = addressingContext ;
            this.arjunaContext = arjunaContext ;
        }

        /**
         * An exited response.
         * @param exited The exited notification.
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void exited(final NotificationType exited, final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.exited  = true ;
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
         * A status response.
         * @param status The status notification.
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void status(final StatusType status, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
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
