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
package com.arjuna.wst11.tests.junit;

import java.util.HashMap;
import java.util.Map;

import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsba.ParticipantCompletionParticipantInboundEvents;
import com.arjuna.webservices11.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.webservices.SoapFault;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

import javax.xml.ws.addressing.AddressingProperties;


public class TestParticipantCompletionParticipantProcessor extends ParticipantCompletionParticipantProcessor
{
    private Map messageIdMap = new HashMap() ;

    public ParticipantCompletionParticipantDetails getParticipantCompletionParticipantDetails(final String messageId, final long timeout)
    {
        final long endTime = System.currentTimeMillis() + timeout ;
        synchronized(messageIdMap)
        {
            long now = System.currentTimeMillis() ;
            while(now < endTime)
            {
                final ParticipantCompletionParticipantDetails details = (ParticipantCompletionParticipantDetails)messageIdMap.remove(messageId) ;
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
            final ParticipantCompletionParticipantDetails details = (ParticipantCompletionParticipantDetails)messageIdMap.remove(messageId) ;
            if (details != null)
            {
                return details ;
            }
        }
        throw new NullPointerException("Timeout occurred waiting for id: " + messageId) ;
    }

    /**
     * Activate the participant.
     *
     * @param participant The participant.
     * @param identifier  The identifier.
     */
    public void activateParticipant(ParticipantCompletionParticipantInboundEvents participant, String identifier) {
    }

    /**
     * Deactivate the participant.
     *
     * @param participant The participant.
     */
    public void deactivateParticipant(ParticipantCompletionParticipantInboundEvents participant) {
    }

    /**
     * Check whether a participant with the given id is currently active
     *
     * @param identifier The identifier.
     */
    public boolean isActive(String identifier) {
        return false;
    }

    public void cancel(NotificationType cancel, AddressingProperties addressingProperties, ArjunaContext arjunaContext)
    {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionParticipantDetails details = new ParticipantCompletionParticipantDetails(addressingProperties, arjunaContext) ;
        details.setCancel(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void close(NotificationType close, AddressingProperties addressingProperties, ArjunaContext arjunaContext)
    {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionParticipantDetails details = new ParticipantCompletionParticipantDetails(addressingProperties, arjunaContext) ;
        details.setClose(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void compensate(NotificationType compensate, AddressingProperties addressingProperties, ArjunaContext arjunaContext)
    {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionParticipantDetails details = new ParticipantCompletionParticipantDetails(addressingProperties, arjunaContext) ;
        details.setCompensate(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Exited.
     *
     * @param exited            The exited notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void exited(NotificationType exited, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionParticipantDetails details = new ParticipantCompletionParticipantDetails(addressingProperties, arjunaContext) ;
        details.setExited(true); ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Not Completed.
     *
     * @param notCompleted         The not completed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext        The arjuna context.
     */
    public void notCompleted(NotificationType notCompleted, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionParticipantDetails details = new ParticipantCompletionParticipantDetails(addressingProperties, arjunaContext) ;
        details.setNotCompleted(true); ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Faulted.
     *
     * @param faulted           The faulted notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void failed(NotificationType faulted, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionParticipantDetails details = new ParticipantCompletionParticipantDetails(addressingProperties, arjunaContext) ;
        details.setFaulted(true); ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void getStatus(NotificationType getStatus, AddressingProperties addressingProperties, ArjunaContext arjunaContext)
    {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionParticipantDetails details = new ParticipantCompletionParticipantDetails(addressingProperties, arjunaContext) ;
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
        final ParticipantCompletionParticipantDetails details = new ParticipantCompletionParticipantDetails(addressingProperties, arjunaContext) ;
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
        final ParticipantCompletionParticipantDetails details = new ParticipantCompletionParticipantDetails(addressingProperties, arjunaContext) ;
        details.setSoapFault(soapFault) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
        details.setSoapFault(soapFault);
    }

    public static class ParticipantCompletionParticipantDetails
    {
        private final AddressingProperties addressingProperties ;
        private final ArjunaContext arjunaContext ;
        private boolean cancel ;
        private boolean close ;
        private boolean compensate ;
        private boolean getStatus ;
        private boolean faulted;
        private boolean exited;
        private boolean notCompleted;
        private StatusType status;
        private SoapFault soapFault;

        ParticipantCompletionParticipantDetails(final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
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

        public boolean hasCancel()
        {
            return cancel ;
        }

        void setCancel(final boolean cancel)
        {
            this.cancel = cancel ;
        }

        public boolean hasClose()
        {
            return close ;
        }

        void setClose(final boolean close)
        {
            this.close = close ;
        }

        public boolean hasCompensate()
        {
            return compensate ;
        }

        void setCompensate(final boolean compensate)
        {
            this.compensate = compensate ;
        }

        public boolean hasGetStatus()
        {
            return getStatus ;
        }

        void setGetStatus(final boolean getStatus)
        {
            this.getStatus = getStatus ;
        }

        public boolean hasFaulted() {
            return faulted;
        }

        public void setFaulted(boolean faulted) {
            this.faulted = faulted;
        }

        public boolean hasExited() {
            return exited;
        }

        public void setExited(boolean exited) {
            this.exited = exited;
        }

        public boolean hasNotCompleted() {
            return notCompleted;
        }

        public void setNotCompleted(boolean notCompleted) {
            this.notCompleted = notCompleted;
        }

        public StatusType hasStatus() {
            return status;
        }
        public void setStatus(StatusType status) {
            this.status = status;
        }

        public SoapFault getSoapFault() {
            return soapFault;
        }

        public void setSoapFault(SoapFault soapFault) {
            this.soapFault = soapFault;
        }
    }
}