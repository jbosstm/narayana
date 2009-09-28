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

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsat.ParticipantInboundEvents;
import com.arjuna.webservices11.wsat.processors.ParticipantProcessor;
import org.jboss.wsf.common.addressing.MAP;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

public class TestParticipantProcessor extends ParticipantProcessor
{
    private Map messageIdMap = new HashMap() ;

    public ParticipantDetails getParticipantDetails(final String messageId, final long timeout)
    {
        final long endTime = System.currentTimeMillis() + timeout ;
        synchronized(messageIdMap)
        {
            long now = System.currentTimeMillis() ;
            while(now < endTime)
            {
                final ParticipantDetails details = (ParticipantDetails)messageIdMap.remove(messageId) ;
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
            final ParticipantDetails details = (ParticipantDetails)messageIdMap.remove(messageId) ;
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
    public void activateParticipant(ParticipantInboundEvents participant, String identifier) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Deactivate the participant.
     *
     * @param participant The participant.
     */
    public void deactivateParticipant(ParticipantInboundEvents participant) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Check whether a participant with the given id is currently active
     *
     * @param identifier The identifier.
     */
    public boolean isActive(String identifier) {
        return true;
    }

    public void commit(Notification commit,
            MAP map, ArjunaContext arjunaContext)
    {
        final String messageId = map.getMessageID() ;
        final ParticipantDetails details = new ParticipantDetails(map, arjunaContext) ;
        details.setCommit(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void prepare(Notification prepare,
            MAP map, ArjunaContext arjunaContext)
    {
        final String messageId = map.getMessageID() ;
        final ParticipantDetails details = new ParticipantDetails(map, arjunaContext) ;
        details.setPrepare(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void rollback(Notification rollback,
            MAP map, ArjunaContext arjunaContext)
    {
        final String messageId = map.getMessageID() ;
        final ParticipantDetails details = new ParticipantDetails(map, arjunaContext) ;
        details.setRollback(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void soapFault(SoapFault soapFault, MAP map,
            ArjunaContext arjunaContext)
    {
        final String messageId = map.getMessageID() ;
        final ParticipantDetails details = new ParticipantDetails(map, arjunaContext) ;
        details.setSoapFault(soapFault) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public static class ParticipantDetails
    {
        private final MAP map ;
        private final ArjunaContext arjunaContext ;
        private boolean commit ;
        private boolean prepare ;
        private boolean rollback ;
        private SoapFault soapFault ;

        ParticipantDetails(final MAP map, final ArjunaContext arjunaContext)
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

        public boolean hasCommit()
        {
            return commit ;
        }

        void setCommit(final boolean commit)
        {
            this.commit = commit ;
        }

        public boolean hasPrepare()
        {
            return prepare ;
        }

        void setPrepare(final boolean prepare)
        {
            this.prepare = prepare ;
        }

        public boolean hasRollback()
        {
            return rollback ;
        }

        void setRollback(final boolean rollback)
        {
            this.rollback = rollback ;
        }

        public SoapFault getSoapFault()
        {
            return soapFault ;
        }

        void setSoapFault(final SoapFault soapFault)
        {
            this.soapFault = soapFault ;
        }
    }
}