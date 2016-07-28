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
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.ats.internal.arjuna.Header;
import com.arjuna.ats.internal.jta.xa.XID;
import com.arjuna.ats.jta.xa.XATxConverter;
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

	/**
	 * @deprecated This is only used by test code
	 */
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

	/**
	 * Recovery SAA. If the record is removed and peekXidOnly is true then the Xid will be null.
	 *
	 * @param actId
	 * @param peekXidOnly
	 * @throws ObjectStoreException
	 * @throws IOException
     */
	public SubordinateAtomicAction(Uid actId, boolean peekXidOnly) throws ObjectStoreException, IOException {
		super(actId);
		if (peekXidOnly) {
			InputObjectState os = StoreManager.getParticipantStore().read_committed(objectUid, type());
			if (os == null) {
				// This will have been logged by the ObjectStore during ShadowingStore::read_state as an INFO if there was no content
				return;
			}
			unpackHeader(os, new Header());
			boolean haveXid = os.unpackBoolean();

			if (haveXid) {
				_theXid = new XidImple();

				((XidImple) _theXid).unpackFrom(os);
				_parentNodeName = os.unpackString();
			}
		} else {
			_activated = activate();
		}
	}
	
	public SubordinateAtomicAction (int timeout, Xid xid)
	{
		super(timeout); // implicit start (done in base class)
		
		if (xid != null && xid.getFormatId() == XATxConverter.FORMAT_ID) {
			XidImple toImport = new XidImple(xid);
			XID toCheck = toImport.getXID();
			_parentNodeName = XATxConverter.getSubordinateNodeName(toCheck);
			if (_parentNodeName == null) {
				_parentNodeName = XATxConverter.getNodeName(toCheck);
			}
			XATxConverter.setSubordinateNodeName(toImport.getXID(), TxControl.getXANodeName());
			_theXid = new XidImple(toImport);
		} else {
			_theXid = new XidImple(xid);
		}
		
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

	/**
	 * If the record was removed Xid will be null
	 *
	 * @return
     */
	public final Xid getXid ()
	{
	    // could be null if activation failed.
	    
		return _theXid;
	}
	
	public String getParentNodeName() {
		return _parentNodeName;
	}

	public boolean save_state (OutputObjectState os, int t)
	{
	    try
	    {
	        // pack the header first for the benefit of the tooling
	        packHeader(os, new Header(get_uid(), Utility.getProcessUid()));

	        if (_theXid != null)
	        {
	            os.packBoolean(true);

	            ((XidImple) _theXid).packInto(os);
	            os.packString(_parentNodeName);
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
			unpackHeader(os, new Header());

			boolean haveXid = os.unpackBoolean();
			
			if (haveXid)
			{
				_theXid = new XidImple();
				
				((XidImple) _theXid).unpackFrom(os);
				_parentNodeName = os.unpackString();
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
	private String _parentNodeName;
    private boolean _activated;
}
