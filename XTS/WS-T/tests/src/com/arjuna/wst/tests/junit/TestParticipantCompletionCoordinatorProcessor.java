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
package com.arjuna.wst.tests.junit;

import java.util.HashMap;
import java.util.Map;

import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.ParticipantCompletionCoordinatorInboundEvents;
import com.arjuna.webservices.wsba.ExceptionType;
import com.arjuna.webservices.wsba.StatusType;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionCoordinatorProcessor;
import com.arjuna.webservices.SoapFault;


public class TestParticipantCompletionCoordinatorProcessor extends ParticipantCompletionCoordinatorProcessor
{
    private Map messageIdMap = new HashMap() ;
    
    public ParticipantCompletionCoordinatorDetails getParticipantCompletionCoordinatorDetails(final String messageId, final long timeout)
    {
        final long endTime = System.currentTimeMillis() + timeout ;
        synchronized(messageIdMap)
        {
            long now = System.currentTimeMillis() ;
            while(now < endTime)
            {
                final ParticipantCompletionCoordinatorDetails details = (ParticipantCompletionCoordinatorDetails)messageIdMap.remove(messageId) ;
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
            final ParticipantCompletionCoordinatorDetails details = (ParticipantCompletionCoordinatorDetails)messageIdMap.remove(messageId) ;
            if (details != null)
            {
                return details ;
            }
        }
        throw new NullPointerException("Timeout occurred waiting for id: " + messageId) ;
    }

    /**
     * Activate the coordinator.
     *
     * @param coordinator The coordinator.
     * @param identifier       The identifier.
     */
    public void activateCoordinator(ParticipantCompletionCoordinatorInboundEvents coordinator, String identifier) {
    }

    /**
     * Deactivate the coordinator.
     *
     * @param coordinator The coordinator.
     */
    public void deactivateCoordinator(ParticipantCompletionCoordinatorInboundEvents coordinator) {
    }

    /**
     * Cancelled.
     *
     * @param cancelled         The cancelled notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void cancelled(NotificationType cancelled, AddressingContext addressingContext, ArjunaContext arjunaContext) {
        final String messageId = addressingContext.getMessageID().getValue() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingContext, arjunaContext) ;
        details.setCancelled(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Closed.
     *
     * @param closed            The closed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void closed(NotificationType closed, AddressingContext addressingContext, ArjunaContext arjunaContext) {
        final String messageId = addressingContext.getMessageID().getValue() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingContext, arjunaContext) ;
        details.setClosed(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Compensated.
     *
     * @param compensated       The compensated notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void compensated(NotificationType compensated, AddressingContext addressingContext, ArjunaContext arjunaContext) {
        final String messageId = addressingContext.getMessageID().getValue() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingContext, arjunaContext) ;
        details.setCompensated(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Completed.
     *
     * @param completed         The completed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void completed(NotificationType completed, AddressingContext addressingContext, ArjunaContext arjunaContext) {
        final String messageId = addressingContext.getMessageID().getValue() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingContext, arjunaContext) ;
        details.setCompleted(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void exit(NotificationType exit, AddressingContext addressingContext, ArjunaContext arjunaContext)
    {
        final String messageId = addressingContext.getMessageID().getValue() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingContext, arjunaContext) ;
        details.setExit(true) ;
        
        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Fault.
     *
     * @param fault             The fault exception.
     * @param addressingContext The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void fault(ExceptionType fault, AddressingContext addressingContext, ArjunaContext arjunaContext) {
        final String messageId = addressingContext.getMessageID().getValue() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingContext, arjunaContext) ;
        details.setFault(fault) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void getStatus(NotificationType getStatus, AddressingContext addressingContext, ArjunaContext arjunaContext)
    {
        final String messageId = addressingContext.getMessageID().getValue() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingContext, arjunaContext) ;
        details.setGetStatus(true) ;
        
        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Status.
     *
     * @param status            The status.
     * @param addressingContext The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void status(StatusType status, AddressingContext addressingContext, ArjunaContext arjunaContext) {
        final String messageId = addressingContext.getMessageID().getValue() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingContext, arjunaContext) ;
        details.setStatus(status) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * SOAP fault.
     *
     * @param soapFault         The SOAP fault.
     * @param addressingContext The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void soapFault(SoapFault soapFault, AddressingContext addressingContext, ArjunaContext arjunaContext) {
        final String messageId = addressingContext.getMessageID().getValue() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingContext, arjunaContext) ;
        details.setSoapFault(soapFault) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public static class ParticipantCompletionCoordinatorDetails
    {
        private final AddressingContext addressingContext ;
        private final ArjunaContext arjunaContext ;
        private boolean exit ;
        private boolean getStatus ;
        private boolean cancelled;
        private boolean closed;
        private boolean compensated;
        private boolean completed;
        private ExceptionType fault;
        private StatusType status;
        private SoapFault soapFault;

        ParticipantCompletionCoordinatorDetails(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
        {
            this.addressingContext = addressingContext ;
            this.arjunaContext = arjunaContext ;
        }
        
        public AddressingContext getAddressingContext()
        {
            return addressingContext ;
        }
        
        public ArjunaContext getArjunaContext()
        {
            return arjunaContext ;
        }
        
        public boolean hasExit()
        {
            return exit ;
        }
        
        void setExit(final boolean exit)
        {
            this.exit = exit ;
        }
        
        public boolean hasGetStatus()
        {
            return getStatus ;
        }
        
        void setGetStatus(final boolean getStatus)
        {
            this.getStatus = getStatus ;
        }

        public boolean hasCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        public boolean hasClosed() {
            return closed;
        }

        public void setClosed(boolean closed) {
            this.closed = closed;
        }

        public boolean hasCompensated() {
            return compensated;
        }

        public void setCompensated(boolean compensated) {
            this.compensated = compensated;
        }

        public boolean hasCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public ExceptionType hasFault() {
            return fault;
        }

        public void setFault(ExceptionType fault) {
            this.fault = fault;
        }

        public StatusType hasStatus() {
            return status;
        }

        public void setStatus(StatusType status) {
            this.status = status;
        }

        public SoapFault hasSoapFault() {
            return soapFault;
        }

        public void setSoapFault(SoapFault soapFault) {
            this.soapFault = soapFault;
        }
    }
}
