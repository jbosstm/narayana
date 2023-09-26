/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jts.extensions;

import org.omg.CORBA.SystemException;

import com.arjuna.ats.internal.jts.ControlWrapper;

/**
 * Whereas TxAssociation classes are on a per
 * thread basis, insances of this type apply to all threads.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: DebugTxAssociation.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class DebugTxAssociation implements TxAssociation
{
    
    public void begin (ControlWrapper tx) throws SystemException
    {
	String name = ((tx == null) ? "none" : tx.get_transaction_name());
	
	System.err.println("Transaction "+name+" begun");
    }

    public void commit (ControlWrapper tx) throws SystemException
    {
	String name = ((tx == null) ? "none" : tx.get_transaction_name());
	
	System.err.println("Transaction "+name+" commit");
    }

    public void rollback (ControlWrapper tx) throws SystemException
    {
	String name = ((tx == null) ? "none" : tx.get_transaction_name());

	System.err.println("Transaction "+name+" rollback");
    }

    public void suspend (ControlWrapper tx) throws SystemException
    {
	String name = ((tx == null) ? "none" : tx.get_transaction_name());

	System.err.println("Transaction "+name+" suspend");
    }

    public void resume (ControlWrapper tx) throws SystemException
    {
	String name = ((tx == null) ? "none" : tx.get_transaction_name());

	System.err.println("Transaction "+name+" resume");
    }

    public String name ()
    {
	return "Debug";
    }

}