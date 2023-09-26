/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jts.extensions;

import org.omg.CORBA.SystemException;

import com.arjuna.ats.internal.jts.ControlWrapper;

/**
 * Instances of these classes are informed whenever a
 * transaction is begun/suspended/resumed/ended.
 * The instance can control whether it wants to be informed
 * about all transactions, or just subtransactions.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: TxAssociation.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public interface TxAssociation
{
 
    /*
     * Since one instance could mark the transaction as rollback
     * we could inform subsequent instances. We don't at present.
     */

    public void begin (ControlWrapper tx) throws SystemException;
    public void commit (ControlWrapper tx) throws SystemException;
    public void rollback (ControlWrapper tx) throws SystemException;
    public void suspend (ControlWrapper tx) throws SystemException;
    public void resume (ControlWrapper tx) throws SystemException;

    public String name ();
 
    /*
     * We could impose some ordering constraints
     * on instances, such that users have the capability
     * of specifying how they are invoked, e.g., A before
     * B. It would affect performance of adding, but if
     * that does not happen frequently it probably doesn't
     * matter.
     */
 
}