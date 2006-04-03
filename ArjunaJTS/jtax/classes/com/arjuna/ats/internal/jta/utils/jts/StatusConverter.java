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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: StatusConverter.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.utils.jts;

import org.omg.CosTransactions.*;

import javax.transaction.*;
import javax.transaction.xa.*;

/**
 * Some useful utility routines.
 */

public class StatusConverter
{

public static int convert (org.omg.CosTransactions.Status status)
    {
	switch (status.value())
	{
	case org.omg.CosTransactions.Status._StatusActive:
	    return javax.transaction.Status.STATUS_ACTIVE;
	case org.omg.CosTransactions.Status._StatusCommitted:
	    return javax.transaction.Status.STATUS_COMMITTED;
	case org.omg.CosTransactions.Status._StatusMarkedRollback:
	    return javax.transaction.Status.STATUS_MARKED_ROLLBACK;
	case org.omg.CosTransactions.Status._StatusNoTransaction:
	    return javax.transaction.Status.STATUS_NO_TRANSACTION;
	case org.omg.CosTransactions.Status._StatusPrepared:
	    return javax.transaction.Status.STATUS_PREPARED;
	case org.omg.CosTransactions.Status._StatusRolledBack:
	    return javax.transaction.Status.STATUS_ROLLEDBACK;
	case org.omg.CosTransactions.Status._StatusUnknown:
	default:
	    return javax.transaction.Status.STATUS_UNKNOWN;
	}
    }

}
