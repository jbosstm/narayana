/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.common;

import com.arjuna.ats.jta.utils.JTAHelper;

public class Synchronization implements jakarta.transaction.Synchronization
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
	    jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

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
	    jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

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