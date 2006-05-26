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
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionCoordinatorCancelledHandler;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionCoordinatorClosedHandler;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionCoordinatorCompensatedHandler;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionCoordinatorCompletedHandler;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionCoordinatorExitHandler;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionCoordinatorFaultHandler;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionCoordinatorGetStatusHandler;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionCoordinatorSoapFaultHandler;
import com.arjuna.webservices.wsba.handlers.CoordinatorCompletionCoordinatorStatusHandler;

/**
 * Policy responsible for binding in the WS-BusinessActivity header handlers.
 * @author kevin
 */
public class CoordinatorCompletionCoordinatorPolicy
{
    /**
     * Add this policy to the registry.
     * @param registry The registry containing the policy.
     */
    public static void register(final HandlerRegistry registry)
    {
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_COMPLETED_QNAME, new CoordinatorCompletionCoordinatorCompletedHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_FAULT_QNAME, new CoordinatorCompletionCoordinatorFaultHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_COMPENSATED_QNAME, new CoordinatorCompletionCoordinatorCompensatedHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_CLOSED_QNAME, new CoordinatorCompletionCoordinatorClosedHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_CANCELLED_QNAME, new CoordinatorCompletionCoordinatorCancelledHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_EXIT_QNAME, new CoordinatorCompletionCoordinatorExitHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_GET_STATUS_QNAME, new CoordinatorCompletionCoordinatorGetStatusHandler()) ;
        registry.registerBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_STATUS_QNAME, new CoordinatorCompletionCoordinatorStatusHandler()) ;
        registry.registerFaultHandler(new CoordinatorCompletionCoordinatorSoapFaultHandler()) ;
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
        registry.removeBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_EXIT_QNAME) ;
        registry.removeBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_CANCELLED_QNAME) ;
        registry.removeBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_CLOSED_QNAME) ;
        registry.removeBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_COMPENSATED_QNAME) ;
        registry.removeBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_FAULT_QNAME) ;
        registry.removeBodyHandler(BusinessActivityConstants.WSBA_ELEMENT_COMPLETED_QNAME) ;
    }
}
