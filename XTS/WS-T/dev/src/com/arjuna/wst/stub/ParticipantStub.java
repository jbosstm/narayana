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
 * Copyright (c) 2003, 2004,
 *
 * Arjuna Technologies Limited.
 *
 * $Id: ParticipantStub.java,v 1.1.2.1 2005/11/22 10:35:28 kconner Exp $
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
import com.arjuna.webservices.wsat.NotificationType;
import com.arjuna.webservices.wsat.Participant;
import com.arjuna.webservices.wsat.client.ParticipantClient;
import com.arjuna.webservices.wsat.processors.CoordinatorCallback;
import com.arjuna.webservices.wsat.processors.CoordinatorProcessor;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.Aborted;
import com.arjuna.wst.Prepared;
import com.arjuna.wst.ReadOnly;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;

public class ParticipantStub implements Participant
{
    private final EndpointReferenceType _twoPCParticipant ;
    private final String _id ;
    
    public ParticipantStub(final String id, final EndpointReferenceType twoPCParticipant)
        throws Exception
    {
        _twoPCParticipant = twoPCParticipant;
        _id = id ;
    }

    public Vote prepare()
        throws WrongStateException, SystemException
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_twoPCParticipant, MessageId.getMessageId()) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final CoordinatorProcessor coordinator = CoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(_id, callback) ;
        try
        {
            ParticipantClient.getClient().sendPrepare(addressingContext, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
        finally
        {
            coordinator.removeCallback(_id) ;
        }
        
        if (callback.hasTriggered())
        {
            if (callback.receivedPrepared())
            {
                return new Prepared() ;
            }
            else if (callback.receivedReadOnly())
            {
                return new ReadOnly() ;
            }
            else if (callback.receivedAborted())
            {
                return new Aborted() ;
            }
            final SoapFault soapFault = callback.getSoapFault() ;
            if ((soapFault != null) && ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
            {
                throw new WrongStateException() ;
            }
        }
        
        throw new SystemException() ;
    }

    public void commit()
        throws WrongStateException, SystemException
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_twoPCParticipant, MessageId.getMessageId()) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final CoordinatorProcessor coordinator = CoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(_id, callback) ;
        try
        {
            ParticipantClient.getClient().sendCommit(addressingContext, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
        finally
        {
            coordinator.removeCallback(_id) ;
        }
        
        if (callback.hasTriggered())
        {
            if (callback.receivedCommitted())
            {
                return ;
            }
            else if (callback.receivedReplay())
            {
                commit() ;
                return ;
            }
            final SoapFault soapFault = callback.getSoapFault() ;
            if ((soapFault != null) && ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
            {
                throw new WrongStateException() ;
            }
        }
        
        throw new SystemException() ;
    }

    public void rollback()
        throws WrongStateException, SystemException
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_twoPCParticipant, MessageId.getMessageId()) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final CoordinatorProcessor coordinator = CoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(_id, callback) ;
        try
        {
            ParticipantClient.getClient().sendRollback(addressingContext, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
        finally
        {
            coordinator.removeCallback(_id) ;
        }
        
        if (callback.hasTriggered())
        {
            if (callback.receivedAborted())
            {
                return ;
            }
            else if (callback.receivedPrepared() || callback.receivedReplay())
            {
                rollback() ;
                return ;
            }
            else if (callback.receivedReplay())
            {
                commit() ;
            }
            final SoapFault soapFault = callback.getSoapFault() ;
            if ((soapFault != null) && ArjunaTXConstants.WRONGSTATE_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
            {
                throw new WrongStateException() ;
            }
        }
        
        throw new SystemException() ;
    }

    public void unknown()
        throws SystemException
    {
        error() ;
    }

    /**
     * @message com.arjuna.wst.stub.ParticipantStub_1 [com.arjuna.wst.stub.ParticipantStub_1] - Unknown error
     */
    public void error()
        throws SystemException
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(_twoPCParticipant, MessageId.getMessageId()) ;
        final SoapFault soapFault = new SoapFault(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME,
                WSTLogger.log_mesg.getString("com.arjuna.wst.stub.ParticipantStub_1")) ;
        try
        {
            ParticipantClient.getClient().sendSoapFault(addressingContext, soapFault, new InstanceIdentifier(_id)) ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
    }

    private static final class RequestCallback extends CoordinatorCallback
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
         * The prepared notification flag.
         */
        private boolean prepared ;
        /**
         * The read only notification flag.
         */
        private boolean readOnly ;
        /**
         * The replay notification flag.
         */
        private boolean replay ;
        
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
         * Did we receive a prepared notification?
         * @return True if prepared, false otherwise.
         */
        boolean receivedPrepared()
        {
            return prepared ;
        }
        
        /**
         * Did we receive a read only notification?
         * @return True if read only, false otherwise.
         */
        boolean receivedReadOnly()
        {
            return readOnly ;
        }
        
        /**
         * Did we receive a replay notification?
         * @return True if replay, false otherwise.
         */
        boolean receivedReplay()
        {
            return replay ;
        }
        
        /**
         * An aborted response.
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
         * A committed response.
         * @param committed The committed notification.
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void committed(final NotificationType committed, final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.committed = true ;
            this.addressingContext = addressingContext ;
            this.arjunaContext = arjunaContext ;
        }
        
        /**
         * A prepared response.
         * @param prepared The prepared notification.
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void prepared(final NotificationType prepared, final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.prepared = true ;
            this.addressingContext = addressingContext ;
            this.arjunaContext = arjunaContext ;
        }
        
        /**
         * A read only response.
         * @param readOnly The read only notification.
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void readOnly(final NotificationType readOnly, final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.readOnly = true ;
            this.addressingContext = addressingContext ;
            this.arjunaContext = arjunaContext ;
        }
        
        /**
         * A replay response.
         * @param replay The replay notification.
         * @param addressingContext The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void replay(final NotificationType replay, final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.replay = true ;
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
