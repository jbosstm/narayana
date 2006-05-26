/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
package com.arjuna.wsc.tests.junit;

import java.util.HashMap;
import java.util.Map;

import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wscoor.RegisterType;
import com.arjuna.webservices.wscoor.processors.RegistrationCoordinatorProcessor;

public class TestRegistrationCoordinatorProcessor extends
        RegistrationCoordinatorProcessor
{
    private Map messageIdMap = new HashMap() ;
    
    public void register(final RegisterType register, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
    {
        final String messageId = addressingContext.getMessageID().getValue() ;
        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, new RegisterDetails(register, addressingContext, arjunaContext)) ;
            messageIdMap.notifyAll() ;
        }
    }
    
    public RegisterDetails getRegisterDetails(final String messageId, final long timeout)
    {
        final long endTime = System.currentTimeMillis() + timeout ;
        synchronized(messageIdMap)
        {
            long now = System.currentTimeMillis() ;
            while(now < endTime)
            {
                final RegisterDetails details = (RegisterDetails)messageIdMap.remove(messageId) ;
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
            final RegisterDetails details = (RegisterDetails)messageIdMap.remove(messageId) ;
            if (details != null)
            {
                return details ;
            }
        }
        throw new NullPointerException("Timeout occurred waiting for id: " + messageId) ;
    }
    
    public static class RegisterDetails
    {
        private final RegisterType register ;
        private final AddressingContext addressingContext ;
        private final ArjunaContext arjunaContext ;
        
        RegisterDetails(final RegisterType register,
            final AddressingContext addressingContext,
            final ArjunaContext arjunaContext)
        {
            this.register = register ;
            this.addressingContext = addressingContext ;
            this.arjunaContext = arjunaContext ;
        }
        
        public RegisterType getRegister()
        {
            return register ;
        }
        
        public AddressingContext getAddressingContext()
        {
            return addressingContext ;
        }
        
        public ArjunaContext getArjunaContext()
        {
            return arjunaContext ;
        }
    }
}
