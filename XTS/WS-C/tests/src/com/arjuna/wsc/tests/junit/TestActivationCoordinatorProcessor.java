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
package com.arjuna.wsc.tests.junit;

import java.util.HashMap;
import java.util.Map;

import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wscoor.CreateCoordinationContextType;
import com.arjuna.webservices.wscoor.processors.ActivationCoordinatorProcessor;

public class TestActivationCoordinatorProcessor extends
        ActivationCoordinatorProcessor
{
    private Map messageIdMap = new HashMap() ;
    
    public void createCoordinationContext(final CreateCoordinationContextType createCoordinationContext,
        final AddressingContext addressingContext)
    {
        final String messageId = addressingContext.getMessageID().getValue() ;
        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, new CreateCoordinationContextDetails(createCoordinationContext, addressingContext)) ;
            messageIdMap.notifyAll() ;
        }
    }
    
    public CreateCoordinationContextDetails getCreateCoordinationContextDetails(final String messageId, long timeout)
    {
        final long endTime = System.currentTimeMillis() + timeout ;
        synchronized(messageIdMap)
        {
            long now = System.currentTimeMillis() ;
            while(now < endTime)
            {
                final CreateCoordinationContextDetails details = (CreateCoordinationContextDetails)messageIdMap.remove(messageId) ;
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
            final CreateCoordinationContextDetails details = (CreateCoordinationContextDetails)messageIdMap.remove(messageId) ;
            if (details != null)
            {
                return details ;
            }
        }
        throw new NullPointerException("Timeout occurred waiting for id: " + messageId) ;
    }
    
    public static class CreateCoordinationContextDetails
    {
        private final CreateCoordinationContextType createCoordinationContext ;
        private final AddressingContext addressingContext ;
        
        CreateCoordinationContextDetails(final CreateCoordinationContextType createCoordinationContext,
            final AddressingContext addressingContext)
        {
            this.createCoordinationContext = createCoordinationContext ;
            this.addressingContext = addressingContext ;
        }
        
        public CreateCoordinationContextType getCreateCoordinationContext()
        {
            return createCoordinationContext ;
        }
        
        public AddressingContext getAddressingContext()
        {
            return addressingContext ;
        }
    }
}
