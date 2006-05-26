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
 * $Id: XAOutcome.java,v 1.2 2005/05/19 12:13:22 nmcl Exp $
 */

package com.arjuna.mw.wscf.model.as.coordinator.xa.outcomes;

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;
import com.arjuna.mw.wsas.completionstatus.Failure;
import com.arjuna.mw.wsas.completionstatus.Success;

import com.arjuna.mw.wsas.exceptions.SystemException;

import javax.transaction.xa.XAException;

/**
 * The XAOutcome represents the final outcome of the coordination
 * event. The CompletionStatus and the actual two-phase status value are
 * returned.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: XAOutcome.java,v 1.2 2005/05/19 12:13:22 nmcl Exp $
 */

public class XAOutcome implements Outcome
{

    public XAOutcome ()
    {
	this(Success.instance(), null);
    }
    
    public XAOutcome (XAException ex)
    {
	this(Failure.instance(), ex);
    }

    public XAOutcome (CompletionStatus s)
    {
	this(s, null);
    }

    public XAOutcome (CompletionStatus s, XAException ex)
    {
	_status = s;
	_exp = ex;
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
	return "org.w3c.wscf.xa.outcomes.XAOutcome";
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
	return _exp;
    }

    public String toString ()
    {
	try
	{
	    return name()+":"+_exp;
	}
	catch (SystemException ex)
	{
	    return null;
	}
    }
    
    private CompletionStatus _status;
    private XAException      _exp;
    
}
