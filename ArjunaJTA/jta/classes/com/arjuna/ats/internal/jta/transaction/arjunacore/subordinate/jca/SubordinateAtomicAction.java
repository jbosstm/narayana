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
 * Copyright (C) 2003,
 * 
 * Hewlett-Packard Arjuna Labs, Newcastle upon Tyne, Tyne and Wear, UK.
 * 
 * $Id: SubordinateAtomicAction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca;

import java.io.IOException;

import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.jta.xa.XidImple;

/**
 * A subordinate JTA transaction; used when importing another transaction
 * context via JCA. This overrides the basic subordinate transaction simply so
 * that we can ensure JCA transactions are laid out in the right place in the
 * object store.
 * 
 * @author mcl
 */

public class SubordinateAtomicAction extends
		com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.SubordinateAtomicAction
{

	public SubordinateAtomicAction ()
	{
		super();  // does start for us

		_activated = true;
		_theXid = new XidImple(Uid.nullUid());
	}

	public SubordinateAtomicAction (Uid actId)
	{
		super(actId);
		
		_activated = activate(); // if this fails, we'll retry later.
	}
	
	public SubordinateAtomicAction (int timeout, Xid xid)
	{
		super(timeout); // implicit start (done in base class)
		
		_theXid = new XidImple(xid);
		_activated = true;
	}
	
	/**
	 * The type of the class is used to locate the state of the transaction log
	 * in the object store.
	 * 
	 * Overloads BasicAction.type()
	 * 
	 * @return a string representation of the hierarchy of the class for storing
	 *         logs in the transaction object store.
	 */

	public String type ()
	{
		return getType();
	}

	public static final String getType ()
	{
		return "/StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction/SubordinateAtomicAction/JCA";
	}
	
	public final Xid getXid ()
	{
		return _theXid;
	}

	public boolean save_state (OutputObjectState os, int t)
	{
	    try
	    {
	        if (_theXid != null)
	        {
	            os.packBoolean(true);

	            ((XidImple) _theXid).packInto(os);
	        }
	        else
	            os.packBoolean(false);
	    }
	    catch (IOException ex)
	    {
	        return false;
	    }

	    return super.save_state(os, t);
	}
	
	public boolean restore_state (InputObjectState os, int t)
	{
	    _theXid = null;
	    
		try
		{
			boolean haveXid = os.unpackBoolean();
			
			if (haveXid)
			{
				_theXid = new XidImple();
				
				((XidImple) _theXid).unpackFrom(os);
			}
		}
		catch (IOException ex)
		{
			return false;
		}
		
		return super.restore_state(os, t);
	}

    public boolean activated ()
    {
    	return _activated;
    }

	private Xid _theXid;
    private boolean _activated;
}
