/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.hp.mwtests.ts.jts.utils.Util;

public class ThreadObject3a extends Thread
{

public ThreadObject3a (boolean doCommit)
    {
	if (doCommit)
	    _threadId = commitThreadId++;
	else
	    _threadId = abortThreadId++;
	
	_commit = doCommit;
    }

public void run ()
    {
	CurrentImple current = OTSImpleManager.current();

	try
	{
	    current.begin();

	    Util.indent(_threadId, 0);
	    System.out.println("begin");

	    AtomicWorker3.randomOperation(_threadId, 0);
	    AtomicWorker3.randomOperation(_threadId, 0);

	    if (_commit)
		current.commit(false);
	    else
		current.rollback();

	    Util.indent(_threadId, 0);

	    if (_commit)
		System.out.println("end");
	    else
		System.out.println("abort");
	}
	catch (Exception e)
	{
	    System.err.println(e);
	}
    }

private int _threadId;
private boolean _commit;

private static int commitThreadId = 3;
private static int abortThreadId = 3;
    
};