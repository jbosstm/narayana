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
import java.io.PrintWriter;
import java.text.*;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.ats.arjuna.recovery.ExpiryScanner;
import com.arjuna.ats.arjuna.recovery.RecoveryEnvironment;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.coordinator.TxControl;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.common.util.logging.*;

/**
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionScanner_1
 *          [com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionScanner_1] -
 *          ExpiredTransactionScanner created, with expiry time of {0} seconds
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionScanner_2
 *          [com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionScanner_2] -
 *          ExpiredTransactionScanner - exception during attempted move {0} {1}
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionScanner_3
 *          [com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionScanner_3] -
 *          ExpiredTransactionScanner - could not moved log {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionScanner_4
 *          [com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionScanner_3] -
 *          ExpiredTransactionScanner - log {0} is assumed complete and will be
 *          moved.
 */

/**
 * This class is a plug-in module for the recovery manager. This class is
 * responsible for the removing ransaction status manager items that are too
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
				Uid theUid = new Uid(Uid.nullUid());

				boolean endOfUids = false;

				while (!endOfUids)
				{
					// extract a uid
					theUid.unpack(uids);

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
								if (tsLogger.arjLoggerI18N.isInfoEnabled())
									tsLogger.arjLoggerI18N
											.info(
													"com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionScanner_4",
													new Object[]
													{ newUid });

								try
								{
									InputObjectState state = _objectStore
											.read_committed(newUid, _typeName);

									if (state != null) // just in case recovery
														// kicked-in
									{
										boolean moved = _objectStore
												.write_committed(newUid,
														_movedTypeName,
														new OutputObjectState(
																state));

										if (!moved)
										{
											tsLogger.arjLoggerI18N
													.warn(
															"com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_3",
															new Object[]
															{ newUid });
										}
									}
								}
								catch (Exception ex)
								{
									tsLogger.arjLoggerI18N
											.warn(
													"com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionScanner_2",
													new Object[]
													{ newUid, ex });

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

	private String _typeName;

	private String _movedTypeName;

	private ObjectStore _objectStore;

	private Hashtable _scanM = null;

	private Hashtable _scanN = null;

}
