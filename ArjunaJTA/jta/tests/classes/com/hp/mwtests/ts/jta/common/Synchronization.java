/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Synchronization.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.common;

import com.arjuna.ats.jta.utils.JTAHelper;

import javax.transaction.xa.*;

public class Synchronization implements javax.transaction.Synchronization
{
	public final static int ERROR_STATUS = 0;
	public final static int INITIAL_STATUS = 1;
	public final static int BEFORE_COMPLETION_STATUS = 2;
	public final static int AFTER_COMPLETION_STATUS = 3;

	private int _currentStatus = INITIAL_STATUS;

	public int getCurrentStatus()
	{
		return _currentStatus;
	}

    public void beforeCompletion ()
    {
	try
	{
	    javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

	    if (_currentStatus != INITIAL_STATUS)
		_currentStatus = ERROR_STATUS;
	    else
		_currentStatus = BEFORE_COMPLETION_STATUS;
	    System.out.println("beforeCompletion called from "+tm.getTransaction());
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	    _currentStatus = ERROR_STATUS;
	}
    }

    public void afterCompletion (int status)
    {
	try
	{
	    javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

	    if (_currentStatus != BEFORE_COMPLETION_STATUS)
		_currentStatus = ERROR_STATUS;
	    else
		_currentStatus = AFTER_COMPLETION_STATUS;

	    System.out.println("afterCompletion called: "+JTAHelper.stringForm(status)+" from "+tm.getTransaction());
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	    _currentStatus = ERROR_STATUS;
	}
    }

}
