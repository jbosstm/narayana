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
package com.arjuna.webservices.wsarjtx.policy;

import com.arjuna.webservices.HandlerRegistry;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices.wsarjtx.handlers.TerminationParticipantCancelledHandler;
import com.arjuna.webservices.wsarjtx.handlers.TerminationParticipantClosedHandler;
import com.arjuna.webservices.wsarjtx.handlers.TerminationParticipantCompletedHandler;
import com.arjuna.webservices.wsarjtx.handlers.TerminationParticipantFaultedHandler;
import com.arjuna.webservices.wsarjtx.handlers.TerminationParticipantSoapFaultHandler;

/**
 * Policy responsible for binding in the terminator coordinator body handlers.
 * @author kevin
 */
public class TerminationParticipantPolicy
{
    /**
     * Add this policy to the registry.
     * @param registry The registry containing the policy.
     */
    public static void register(final HandlerRegistry registry)
    {
        registry.registerBodyHandler(ArjunaTXConstants.WSARJTX_ELEMENT_CLOSED_QNAME, new TerminationParticipantClosedHandler()) ;
        registry.registerBodyHandler(ArjunaTXConstants.WSARJTX_ELEMENT_CANCELLED_QNAME, new TerminationParticipantCancelledHandler()) ;
        registry.registerBodyHandler(ArjunaTXConstants.WSARJTX_ELEMENT_COMPLETED_QNAME, new TerminationParticipantCompletedHandler()) ;
        registry.registerBodyHandler(ArjunaTXConstants.WSARJTX_ELEMENT_FAULTED_QNAME, new TerminationParticipantFaultedHandler()) ;
        registry.registerFaultHandler(new TerminationParticipantSoapFaultHandler()) ;
    }

    /**
     * Remove this policy from the registry.
     * @param registry The registry containing the policy.
     */
    public static void remove(final HandlerRegistry registry)
    {
        registry.registerFaultHandler(null) ;
        registry.removeBodyHandler(ArjunaTXConstants.WSARJTX_ELEMENT_FAULTED_QNAME) ;
        registry.removeBodyHandler(ArjunaTXConstants.WSARJTX_ELEMENT_COMPLETED_QNAME) ;
        registry.removeBodyHandler(ArjunaTXConstants.WSARJTX_ELEMENT_CANCELLED_QNAME) ;
        registry.removeBodyHandler(ArjunaTXConstants.WSARJTX_ELEMENT_CLOSED_QNAME) ;
    }
}
