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
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: AsyncCommit.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.internal.arjuna.thread.*;
import java.lang.Thread;

/**
 * This class is responsible for performing asynchronous termination of
 * a transaction. Despite its name, it is also able to perform
 * asynchronous rollback as well as commit. The transaction will have
 * been prepared by the time an instance of this class is used.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: AsyncCommit.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

/*
 * Default visibility.
 */

class AsyncCommit extends Thread
{

    /**
     * Create a new instance, and give it the transaction to
     * control. The commit parameter determines whether the thread
     * should commit or rollback the transaction.
     */

public static AsyncCommit create (BasicAction toControl, boolean commit)
    {
	AsyncCommit c = new AsyncCommit(toControl, commit);

	c.start();

	Thread.yield();

	return c;
    }

    /**
     * Overloads Thread.run
     */
    
public void run ()
    {
	if (_theAction != null)
	{
	    /*
	     * This is a transient thread so we don't
	     * want to register it with the action it is
	     * committing/aborting, only change its notion of the
	     * current transaction so that any abstract
	     * records that need that information can still
	     * have it.
	     */
	    
	    ThreadActionData.pushAction(_theAction, false);
	    
	    if (_commit)
		doPhase2Commit();
	    else
		doPhase2Abort();

	    ThreadActionData.popAction(false);
	}
    }

    /**
     * The actual constructor for a new instance.
     */

protected AsyncCommit (BasicAction toControl, boolean commit)
    {
	_theAction = toControl;
	_commit = commit;
    }

    /**
     * Perform phase 2 commit on the transaction.
     */

protected synchronized boolean doPhase2Commit ()
    {
	if (_theAction != null)
	{
	    /*
	     * Don't want heuristic information, otherwise would
	     * not be asynchronous.
	     */

	    _theAction.phase2Commit(false);

	    return true;
	}
	else
	    return false;
    }

    /**
     * Do phase 2 abort (rollback) on the transaction.
     */

protected boolean doPhase2Abort ()
    {
	if (_theAction != null)
	{
	    _theAction.phase2Abort(false);

	    return true;
	}
	else
	    return false;
    }

private BasicAction _theAction;
private boolean     _commit;

};
