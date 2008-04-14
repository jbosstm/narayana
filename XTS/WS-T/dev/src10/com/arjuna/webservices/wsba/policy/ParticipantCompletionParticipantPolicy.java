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
package com.arjuna.webservices.wsba.policy;

import com.arjuna.webservices.HandlerRegistry;
import com.arjuna.webservices.wsba.BusinessActivityConstants;
import com.arjuna.webservices.wsba.handlers.ParticipantCompletionParticipantCancelHandler;
import com.arjuna.webservices.wsba.handlers.ParticipantCompletionParticipantCloseHandler;
import com.arjuna.webservices.wsba.handlers.ParticipantCompletionParticipantCompensateHandler;
import com.arjuna.webservices.wsba.handlers.ParticipantCompletionParticipantExitedHandler;
import com.arjuna.webservices.wsba.handlers.ParticipantCompletionParticipantFaultedHandler;
import com.arjuna.webservices.wsba.handlers.ParticipantCompletionParticipantGetStatusHandler;
import com.arjuna.webservices.wsba.handlers.ParticipantCompletionParticipantSoapFaultHandler;
import com.arjuna.webservices.wsba.handlers.ParticipantCompletionParticipantStatusHandler;

/**
 * Policy responsible for binding in the WS-BusinessActivity header handlers.
 * @author kevin
 */
public class ParticipantCompletionParticipantPolicy
{
    /**
     * Add this policy to the registry.
     * @param registry The registry containing the policy.
     */
    public static void register(final HandlerRegistry registry)
    {
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_CLOSE_QNAME, new ParticipantCompletionParticipantCloseHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_CANCEL_QNAME, new ParticipantCompletionParticipantCancelHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_COMPENSATE_QNAME, new ParticipantCompletionParticipantCompensateHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_FAULTED_QNAME, new ParticipantCompletionParticipantFaultedHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_EXITED_QNAME, new ParticipantCompletionParticipantExitedHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_GET_STATUS_QNAME, new ParticipantCompletionParticipantGetStatusHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_STATUS_QNAME, new ParticipantCompletionParticipantStatusHandler()) ;
        registry.registerFaultHandler(new ParticipantCompletionParticipantSoapFaultHandler()) ;
    }

    /**
     * Remove this policy from the registry.
     * @param registry The registry containing the policy.
     */
    public static void remove(final HandlerRegistry registry)
    {
        registry.registerFaultHandler(null) ;
        registry.removeBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_STATUS_QNAME) ;
        registry.removeBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_GET_STATUS_QNAME) ;
        registry.removeBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_EXITED_QNAME) ;
        registry.removeBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_FAULTED_QNAME) ;
        registry.removeBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_COMPENSATE_QNAME) ;
        registry.removeBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_CANCEL_QNAME) ;
        registry.removeBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_CLOSE_QNAME) ;
    }
}
