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
import com.arjuna.webservices11.wsba.CoordinatorCompletionParticipantInboundEvents;
import com.arjuna.webservices11.wsba.processors.CoordinatorCompletionParticipantProcessor;
import org.jboss.jbossts.xts.wsaddr.map.MAP;
import com.arjuna.webservices.SoapFault;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

public class TestCoordinatorCompletionParticipantProcessor extends CoordinatorCompletionParticipantProcessor
{
    private Map messageIdMap = new HashMap() ;

    public CoordinatorCompletionParticipantDetails getCoordinatorCompletionParticipantDetails(final String messageId, final long timeout)
    {
        final long endTime = System.currentTimeMillis() + timeout ;
        synchronized(messageIdMap)
        {
            long now = System.currentTimeMillis() ;
            while(now < endTime)
            {
                final CoordinatorCompletionParticipantDetails details = (CoordinatorCompletionParticipantDetails)messageIdMap.remove(messageId) ;
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
            final CoordinatorCompletionParticipantDetails details = (CoordinatorCompletionParticipantDetails)messageIdMap.remove(messageId) ;
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
    public void activateParticipant(CoordinatorCompletionParticipantInboundEvents participant, String identifier) {
    }

    /**
     * Deactivate the participant.
     *
     * @param participant The participant.
     */
    public void deactivateParticipant(CoordinatorCompletionParticipantInboundEvents participant) {
    }

    /**
     * Check whether a participant with the given id is currently active
     *
     * @param identifier The identifier.
     */
    public boolean isActive(String identifier) {
        return false;
    }

    public void cancel(NotificationType cancel, MAP map, ArjunaContext arjunaContext)
    {
        final String messageId = map.getMessageID() ;
        final CoordinatorCompletionParticipantDetails details = new CoordinatorCompletionParticipantDetails(map, arjunaContext) ;
        details.setCancel(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void close(NotificationType close, MAP map, ArjunaContext arjunaContext)
    {
        final String messageId = map.getMessageID() ;
        final CoordinatorCompletionParticipantDetails details = new CoordinatorCompletionParticipantDetails(map, arjunaContext) ;
        details.setClose(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void compensate(NotificationType compensate, MAP map, ArjunaContext arjunaContext)
    {
        final String messageId = map.getMessageID() ;
        final CoordinatorCompletionParticipantDetails details = new CoordinatorCompletionParticipantDetails(map, arjunaContext) ;
        details.setCompensate(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void complete(NotificationType complete, MAP map, ArjunaContext arjunaContext)
    {
        final String messageId = map.getMessageID() ;
        final CoordinatorCompletionParticipantDetails details = new CoordinatorCompletionParticipantDetails(map, arjunaContext) ;
        details.setComplete(true) ;

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
     * @param map The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void exited(NotificationType exited, MAP map, ArjunaContext arjunaContext) {
        final String messageId = map.getMessageID() ;
        final CoordinatorCompletionParticipantDetails details = new CoordinatorCompletionParticipantDetails(map, arjunaContext) ;
        details.setExited(true) ;

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
     * @param map The addressing context.
     * @param arjunaContext        The arjuna context.
     */
    public void notCompleted(NotificationType notCompleted, MAP map, ArjunaContext arjunaContext) {
        final String messageId = map.getMessageID() ;
        final CoordinatorCompletionParticipantDetails details = new CoordinatorCompletionParticipantDetails(map, arjunaContext) ;
        details.setNotCompleted(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Faulted.
     *
     * @param failed           The failed notification.
     * @param map The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void failed(NotificationType failed, MAP map, ArjunaContext arjunaContext) {
        final String messageId = map.getMessageID() ;
        final CoordinatorCompletionParticipantDetails details = new CoordinatorCompletionParticipantDetails(map, arjunaContext) ;
        details.setFailed(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void getStatus(NotificationType getStatus, MAP map, ArjunaContext arjunaContext)
    {
        final String messageId = map.getMessageID() ;
        final CoordinatorCompletionParticipantDetails details = new CoordinatorCompletionParticipantDetails(map, arjunaContext) ;
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
     * @param map The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void status(StatusType status, MAP map, ArjunaContext arjunaContext) {
        final String messageId = map.getMessageID() ;
        final CoordinatorCompletionParticipantDetails details = new CoordinatorCompletionParticipantDetails(map, arjunaContext) ;
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
     * @param map The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void soapFault(SoapFault soapFault, MAP map, ArjunaContext arjunaContext) {
        final String messageId = map.getMessageID() ;
        final CoordinatorCompletionParticipantDetails details = new CoordinatorCompletionParticipantDetails(map, arjunaContext) ;
        details.setSoapFault(soapFault) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void activateParticipant(BusinessAgreementWithCoordinatorCompletionParticipant participant, String identifier)
    {
    }

    public void deactivateParticipant(BusinessAgreementWithCoordinatorCompletionParticipant participant)
    {
    }

    public static class CoordinatorCompletionParticipantDetails
    {
        private final MAP map ;
        private final ArjunaContext arjunaContext ;
        private boolean cancel ;
        private boolean close ;
        private boolean compensate ;
        private boolean complete ;
        private boolean failed;
        private boolean exited ;
        private boolean notCompleted ;
        private StatusType status ;
        private boolean getStatus ;
        private SoapFault soapFault ;

        CoordinatorCompletionParticipantDetails(final MAP map, final ArjunaContext arjunaContext)
        {
            this.map = map ;
            this.arjunaContext = arjunaContext ;
        }

        public MAP getMAP()
        {
            return map ;
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

        public boolean hasComplete()
        {
            return complete ;
        }

        void setComplete(final boolean complete)
        {
            this.complete = complete ;
        }

        public boolean hasGetStatus()
        {
            return getStatus ;
        }

        void setGetStatus(final boolean getStatus)
        {
            this.getStatus = getStatus ;
        }
        public boolean hasFailed() {
            return failed;
        }

        public void setFailed(boolean failed) {
            this.failed = failed;
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

        public SoapFault hasSoapFault() {
            return soapFault;
        }

        public void setSoapFault(SoapFault soapFault) {
            this.soapFault = soapFault;
        }
    }
}