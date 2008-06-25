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
package com.arjuna.webservices.wsat.policy;

import com.arjuna.webservices.HandlerRegistry;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsat.handlers.CoordinatorAbortedHandler;
import com.arjuna.webservices.wsat.handlers.CoordinatorCommittedHandler;
import com.arjuna.webservices.wsat.handlers.CoordinatorSoapFaultHandler;
import com.arjuna.webservices.wsat.handlers.CoordinatorPreparedHandler;
import com.arjuna.webservices.wsat.handlers.CoordinatorReadOnlyHandler;
import com.arjuna.webservices.wsat.handlers.CoordinatorReplayHandler;

/**
 * Policy responsible for binding in the WS-AtomicTransaction coordinator handlers.
 * @author kevin
 */
public class CoordinatorPolicy
{
    /**
     * Add this policy to the registry.
     * @param registry The registry containing the policy.
     */
    public static void register(final HandlerRegistry registry)
    {
        registry.registerBodyHandler(AtomicTransactionConstants.WSAT_ELEMENT_PREPARED_QNAME, new CoordinatorPreparedHandler()) ;
        registry.registerBodyHandler(AtomicTransactionConstants.WSAT_ELEMENT_ABORTED_QNAME, new CoordinatorAbortedHandler()) ;
        registry.registerBodyHandler(AtomicTransactionConstants.WSAT_ELEMENT_READ_ONLY_QNAME, new CoordinatorReadOnlyHandler()) ;
        registry.registerBodyHandler(AtomicTransactionConstants.WSAT_ELEMENT_COMMITTED_QNAME, new CoordinatorCommittedHandler()) ;
        registry.registerBodyHandler(AtomicTransactionConstants.WSAT_ELEMENT_REPLAY_QNAME, new CoordinatorReplayHandler()) ;
        registry.registerFaultHandler(new CoordinatorSoapFaultHandler()) ;
    }

    /**
     * Remove this policy from the registry.
     * @param registry The registry containing the policy.
     */
    public static void remove(final HandlerRegistry registry)
    {
        registry.registerFaultHandler(null) ;
        registry.removeBodyHandler(AtomicTransactionConstants.WSAT_ELEMENT_REPLAY_QNAME) ;
        registry.removeBodyHandler(AtomicTransactionConstants.WSAT_ELEMENT_COMMITTED_QNAME) ;
        registry.removeBodyHandler(AtomicTransactionConstants.WSAT_ELEMENT_READ_ONLY_QNAME) ;
        registry.removeBodyHandler(AtomicTransactionConstants.WSAT_ELEMENT_ABORTED_QNAME) ;
        registry.removeBodyHandler(AtomicTransactionConstants.WSAT_ELEMENT_PREPARED_QNAME) ;
    }
}
