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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: XAResourceImple.java,v 1.3 2005/05/19 12:13:33 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.as.coordinator.jta;

import com.arjuna.mw.wscf.logging.wscfLogger;

import javax.transaction.xa.*;

import com.arjuna.mw.wscf.model.as.coordinator.xa.messages.*;
import com.arjuna.mw.wscf.model.as.coordinator.xa.outcomes.*;

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wscf.common.Qualifier;

import com.arjuna.mw.wscf.model.as.coordinator.Participant;

import com.arjuna.mw.wsas.exceptions.SystemException;

import java.io.Serializable;

/**
 * JTA resource that wraps the activity service coordinator Participant.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: XAResourceImple.java,v 1.3 2005/05/19 12:13:33 nmcl Exp $
 */

public class XAResourceImple implements XAResource, Serializable
{
    
    /**
     * Constructor.
     *
     * @param theResource is the proxy that allows us to call out to the
     * object.
     *
     * @message com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_1 [com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_1] - XAResourceImple {0} - null participant provided!
     */

    public XAResourceImple (Participant theResource, Qualifier[] quals)
    {
	_resourceHandle = theResource;
	_quals = quals;
	
	if (_resourceHandle == null)
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_1",
					  new Object[]{_resourceHandle});
    }


    public int getTransactionTimeout() throws XAException
    {
	// TODO

	//        return _timeout;

	return 0;
    }

    public boolean isSameRM (XAResource xares) throws XAException
    {
        return (xares.equals(this));
    }

    public boolean setTransactionTimeout(int seconds) throws XAException
    {
        return true;
    }

    /**
     * @message com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_2 [com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_2] - XAResourceImple.start {0} caught: {1}
     */

    public void start (Xid xid, int flags) throws XAException
    {
        try
        {
            if (_resourceHandle != null)
            {
		Outcome res = _resourceHandle.processMessage(new XAStart(xid, flags));
		
		if (res != null)
		{
		    if (res instanceof XAOutcome)
		    {
			generateException((XAOutcome) res);
		    }
		    else
			throw new XAException(XAException.XAER_INVAL);
		}
            }
            else
		throw new XAException(XAException.XAER_INVAL);
        }
        catch (SystemException ex1)
        {
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_2",
					  new Object[]{xid, ex1});

	    throw new XAException(XAException.XAER_INVAL);            
        }
        catch (Exception ex6)
        {
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_2",
					  new Object[]{xid, ex6});

	    ex6.printStackTrace();

	    throw new XAException(XAException.XAER_INVAL);
        }
    }

    /**
     * @message com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_3 [com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_3] - XAResourceImple.end {0} caught: {1}
     */

    public void end (Xid xid, int flags) throws XAException
    {
        try
        {
            if (_resourceHandle != null)
            {
		Outcome res = _resourceHandle.processMessage(new XAEnd(xid, flags));
		
		if (res != null)
		{
		    if (res instanceof XAOutcome)
		    {
			generateException((XAOutcome) res);
		    }
		    else
			throw new XAException(XAException.XAER_INVAL);
		}
            }
            else
		throw new XAException(XAException.XAER_INVAL);
        }
        catch (SystemException ex1)
        {
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_3",
					  new Object[]{xid, ex1});

	    throw new XAException(XAException.XAER_INVAL);            
        }
        catch (Exception ex6)
        {
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_3",
					  new Object[]{xid, ex6});

	    ex6.printStackTrace();

	    throw new XAException(XAException.XAER_INVAL);
        }
    }

    /**
     * The record is being driven through top-level rollback.
     *
     * @message com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_4 [com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_4] - XAResourceImple.rollback {0} caught: {1}
     */

    public void rollback (Xid xid) throws XAException
    {
        try
        {
            if (_resourceHandle != null)
            {
		Outcome res = _resourceHandle.processMessage(new XARollback(xid));
		
		if (res != null)
		{
		    if (res instanceof XAOutcome)
		    {
			generateException((XAOutcome) res);
		    }
		    else
			throw new XAException(XAException.XAER_RMERR);
		}
            }
            else
		throw new XAException(XAException.XAER_RMERR);
        }
        catch (SystemException ex1)
        {
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_4",
					  new Object[]{xid, ex1});

	    throw new XAException(XAException.XA_HEURHAZ);            
        }
        catch (Exception ex6)
        {
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_4",
					  new Object[]{xid, ex6});

	    ex6.printStackTrace();

	    throw new XAException(XAException.XA_HEURHAZ);
        }
    }

    /**
     * The record is being driven through top-level commit.
     *
     * @message com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_5 [com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_5] - XAResourceImple.commit {0} caught: {1}
     */

    public void commit(Xid id, boolean onePhase) throws XAException
    {
        try
        {
            if (_resourceHandle != null)
            {
		Outcome res = _resourceHandle.processMessage(new XACommit(id, onePhase));
		
		if (res != null)
		{
		    if (res instanceof XAOutcome)
		    {
			generateException((XAOutcome) res);
		    }
		    else
			throw new XAException(XAException.XA_HEURHAZ);
		}
            }
            else
		throw new XAException(XAException.XA_HEURHAZ);
        }
        catch (SystemException ex1)
        {
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_5",
					  new Object[]{id, ex1});

	    throw new XAException(XAException.XA_HEURHAZ);
        }
        catch (Exception ex6)
        {
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_5",
					  new Object[]{id, ex6});

	    throw new XAException(XAException.XA_HEURHAZ);
        }
    }

    /**
     * The record is being driven through top-level prepare.
     *
     * @message com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_6 [com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_6] - XAResourceImple.prepare {0} caught: {1}
     */

    public int prepare (Xid xid) throws XAException
    {
        try
        {
            if (_resourceHandle != null)
            {
		Outcome res = _resourceHandle.processMessage(new XAPrepare(xid));
		
		if (res != null)
		{
		    if (res instanceof XAPrepareOutcome)
			return ((XAPrepareOutcome) res).vote();
		    else
		    {
			if (res instanceof XAOutcome)
			    generateException((XAOutcome) res);
			else
			    throw new XAException(XAException.XA_HEURHAZ);
		    }
		}
		else
		    throw new XAException(XAException.XA_HEURHAZ);
            }
            else
		throw new XAException(XAException.XAER_PROTO);
        }
        catch (SystemException e1)
        {
            /*
            * Assume that this exception is thrown to indicate a
            * communication failure or some other system-like
            * exception. In which case, crash recovery should try to
            * recover for us.
            */

	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_6",
					  new Object[]{xid, e1});

	    throw new XAException(XAException.XA_HEURHAZ);
        }
        catch (Exception e6)
        {
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_6",
					  new Object[]{xid, e6});

	    e6.printStackTrace();
        }

	throw new XAException(XAException.XA_HEURHAZ);	    
    }

    /**
     * The record generated a heuristic and can now forget about it.
     *
     * @message com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_7 [com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_7] - XAResourceImple.forget {0} caught: {1}
     */

    public void forget (Xid xid) throws XAException
    {
        try
        {
            if (_resourceHandle != null)
            {
		Outcome res = _resourceHandle.processMessage(new XAForget(xid));
		
		if (res != null)
		{
		    if (res instanceof XAOutcome)
		    {
			generateException((XAOutcome) res);
		    }
		    else
			throw new XAException(XAException.XAER_RMERR);
		}
            }
            else
		throw new XAException(XAException.XAER_RMFAIL);
        }
        catch (SystemException ex1)
        {
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_7",
					  new Object[]{xid, ex1});

	    throw new XAException(XAException.XAER_RMERR);
        }
        catch (Exception ex6)
        {
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_7",
					  new Object[]{xid, ex6});

	    throw new XAException(XAException.XAER_RMERR);
        }
    }

    /**
     * @message com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_8 [com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_8] - XAResourceImple.recover {0}
     */

    public Xid[] recover (int flag) throws XAException
    {
        try
        {
            if (_resourceHandle != null)
            {
		Outcome res = _resourceHandle.processMessage(new XARecover(flag));
		
		if (res != null)
		{
		    if (res instanceof XARecoverOutcome)
			return ((XARecoverOutcome) res).xids();
		    
		    if (res instanceof XAOutcome)
		    {
			generateException((XAOutcome) res);
		    }
		    else
			throw new XAException(XAException.XAER_RMERR);
		}
		else
		    return null;
            }
            else
		throw new XAException(XAException.XAER_RMFAIL);
        }
        catch (SystemException ex1)
        {
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_8",
					  new Object[]{ex1});

	    throw new XAException(XAException.XAER_RMERR);
        }
        catch (Exception ex6)
        {
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.jta.XAResourceImple_8",
					  new Object[]{ex6});
        }

	throw new XAException(XAException.XAER_RMERR);
    }

    private final void generateException (XAOutcome res) throws XAException
    {
	try
	{
	    Object data = res.data();
	
	    if (data instanceof XAException)
		throw (XAException) data;
	}
	catch (SystemException ex)
	{
	    throw new XAException(XAException.XAER_RMERR);
	}
    }
    
    private Participant _resourceHandle;
    private Qualifier[] _quals;
    
}

