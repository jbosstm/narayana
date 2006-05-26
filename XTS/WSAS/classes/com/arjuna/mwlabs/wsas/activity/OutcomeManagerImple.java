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
/*
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: OutcomeManagerImple.java,v 1.2 2005/05/19 12:13:18 nmcl Exp $
 */

package com.arjuna.mwlabs.wsas.activity;

import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.activity.Outcome;
import com.arjuna.mw.wsas.activity.OutcomeManager;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.NoActivityException;

/**
 * The Outcome represents the final outcome of the activity. Because
 * different HLS implementations will have different requirements
 * on what they can or cannot return, this interface is deliberately
 * generic.
 */

public class OutcomeManagerImple implements OutcomeManager
{

    /**
     * Even if exceptions occur just remember the equivalent exception
     * and keep telling all other HLSes that the activity is completing.
     */

    public Outcome processOutcome (Outcome current, Outcome next) throws SystemException
    {
	if (current == null)
	    return next;
	
	if (next == null)
	    return current;

	if (current instanceof CompositeOutcomeImple)
	{
	    ((CompositeOutcomeImple) current).add(next);

	    return current;
	}
	else
	{
	    try
	    {
		CompositeOutcomeImple toReturn = new CompositeOutcomeImple(UserActivityFactory.userActivity().getCompletionStatus());
	    
		toReturn.add(current);
		toReturn.add(next);

		return toReturn;
	    }
	    catch (NoActivityException ex)
	    {
		throw new SystemException(ex.toString());
	    }
	}
    }
    
}
