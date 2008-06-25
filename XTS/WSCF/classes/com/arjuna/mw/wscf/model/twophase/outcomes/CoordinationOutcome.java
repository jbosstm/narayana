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
/*
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: CoordinationOutcome.java,v 1.4 2005/05/19 12:13:25 nmcl Exp $
 */

package com.arjuna.mw.wscf.model.twophase.outcomes;

import com.arjuna.mw.wscf.model.twophase.common.TwoPhaseResult;

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;
import com.arjuna.mw.wsas.completionstatus.Success;

import com.arjuna.mw.wsas.exceptions.SystemException;

/**
 * The CoordinationOutcome represents the final outcome of the coordination
 * event. The CompletionStatus and the actual two-phase status value are
 * returned.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: CoordinationOutcome.java,v 1.4 2005/05/19 12:13:25 nmcl Exp $
 */

public class CoordinationOutcome implements Outcome
{
    
    public CoordinationOutcome (int twophase)
    {
	this(Success.instance(), twophase);
    }

    public CoordinationOutcome (CompletionStatus s, int twophase)
    {
	_status = s;
	_twophaseOutcome = twophase;
    }

    /**
     * @return the two-phase status result.
     * @see com.arjuna.mw.wscf.model.twophase.common.TwoPhaseResult
     */

    public final int result ()
    {
	return _twophaseOutcome;
    }
    
    /**
     * Give a name for this outcome.
     *
     * @exception SystemException Thrown if an error occurs.
     * @return some implementation specific name for the Outcome. Typically
     * this will be the only thing necessary to determine the transaction's
     * outcome, e.g., "RolledBack".
     */

    public String name () throws SystemException
    {
	return "org.w3c.wscf.twophase.outcomes.CoordinationOutcome";
    }

    /**
     * The state in which the activity completed.
     *
     * @exception SystemException Thrown if an error occurs.
     *
     * @return the final completion status of the transaction. Any additional
     * information (e.g., exception types) may be provided by the data method.
     */
 
    public CompletionStatus completedStatus () throws SystemException
    {
	return _status;
    }
 
    /**
     * Outcome specific information that can be used to determine finer
     * level details about the outcome instance.
     *
     * @exception SystemException Thrown if an error occurs.
     *
     * @return completion specific data for this activity. Examples include
     * the type of failure exception that was thrown by the implementation
     * (e.g., HeuristicMixed).
     */

    public Object data () throws SystemException
    {
	return null;
    }

    public String toString ()
    {
	return "com.arjuna.mw.wscf.twophase.outcomes.CoordinationOutcome: "+TwoPhaseResult.stringForm(_twophaseOutcome);
    }
    
    private CompletionStatus _status;
    private int              _twophaseOutcome;
    
}
