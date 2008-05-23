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

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsat.ParticipantInboundEvents;
import com.arjuna.webservices11.wsat.processors.ParticipantProcessor;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

import javax.xml.ws.addressing.AddressingProperties;

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

    public void commit(Notification commit,
            AddressingProperties addressingProperties, ArjunaContext arjunaContext)
    {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantDetails details = new ParticipantDetails(addressingProperties, arjunaContext) ;
        details.setCommit(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void prepare(Notification prepare,
            AddressingProperties addressingProperties, ArjunaContext arjunaContext)
    {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantDetails details = new ParticipantDetails(addressingProperties, arjunaContext) ;
        details.setPrepare(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void rollback(Notification rollback,
            AddressingProperties addressingProperties, ArjunaContext arjunaContext)
    {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantDetails details = new ParticipantDetails(addressingProperties, arjunaContext) ;
        details.setRollback(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void soapFault(SoapFault soapFault, AddressingProperties addressingProperties,
            ArjunaContext arjunaContext)
    {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantDetails details = new ParticipantDetails(addressingProperties, arjunaContext) ;
        details.setSoapFault(soapFault) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public static class ParticipantDetails
    {
        private final AddressingProperties addressingProperties ;
        private final ArjunaContext arjunaContext ;
        private boolean commit ;
        private boolean prepare ;
        private boolean rollback ;
        private SoapFault soapFault ;

        ParticipantDetails(final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
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