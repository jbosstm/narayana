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
 * Copyright (C) 2005,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ServerTransaction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.coordinator;

import com.arjuna.ats.jta.xa.XidImple;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;

import java.io.IOException;

import javax.transaction.xa.Xid;

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
		
		// convert to internal format (makes saving/restoring easier)
		
		_theXid = new XidImple(xid);
	}

	public ServerTransaction (Uid actId)
	{
		super(actId);
		
		activate();  // if this fails we'll retry recovery periodically.
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
			if (_theXid != null)
				os.packBoolean(true);
			else
				os.packBoolean(false);
			
			_theXid.packInto(os);
			
			return super.save_state(os, ot);
		}
		catch (IOException e)
		{
			e.printStackTrace();
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
			ex.printStackTrace();
		}

		return false;
	}

	private XidImple _theXid;
	
}
