/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.recovery.recoverycoordinators;

import org.omg.CosTransactions.Resource;

import com.arjuna.ats.jts.logging.jtsLogger;

/**
 * Threaded object used to terminate a prepared Resource after a
 * failure.  Normally failed resources are terminated by replaying the
 * transaction in which the Resource was participating. However, if
 * the transaction rolled back then we won't have any persistent
 * record of the transaction so it can't be reactivated. Resources (and
 * subordinate coordinators) may use the replay_completion response to
 * trigger their own rollback.
 * 
 */

public class ResourceCompletor extends Thread
{
    public static final int ROLLBACK = 0;
    public static final int COMMIT = 1;

    public ResourceCompletor( Resource res, int action )
    {
	_res = res;
	_action = action;
    }
    
    public final void run ()
    {
	if (_action == ROLLBACK)
	    rollback();
    }

    private final void rollback ()
    {
	try
	{
	    if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("ResourceCompletor.rollback()");
        }
	    
	    _res.rollback();
	}
	catch (Exception e)
	{
	    if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("ResourceCompletor.rollback() - rollback failed: "+e);
        }
	}
    }

    private Resource _res = null;
    private int      _action = 0;
}