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
import com.arjuna.webservices11.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.wst11.CompletionCoordinatorParticipant;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

import javax.xml.ws.addressing.AddressingProperties;

public class TestCompletionCoordinatorProcessor extends CompletionCoordinatorProcessor
{
    private Map messageIdMap = new HashMap() ;

    public CompletionCoordinatorDetails getCompletionCoordinatorDetails(final String messageId, final long timeout)
    {
        final long endTime = System.currentTimeMillis() + timeout ;
        synchronized(messageIdMap)
        {
            long now = System.currentTimeMillis() ;
            while(now < endTime)
            {
                final CompletionCoordinatorDetails details = (CompletionCoordinatorDetails)messageIdMap.remove(messageId) ;
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
            final CompletionCoordinatorDetails details = (CompletionCoordinatorDetails)messageIdMap.remove(messageId) ;
            if (details != null)
            {
                return details ;
            }
        }
        throw new NullPointerException("Timeout occurred waiting for id: " + messageId) ;
    }

    /**
     * Commit.
     * @param commit The commit notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void commit(final Notification commit, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final String messageId = addressingProperties.getMessageID().getURI().toString();
        final CompletionCoordinatorDetails details = new CompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setCommit(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Rollback.
     * @param rollback The rollback notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void rollback(final Notification rollback, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final CompletionCoordinatorDetails details = new CompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setRollback(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Activate the participant.
     * @param participant The participant.
     * @param identifier The identifier.
     */
    public void activateParticipant(final CompletionCoordinatorParticipant participant, final String identifier)
    {
    }

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public void deactivateParticipant(final CompletionCoordinatorParticipant participant)
    {
    }

    public static class CompletionCoordinatorDetails
    {
        private final AddressingProperties addressingProperties ;
        private final ArjunaContext arjunaContext ;
        private boolean commit ;
        private boolean rollback ;

        CompletionCoordinatorDetails(final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
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

        public boolean hasCommit()
        {
            return commit ;
        }

        void setCommit(final boolean commit)
        {
            this.commit = commit ;
        }

        public boolean hasRollback()
        {
            return rollback ;
        }

        void setRollback(final boolean rollback)
        {
            this.rollback = rollback ;
        }
    }
}