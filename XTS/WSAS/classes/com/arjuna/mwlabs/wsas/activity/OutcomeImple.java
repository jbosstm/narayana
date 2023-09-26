/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mwlabs.wsas.activity;

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.HLSException;

/**
 * The Outcome represents the final outcome of the activity. Because
 * different HLS implementations will have different requirements
 * on what they can or cannot return, this interface is deliberately
 * generic.
 */

public class OutcomeImple implements Outcome
{

    public OutcomeImple (CompletionStatus s)
    {
	_status = s;
	_exception = null;
    }

    public OutcomeImple (HLSException ex, CompletionStatus cs)
    {
	_status = cs;
	_exception = ex;
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
	return "OutcomeImple";
    }

    /**
     * The state in which the activity completed.
     *
     * @exception SystemException Thrown if an error occurs.
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
     * @return completion specific data for this activity. Examples include
     * the type of failure exception that was thrown by the implementation
     * (e.g., HeuristicMixed).
     */

    //    public org.w3c.dom.Document data () throws SystemException
    public Object data () throws SystemException
    {
	return null;
    }

    public final HLSException exception ()
    {
	return _exception;
    }

    private CompletionStatus _status;
    private HLSException     _exception;
    
}