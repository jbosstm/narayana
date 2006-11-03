/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
 * Copyright (C) 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: XARecoveryResourceImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.recovery.jts;

import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.recovery.XARecoveryResource;

import com.arjuna.ats.arjuna.common.*;

import com.arjuna.ats.internal.jta.resources.jts.orbspecific.XAResourceRecord;

import com.arjuna.common.util.logging.*;

import java.util.*;
import javax.transaction.xa.*;

/*
 * Default visibility.
 */

class XARecoveryResourceImple extends XAResourceRecord implements XARecoveryResource
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
	 * If we haven't got an XID then we tried to load the
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
	    if (jtaLogger.logger.isDebugEnabled())
	    {
		jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				       com.arjuna.ats.arjuna.logging.FacilityCode.FAC_CRASH_RECOVERY, "XARecoveryResourceImple.notAProblem - no error with XAER_NOTA on "
				       + (commit ? " commit" : " rollback"));
	    }
	    
	    return true;
	}
	return false;
    }

}
