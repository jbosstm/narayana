/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.wst.tests.arq;

import java.util.HashMap;
import java.util.Map;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsat.CoordinatorInboundEvents;
import com.arjuna.webservices11.wsat.processors.CoordinatorProcessor;
import org.jboss.ws.api.addressing.MAP;

import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

public class TestCoordinatorProcessor extends CoordinatorProcessor
{
    private Map<String,CoordinatorDetails> messageIdMap = new HashMap<>() ;

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
    public void deactivateCoordinator(CoordinatorInboundEvents coordinator) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Fetch the coordinator with a given identifier.
     *
     * @param identifier The identifier.
     */
    public CoordinatorInboundEvents getCoordinator(String identifier) {
        return null;
    }

    public void aborted(Notification aborted,
            MAP map, ArjunaContext arjunaContext)
    {
        final String messageId = map.getMessageID() ;
        final CoordinatorDetails details = new CoordinatorDetails(map, arjunaContext) ;
        details.setAborted(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void committed(Notification committed,
            MAP map, ArjunaContext arjunaContext)
    {
        final String messageId = map.getMessageID() ;
        final CoordinatorDetails details = new CoordinatorDetails(map, arjunaContext) ;
        details.setCommitted(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void prepared(Notification prepared,
            MAP map, ArjunaContext arjunaContext)
    {
        final String messageId = map.getMessageID() ;
        final CoordinatorDetails details = new CoordinatorDetails(map, arjunaContext) ;
        details.setPrepared(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void readOnly(Notification readOnly,
            MAP map, ArjunaContext arjunaContext)
    {
        final String messageId = map.getMessageID() ;
        final CoordinatorDetails details = new CoordinatorDetails(map, arjunaContext) ;
        details.setReadOnly(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void replay(Notification replay,
            MAP map, ArjunaContext arjunaContext)
    {
        final String messageId = map.getMessageID() ;
        final CoordinatorDetails details = new CoordinatorDetails(map, arjunaContext) ;
        details.setReplay(true) ;

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
        final CoordinatorDetails details = new CoordinatorDetails(map, arjunaContext) ;
        details.setSoapFault(soapFault) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public static class CoordinatorDetails
    {
        private final MAP map ;
        private final ArjunaContext arjunaContext ;
        private boolean aborted ;
        private boolean committed ;
        private boolean prepared ;
        private boolean readOnly ;
        private boolean replay ;
        private SoapFault soapFault ;

        CoordinatorDetails(final MAP map, final ArjunaContext arjunaContext)
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