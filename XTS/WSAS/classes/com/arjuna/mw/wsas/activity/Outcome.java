/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.activity;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;

import com.arjuna.mw.wsas.exceptions.SystemException;

/**
 * The Outcome represents the final outcome of the activity. Because
 * different HLS implementations will have different requirements
 * on what they can or cannot return, this interface is deliberately
 * generic.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Outcome.java,v 1.2 2005/05/19 12:13:16 nmcl Exp $
 * @since 1.0.
 */

public interface Outcome
{

    /**
     * Give a name for this outcome.
     *
     * @exception SystemException Thrown if an error occurs.
     * @return some implementation specific name for the Outcome. Typically
     * this will be the only thing necessary to determine the transaction's
     * outcome, e.g., "RolledBack".
     */

    public String name () throws SystemException;

    /**
     * The state in which the activity completed.
     *
     * @exception SystemException Thrown if an error occurs.
     * @return the final completion status of the transaction. Any additional
     * information (e.g., exception types) may be provided by the data method.
     */

    public CompletionStatus completedStatus () throws SystemException;
 
    /**
     * Outcome specific information that can be used to determine finer
     * level details about the outcome instance.
     *
     * @exception SystemException Thrown if an error occurs.
     * @return completion specific data for this activity. Examples include
     * the type of failure exception that was thrown by the implementation
     * (e.g., HeuristicMixed).
     */

    public Object data () throws SystemException;
    
}