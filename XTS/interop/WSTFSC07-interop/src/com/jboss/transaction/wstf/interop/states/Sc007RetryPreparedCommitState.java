/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package com.jboss.transaction.wstf.interop.states;

import com.arjuna.webservices11.wsat.AtomicTransactionConstants;

/**
 * A conversation state for retry prepared commit state.
 */
public class Sc007RetryPreparedCommitState extends BaseState
{
    /**
     * The prepared count.
     */
    private int preparedCount ;
    /**
     * The committed count.
     */
    private int committedCount ;
    /**
     * The first identifier.
     */
    private String firstIdentifier ;
    /**
     * Are we dropping other messages?
     */
    private boolean drop ;
    
    /**
     * Handle the next action in the sequence.
     * @param action The SOAP action.
     * @param identifier The identifier associated with the endpoint.
     * @return true if the message should be dropped, false otherwise.
     */
    public boolean handleAction(final String action, final String identifier)
    {
        if (AtomicTransactionConstants.WSAT_ACTION_PREPARED.equals(action))
        {
            preparedCount++ ;
            if (preparedCount == 1)
            {
                firstIdentifier = identifier ;
            }
            else if (preparedCount == 2)
            {
                drop = true ;
            }
            else if (firstIdentifier.equals(identifier))
            {
                drop = false ;
            }
        }
        else if (AtomicTransactionConstants.WSAT_ACTION_COMMITTED.equals(action))
        {
            if (++committedCount == 2)
            {
                success() ;
            }
        }
        return drop ;
    }
}
