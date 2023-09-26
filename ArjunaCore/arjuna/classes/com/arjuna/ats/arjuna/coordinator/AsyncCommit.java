/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

import java.util.concurrent.Callable;

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

class AsyncCommit implements Runnable
{
 public void run() {
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
 }


    /**
     * Create a new instance, and give it the transaction to
     * control. The commit parameter determines whether the thread
     * should commit or rollback the transaction.
     */
 AsyncCommit (BasicAction toControl, boolean commit)
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

}