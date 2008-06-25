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
 * Copyright (C) 2000, 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DebugTxAssociation.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.extensions;

import com.arjuna.ats.internal.jts.ControlWrapper;

import org.omg.CORBA.SystemException;

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
