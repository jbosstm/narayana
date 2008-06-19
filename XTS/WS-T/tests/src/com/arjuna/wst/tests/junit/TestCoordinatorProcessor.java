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
package com.arjuna.wst.tests.junit;

import java.util.HashMap;
import java.util.Map;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsat.NotificationType;
import com.arjuna.webservices.wsat.ParticipantInboundEvents;
import com.arjuna.webservices.wsat.CoordinatorInboundEvents;
import com.arjuna.webservices.wsat.processors.ParticipantProcessor;
import com.arjuna.webservices.wsat.processors.CoordinatorProcessor;

public class TestCoordinatorProcessor extends CoordinatorProcessor
{
    private Map messageIdMap = new HashMap() ;

    public CoordinatorDetails getCoordinatorDetails(final String messageId, final long timeout)
    {
        final long endTime = System.currentTimeMillis() + timeout ;
        synchronized(messageIdMap)
        {
            long now = System.currentTimeMillis() ;
            while(now < endTime)
            {
                final CoordinatorDetails details = (CoordinatorDetails)messageIdMap.remove(messageId) ;
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
            final CoordinatorDetails details = (CoordinatorDetails)messageIdMap.remove(messageId) ;
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
     * @param coordinator The participant.
     * @param identifier  The identifier.
     */
    public void activateCoordinator(CoordinatorInboundEvents coordinator, String identifier) {
    }

    /**
     * Deactivate the participant.
     *
     * @param coordinator The participant.
     */
    public void deactivateCoordinator(CoordinatorInboundEvents coordinator, boolean leaveGhost) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void aborted(NotificationType aborted,
            AddressingContext addressingContext, ArjunaContext arjunaContext)
    {
        final String messageId = addressingContext.getMessageID().getValue() ;
        final CoordinatorDetails details = new CoordinatorDetails(addressingContext, arjunaContext) ;
        details.setAborted(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void committed(NotificationType committed,
            AddressingContext addressingContext, ArjunaContext arjunaContext)
    {
        final String messageId = addressingContext.getMessageID().getValue() ;
        final CoordinatorDetails details = new CoordinatorDetails(addressingContext, arjunaContext) ;
        details.setCommitted(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void prepared(NotificationType prepared,
            AddressingContext addressingContext, ArjunaContext arjunaContext)
    {
        final String messageId = addressingContext.getMessageID().getValue() ;
        final CoordinatorDetails details = new CoordinatorDetails(addressingContext, arjunaContext) ;
        details.setPrepared(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void readOnly(NotificationType readOnly,
            AddressingContext addressingContext, ArjunaContext arjunaContext)
    {
        final String messageId = addressingContext.getMessageID().getValue() ;
        final CoordinatorDetails details = new CoordinatorDetails(addressingContext, arjunaContext) ;
        details.setReadOnly(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void replay(NotificationType replay,
            AddressingContext addressingContext, ArjunaContext arjunaContext)
    {
        final String messageId = addressingContext.getMessageID().getValue() ;
        final CoordinatorDetails details = new CoordinatorDetails(addressingContext, arjunaContext) ;
        details.setReplay(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void soapFault(SoapFault soapFault, AddressingContext addressingContext,
            ArjunaContext arjunaContext)
    {
        final String messageId = addressingContext.getMessageID().getValue() ;
        final CoordinatorDetails details = new CoordinatorDetails(addressingContext, arjunaContext) ;
        details.setSoapFault(soapFault) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public static class CoordinatorDetails
    {
        private final AddressingContext addressingContext ;
        private final ArjunaContext arjunaContext ;
        private boolean aborted ;
        private boolean committed ;
        private boolean prepared ;
        private boolean readOnly ;
        private boolean replay ;
        private SoapFault soapFault ;

        CoordinatorDetails(final AddressingContext addressingContext, final ArjunaContext arjunaContext)
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

        public boolean hasAborted()
        {
            return aborted ;
        }

        void setAborted(final boolean aborted)
        {
            this.aborted = aborted ;
        }

        public boolean hasCommitted()
        {
            return committed ;
        }

        void setCommitted(final boolean committed)
        {
            this.committed = committed ;
        }

        public boolean hasPrepared()
        {
            return prepared ;
        }

        void setPrepared(final boolean prepared)
        {
            this.prepared = prepared ;
        }

        public boolean hasReadOnly() {
            return readOnly;
        }

        public void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }

        public boolean hasReplay() {
            return replay;
        }

        public void setReplay(boolean replay) {
            this.replay = replay;
        }

        public SoapFault hasSoapFault()
        {
            return soapFault ;
        }

        void setSoapFault(final SoapFault soapFault)
        {
            this.soapFault = soapFault ;
        }
    }
}