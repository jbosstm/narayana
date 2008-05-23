/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
package com.arjuna.wst11.tests.junit;

import java.util.HashMap;
import java.util.Map;

import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsba.CoordinatorCompletionCoordinatorInboundEvents;
import com.arjuna.webservices11.wsba.processors.CoordinatorCompletionCoordinatorProcessor;
import com.arjuna.webservices.SoapFault;
import org.oasis_open.docs.ws_tx.wsba._2006._06.ExceptionType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

import javax.xml.ws.addressing.AddressingProperties;


public class TestCoordinatorCompletionCoordinatorProcessor extends CoordinatorCompletionCoordinatorProcessor
{
    private Map messageIdMap = new HashMap() ;

    public CoordinatorCompletionCoordinatorDetails getCoordinatorCompletionCoordinatorDetails(final String messageId, final long timeout)
    {
        final long endTime = System.currentTimeMillis() + timeout ;
        synchronized(messageIdMap)
        {
            long now = System.currentTimeMillis() ;
            while(now < endTime)
            {
                final CoordinatorCompletionCoordinatorDetails details = (CoordinatorCompletionCoordinatorDetails)messageIdMap.remove(messageId) ;
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
            final CoordinatorCompletionCoordinatorDetails details = (CoordinatorCompletionCoordinatorDetails)messageIdMap.remove(messageId) ;
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
    public void activateCoordinator(CoordinatorCompletionCoordinatorInboundEvents coordinator, String identifier) {
    }

    /**
     * Deactivate the coordinator.
     *
     * @param coordinator The coordinator.
     */
    public void deactivateCoordinator(CoordinatorCompletionCoordinatorInboundEvents coordinator) {
    }

    /**
     * Cancelled.
     *
     * @param cancelled         The cancelled notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void cancelled(NotificationType cancelled, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final CoordinatorCompletionCoordinatorDetails details = new CoordinatorCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setCancelled(true); ;

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
     * @param addressingProperties The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void closed(NotificationType closed, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final CoordinatorCompletionCoordinatorDetails details = new CoordinatorCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setClosed(true); ;

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
     * @param addressingProperties The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void compensated(NotificationType compensated, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final CoordinatorCompletionCoordinatorDetails details = new CoordinatorCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setCompensated(true); ;

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
     * @param addressingProperties The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void completed(NotificationType completed, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final CoordinatorCompletionCoordinatorDetails details = new CoordinatorCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setCompleted(true); ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void exit(NotificationType exit, AddressingProperties addressingProperties, ArjunaContext arjunaContext)
    {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final CoordinatorCompletionCoordinatorDetails details = new CoordinatorCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setExit(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Cannot complete.
     *
     * @param cannotComplete       The cannot complete notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext        The arjuna context.
     */
    public void cannotComplete(NotificationType cannotComplete, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Fault.
     *
     * @param fault             The fault exception.
     * @param addressingProperties The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void fail(ExceptionType fault, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final CoordinatorCompletionCoordinatorDetails details = new CoordinatorCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setFail(fault); ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void getStatus(NotificationType getStatus, AddressingProperties addressingProperties, ArjunaContext arjunaContext)
    {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final CoordinatorCompletionCoordinatorDetails details = new CoordinatorCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
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
     * @param addressingProperties The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void status(StatusType status, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final CoordinatorCompletionCoordinatorDetails details = new CoordinatorCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setStatus(status); ;

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
     * @param addressingProperties The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void soapFault(SoapFault soapFault, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final CoordinatorCompletionCoordinatorDetails details = new CoordinatorCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setSoapFault(soapFault); ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public static class CoordinatorCompletionCoordinatorDetails
    {
        private final AddressingProperties addressingProperties ;
        private final ArjunaContext arjunaContext ;
        private boolean closed ;
        private boolean cancelled ;
        private boolean compensated ;
        private ExceptionType fail;
        private boolean completed ;
        private boolean cannotComplete ;
        private StatusType status ;
        private SoapFault soapFault ;
        private boolean exit ;
        private boolean getStatus ;

        CoordinatorCompletionCoordinatorDetails(final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
        {
            this.addressingProperties = addressingProperties ;
            this.arjunaContext = arjunaContext ;
        }

        public AddressingProperties getAddressingProperties()
        {
            return addressingProperties ;
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
        public boolean hasClosed() {
            return closed;
        }

        public void setClosed(boolean closed) {
            this.closed = closed;
        }

        public boolean hasCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        public boolean hasCompensated() {
            return compensated;
        }

        public void setCompensated(boolean compensated) {
            this.compensated = compensated;
        }

        public ExceptionType hasFail() {
            return fail;
        }

        public void setFail(ExceptionType fail) {
            this.fail = fail;
        }

        public boolean hasCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public boolean hasCAnnotComplete() {
            return cannotComplete;
        }

        public void setCannotComplete(boolean cannotComplete) {
            this.cannotComplete = cannotComplete;
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