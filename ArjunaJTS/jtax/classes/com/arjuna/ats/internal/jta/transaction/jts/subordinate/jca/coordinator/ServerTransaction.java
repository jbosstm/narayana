/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.coordinator;

import java.io.IOException;

import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jta.utils.jtaxLogger;
import com.arjuna.ats.jta.xa.XidImple;

/**
 * This looks like an Transaction, but is only created for importing
 * transactions via JCA.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $$
 * @since JTS 4.0.
 */

/*
 * This class shouldn't need to be synchronized much since any given instance
 * should be assigned to at most one resource.
 */

public class ServerTransaction extends com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction
{

	public ServerTransaction (Uid actUid, Xid xid)
	{
		super(actUid, null);

		subordinate = true;
		
		// convert to internal format (makes saving/restoring easier)
		
		_theXid = new XidImple(xid);
	}

	public ServerTransaction (Uid actId)
	{
		super(actId);

		subordinate = true;
		
		if (!activate())  // if this fails we'll retry recovery periodically.\
		{
			_theXid = null; // should be the case anyway if activate fails, but ...
		}
	}
	
	public final Xid getXid ()
	{
		return _theXid;
	}
	
	public String type ()
	{
		return getType();
	}
	
	public static final String getType ()
	{
		return "/StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/ServerTransaction/JCA";
	}

	/*
	 * If this is a top-level transaction then we should have a recovery
	 * coordinator reference, so save it away.
	 */

	public boolean save_state (OutputObjectState os, int ot)
	{
		try
		{
			if (_theXid != null) {
				os.packBoolean(true);
				_theXid.packInto(os);
			} else {
				os.packBoolean(false);
			}
			
			return super.save_state(os, ot);
		}
		catch (IOException e)
		{
			jtaxLogger.i18NLogger.warn_cant_save_state(os, ot, e);
		}

		return false;
	}

	public boolean restore_state (InputObjectState os, int ot)
	{
		try
		{
			_theXid = null;
			
			boolean haveXid = os.unpackBoolean();

			if (haveXid)
			{
				_theXid = new XidImple();
				
				_theXid.unpackFrom(os);
			}
			
			return super.restore_state(os, ot);
		}
		catch (IOException ex)
		{
			jtaxLogger.i18NLogger.warn_cant_restore_state(os, ot, ex);
		}

		return false;
	}

	private XidImple _theXid;
	
}