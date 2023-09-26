/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.recovery.jts;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.resources.jts.orbspecific.XAResourceRecord;
import com.arjuna.ats.internal.jta.utils.jtaxLogger;
import com.arjuna.ats.jta.recovery.XARecoveryResource;


public class XARecoveryResourceImple extends XAResourceRecord implements XARecoveryResource
{

    public XARecoveryResourceImple (Uid u)
    {
	super(u);
    }

    public XARecoveryResourceImple (Uid u, XAResource res)
    {
	super(u);

	super._theXAResource = res;
    }

    public final XAResource getXAResource ()
    {
	return super._theXAResource;
    }
    
    public int recoverable ()
    {
	/*
	 * If we haven't got an XID then we probably tried to load the
	 * state of an inflight transaction and that state had
	 * just been deleted. So, no recovery to perform.
	 */

	if (getXid() == null)
	    return XARecoveryResource.INFLIGHT_TRANSACTION;
	
	/*
	 * If we don't have an XAResource then we cannot recover at
	 * this stage. The XAResource will have to be provided for
	 * us and then we can retry.
	 */

	if (super._theXAResource == null)
	    return XARecoveryResource.INCOMPLETE_STATE;
	else
	    return XARecoveryResource.RECOVERY_REQUIRED;
    }
    
    public int recover ()
    {
	return super.recover();
    }

    /**
     * Is the XAException a non-error when received in reply to commit or
     * rollback ?
     *
     * In recovery, the commit/rollback may have been sent before (possibly
     * only just before, in another thread) - in which case the RM will not
     * recognise the XID but it doesn't matter
     */

    protected boolean notAProblem (XAException ex, boolean commit)
    {
	if (ex.errorCode == XAException.XAER_NOTA)
	{ 
	    if (jtaxLogger.logger.isDebugEnabled()) {
            jtaxLogger.logger.debug("XARecoveryResourceImple.notAProblem - no error with XAER_NOTA on "
                    + (commit ? " commit" : " rollback"));
        }
	    
	    return true;
	}
	return false;
    }

}