/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.twophase.participants;

import com.arjuna.mw.wsas.exceptions.SystemException;

/**
 * This is the interface that all synchronization participants must define.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Synchronization.java,v 1.2 2005/05/19 12:13:26 nmcl Exp $
 * @since 1.0.
 */

public interface Synchronization
{

    /**
     * The transaction that the instance is enrolled with is about to
     * commit.
     * 
     * @exception SystemException Thrown if any error occurs. This will cause
     * the transaction to roll back.
     */

    public void beforeCompletion () throws SystemException;

    /**
     * The transaction that the instance is enrolled with has completed and
     * the state in which is completed is passed as a parameter.
     *
     * @param status The state in which the transaction completed.
     *
     * @exception SystemException Thrown if any error occurs. This has no
     * affect on the outcome of the transaction.
     */

    public void afterCompletion (int status) throws SystemException;
    
}