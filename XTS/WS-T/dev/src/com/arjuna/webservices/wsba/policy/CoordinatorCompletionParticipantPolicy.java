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
package com.arjuna.webservices.wsba.policy;

import com.arjuna.webservices.HandlerRegistry;
import com.arjuna.webservices.wsba.BusinessActivityConstants;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionParticipantCancelHandler;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionParticipantCloseHandler;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionParticipantCompensateHandler;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionParticipantCompleteHandler;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionParticipantExitedHandler;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionParticipantFaultedHandler;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionParticipantGetStatusHandler;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionParticipantSoapFaultHandler;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionParticipantStatusHandler;

/**
 * Policy responsible for binding in the WS-BusinessActivity header handlers.
 * @author kevin
 */
public class CoordinatorCompletionParticipantPolicy
{
    /**
     * Add this policy to the registry.
     * @param registry The registry containing the policy.
     */
    public static void register(final HandlerRegistry registry)
    {
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_COMPLETE_QNAME, new CoordinatorCompletionParticipantCompleteHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_CLOSE_QNAME, new CoordinatorCompletionParticipantCloseHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_CANCEL_QNAME, new CoordinatorCompletionParticipantCancelHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_COMPENSATE_QNAME, new CoordinatorCompletionParticipantCompensateHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_FAULTED_QNAME, new CoordinatorCompletionParticipantFaultedHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_EXITED_QNAME, new CoordinatorCompletionParticipantExitedHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_GET_STATUS_QNAME, new CoordinatorCompletionParticipantGetStatusHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_STATUS_QNAME, new CoordinatorCompletionParticipantStatusHandler()) ;
        registry.registerFaultHandler(new CoordinatorCompletionParticipantSoapFaultHandler()) ;
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
        registry.removeBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_COMPLETE_QNAME) ;
    }
}
