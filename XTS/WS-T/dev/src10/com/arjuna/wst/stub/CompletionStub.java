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
 * CompletionStub.java
 */

package com.arjuna.wst.stub;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices.wsat.NotificationType;
import com.arjuna.webservices.wsat.client.CompletionCoordinatorClient;
import com.arjuna.webservices.wsat.processors.CompletionInitiatorCallback;
import com.arjuna.webservices.wsat.processors.CompletionInitiatorProcessor;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;

public class CompletionStub implements
		com.arjuna.wst.CompletionCoordinatorParticipant
{
    private EndpointReferenceType _completionCoordinator = null;
    private String _id;

	public CompletionStub (final String id, final EndpointReferenceType completionCoordinator)
			throws Exception
	{	
		_completionCoordinator = completionCoordinator;
		_id = id;
	}

	public void commit () throws TransactionRolledBackException,
			UnknownTransactionException, SystemException
	{
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_completionCoordinator, MessageId.getMessageId()) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final CompletionInitiatorProcessor completionInitiator = CompletionInitiatorProcessor.getProcessor() ;
        completionInitiator.registerCallback(_id, callback) ;
        try
        {
            CompletionCoordinatorClient.getClient().sendCommit(addressingContext, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
        finally
        {
            completionInitiator.removeCallback(_id) ;
        }
        
        if (callback.hasTriggered())
        {
            if (callback.receivedCommitted())
            {
                return ;
            }
            else if (callback.receivedAborted())
            {
                throw new TransactionRolledBackException() ;
            }
            final SoapFault soapFault = callback.getSoapFault() ;
            if ((soapFault != null) && ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
            {
                throw new UnknownTransactionException();
            }
        }
        
        throw new SystemException() ;
	}

	public void rollback () throws UnknownTransactionException, SystemException
	{
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_completionCoordinator, MessageId.getMessageId()) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final CompletionInitiatorProcessor completionInitiator = CompletionInitiatorProcessor.getProcessor() ;
        completionInitiator.registerCallback(_id, callback) ;
        try
        {
            CompletionCoordinatorClient.getClient().sendRollback(addressingContext, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
        finally
        {
            completionInitiator.removeCallback(_id) ;
        }
        
        if (callback.hasTriggered())
        {
            if (callback.receivedAborted())
            {
                return ;
            }
            final SoapFault soapFault = callback.getSoapFault() ;
            if ((soapFault != null) && ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
            {
                throw new UnknownTransactionException();
            }
        }
        
        throw new SystemException() ;
	}
    
    private static class RequestCallback extends CompletionInitiatorCallback
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
         * The aborted notification flag.
         */
        private boolean aborted ;
        /**
         * The committed notification flag.
         */
        private boolean committed ;
        
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
         * Did we receive a aborted notification?
         * @return True if aborted, false otherwise.
         */
        boolean receivedAborted()
        {
            return aborted ;
        }
        
        /**
         * Did we receive a committed notification?
         * @return True if committed, false otherwise.
         */
        boolean receivedCommitted()
        {
            return committed ;
        }
        
        /**
         * A aborted response.
         * @param aborted The aborted notification.
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void aborted(final NotificationType aborted, final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.aborted = true ;
            this.addressingContext = addressingContext ;
            this.arjunaContext = arjunaContext ;
        }

        /**
         * An committed response.
         * @param committed The committed notification.
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void committed(final NotificationType committed, final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.committed  = true ;
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
