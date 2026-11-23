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
package com.jboss.transaction.txinterop.interop.states;

import com.arjuna.webservices11.wsba.BusinessActivityConstants;


/**
 * A conversation state for waiting on participant completed.
 */
public class BAInteropDroppedParticipantCompletedState extends BaseState
{
    /**
     * The last action.
     */
    private final String lastAction ;
    /**
     * The participant completed flag.
     */
    private boolean participantCompleted ;
    
    /**
     * Construct the participant completed test.
     * @param lastAction The last action.
     */
    public BAInteropDroppedParticipantCompletedState(final String lastAction)
    {
	this.lastAction = lastAction ;
    }
    
    /**
     * Handle the next action in the sequence.
     * @param action The SOAP action.
     * @param identifier The identifier associated with the endpoint.
     * @return true if the message should be dropped, false otherwise.
     */
    public synchronized boolean handleAction(final String action, final String identifier)
    {
        if (!participantCompleted && BusinessActivityConstants.WSBA_ACTION_COMPLETED.equals(action))
        {
            participantCompleted = true ;
            notifyAll() ;
            return true ;
        }
        else if (participantCompleted && lastAction.equals(action))
        {
            success() ;
        }
        return false ;
    }
    
    /**
     * Wait for the participant to complete.
     * @param timeout The timeout.
     * @return true if the participant has completed, false otherwise.
     */
    public boolean waitForParticipantCompleted(final long timeout)
    {
	final long endTime = System.currentTimeMillis() + timeout ;
	final boolean result ;
	synchronized(this)
	{
	    while(!participantCompleted)
	    {
		final long currentTimeout = endTime - System.currentTimeMillis() ;
		if (currentTimeout <= 0)
		{
		    break ;
		}
                try
                {
                    wait(currentTimeout) ;
                }
                catch (final InterruptedException ie) {}
	    }
	    
	    result = participantCompleted ;
	}
	
	if (result)
	{
	    // If it is completd then wait to allow processing of message.
	    try
	    {
		Thread.sleep(2000) ;
	    }
	    catch (final InterruptedException ie) {}
	}
	return result ;
    }
}
