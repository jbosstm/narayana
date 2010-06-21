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
 * (C) 2007,
 * @author JBoss Inc.
 */

package com.arjuna.ats.internal.arjuna.recovery;

import java.util.*;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.recovery.ExpiryScanner;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

/**
 * This class is a plug-in module for the recovery manager. This class is
 * responsible for the removing transaction status manager items that are too
 * old.
 */

public class ExpiredTransactionScanner implements ExpiryScanner
{
	public ExpiredTransactionScanner(String typeName, String movedTypeName)
	{
		_objectStore = TxControl.getStore();
		_typeName = typeName;
		_movedTypeName = movedTypeName;
	}

	/**
	 * This is called periodically by the RecoveryManager
	 */
	public void scan()
	{
		boolean initialScan = false;

		if (_scanM == null)
		{
			_scanM = new Hashtable();
			initialScan = true;
		}

		try
		{
			InputObjectState uids = new InputObjectState();

			// take a snapshot of the log

			if (_objectStore.allObjUids(_typeName, uids))
			{
				Uid theUid = null;

				boolean endOfUids = false;

				while (!endOfUids)
				{
					// extract a uid
				        theUid = UidHelper.unpackFrom(uids);

					if (theUid.equals(Uid.nullUid()))
						endOfUids = true;
					else
					{
						Uid newUid = new Uid(theUid);

						if (initialScan)
							_scanM.put(newUid, newUid);
						else
						{
							if (!_scanM.contains(newUid))
							{
								if (_scanN == null)
									_scanN = new Hashtable();

								_scanN.put(newUid, newUid);
							}
							else
							// log is present in this iteration, so move it
							{
								tsLogger.i18NLogger.info_recovery_ExpiredTransactionScanner_4(newUid);

								try
								{
								    moveEntry(newUid);
								}
								catch (Exception ex)
								{
                                    tsLogger.i18NLogger.warn_recovery_ExpiredTransactionScanner_2(newUid, ex);

									_scanN.put(newUid, newUid);
								}
							}
						}
					}
				}

				if (_scanN != null)
				{
					_scanM = _scanN;
					_scanN = null;
				}
			}
		}
		catch (Exception e)
		{
			// end of uids!
		}
	}

	public boolean toBeUsed()
	{
		return true;
	}

	public boolean moveEntry (Uid newUid) throws ObjectStoreException
	{
	    InputObjectState state = _objectStore.read_committed(newUid, _typeName);
	    boolean res = false;
	    
	    if (state != null) // just in case recovery
	        // kicked-in
	    {
	        boolean moved = _objectStore.write_committed(newUid, _movedTypeName, new OutputObjectState(state));

	        if (!moved) {
                tsLogger.i18NLogger.info_recovery_ExpiredTransactionStatusManagerScanner_3(newUid);
            }
	        else
	            res = _objectStore.remove_committed(newUid, _typeName);
	    }
          
	    return res;
	}
	
	private String _typeName;

	private String _movedTypeName;

	private ObjectStore _objectStore;

	private Hashtable _scanM = null;

	private Hashtable _scanN = null;

}
