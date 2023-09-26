/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.wst.tests.arq;

import java.util.HashMap;
import java.util.Map;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsat.ParticipantInboundEvents;
import com.arjuna.webservices11.wsat.processors.ParticipantProcessor;
import org.jboss.ws.api.addressing.MAP;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

public class TestParticipantProcessor extends ParticipantProcessor
{
    private Map<String,ParticipantDetails> messageIdMap = new HashMap<>() ;

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