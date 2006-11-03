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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: PersistenceRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreType;
import java.io.PrintWriter;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import java.io.IOException;

/**
 * A PersistenceRecord is created whenever a persistent object is
 * created/read/modified within the scope of a transaction. It is responsible
 * for ensuring that state changes are committed or rolled back on behalf of the
 * object depending upon the outcome of the transaction.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: PersistenceRecord.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 * 
 * 
 * @message com.arjuna.ats.arjuna.PersistenceRecord_1
 *          [com.arjuna.ats.arjuna.PersistenceRecord_1]
 *          PersistenceRecord::topLevelCommit() : About to commit state, uid =
 *          {0}, ObjType = {1}
 * @message com.arjuna.ats.arjuna.PersistenceRecord_2
 *          [com.arjuna.ats.arjuna.PersistenceRecord_2]
 *          PersistenceRecord::topLevelCommit - commit_state call failed for {0}
 * @message com.arjuna.ats.arjuna.PersistenceRecord_3
 *          [com.arjuna.ats.arjuna.PersistenceRecord_3]
 *          PersistenceRecord::topLevelCommit - no state to commit!
 * @message com.arjuna.ats.arjuna.PersistenceRecord_4
 *          [com.arjuna.ats.arjuna.PersistenceRecord_4]
 *          PersistenceRecord::topLevelCommit - caught exception: {0}
 * @message com.arjuna.ats.arjuna.PersistenceRecord_5
 *          [com.arjuna.ats.arjuna.PersistenceRecord_5]
 *          PersistenceRecord::topLevelCommit - no object store specified!
 * @message com.arjuna.ats.arjuna.PersistenceRecord_6
 *          [com.arjuna.ats.arjuna.PersistenceRecord_6]
 *          PersistenceRecord::topLevelCommit - commit_state error
 * @message com.arjuna.ats.arjuna.PersistenceRecord_7
 *          [com.arjuna.ats.arjuna.PersistenceRecord_7] PersistenceRecord
 *          deactivate error
 * @message com.arjuna.ats.arjuna.PersistenceRecord_8
 *          [com.arjuna.ats.arjuna.PersistenceRecord_8]
 *          PersistenceRecord.topLevelPrepare - setup error!
 * @message com.arjuna.ats.arjuna.PersistenceRecord_9
 *          [com.arjuna.ats.arjuna.PersistenceRecord_9]
 *          PersistenceRecord::restore_state: Just unpacked object store type =
 *          {0}
 * @message com.arjuna.ats.arjuna.PersistenceRecord_10
 *          [com.arjuna.ats.arjuna.PersistenceRecord_10]
 *          PersistenceRecord::restore_state: Failed to unpack object store type
 * @message com.arjuna.ats.arjuna.PersistenceRecord_11
 *          [com.arjuna.ats.arjuna.PersistenceRecord_11]
 *          PersistenceRecord::save_state - type of store is unknown
 * @message com.arjuna.ats.arjuna.PersistenceRecord_12
 *          [com.arjuna.ats.arjuna.PersistenceRecord_12]
 *          PersistenceRecord::save_state: Packed object store type = {0}
 * @message com.arjuna.ats.arjuna.PersistenceRecord_13
 *          [com.arjuna.ats.arjuna.PersistenceRecord_13]
 *          PersistenceRecord::save_state: Packed object store root
 * @message com.arjuna.ats.arjuna.PersistenceRecord_14
 *          [com.arjuna.ats.arjuna.PersistenceRecord_14]
 *          PersistenceRecord::save_state - packing top level state failed
 * @message com.arjuna.ats.arjuna.PersistenceRecord_15
 *          [com.arjuna.ats.arjuna.PersistenceRecord_15]
 *          PersistenceRecord::save_state - failed
 * @message com.arjuna.ats.arjuna.PersistenceRecord_16
 *          [com.arjuna.ats.arjuna.PersistenceRecord_16]
 *          PersistenceRecord::save_state - no object store defined for object
 * @message com.arjuna.ats.arjuna.PersistenceRecord_17
 *          [com.arjuna.ats.arjuna.PersistenceRecord_17]
 *          PersistenceRecord::PersistenceRecord() - crash recovery constructor
 * @message com.arjuna.ats.arjuna.PersistenceRecord_18
 *          [com.arjuna.ats.arjuna.PersistenceRecord_18]
 *          PersistenceRecord::topLevelAbort() - Expecting state but found none!
 * @message com.arjuna.ats.arjuna.PersistenceRecord_20
 *          [com.arjuna.ats.arjuna.PersistenceRecord_20]
 *          PersistenceRecord::topLevelAbort() - Received ObjectStoreException
 *          {0}
 * @message com.arjuna.ats.arjuna.PersistenceRecord_21
 *          [com.arjuna.ats.arjuna.PersistenceRecord_21]
 *          PersistenceRecord.topLevelPrepare - write_uncommitted error
 */

public class PersistenceRecord extends RecoveryRecord
{

	/**
	 * This constructor is used to create a new instance of PersistenceRecord.
	 */

	public PersistenceRecord (OutputObjectState os, ObjectStore objStore, StateManager sm)
	{
		super(os, sm);

		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ABSTRACT_REC, "PersistenceRecord::PersistenceRecord("
					+ os + ", " + sm.get_uid() + ")");
		}

		shadowMade = false;
		store = objStore;
		topLevelState = null;
	}

	public void finalize ()
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.DESTRUCTORS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ABSTRACT_REC, "PersistenceRecord.finalize() for "
					+ order());
		}

		store = null;
		topLevelState = null;

		super.finalize();
	}

	/**
	 * Redefintions of abstract functions inherited from RecoveryRecord.
	 */

	public int typeIs ()
	{
		return RecordType.PERSISTENCE;
	}

	public ClassName className ()
	{
		return ArjunaNames.Implementation_AbstractRecord_PersistenceRecord();
	}

	/**
	 * topLevelAbort may have to remove the persistent state that was written
	 * into the object store during the processing of topLevelPrepare. It then
	 * does the standard abort processing.
	 */

	public int topLevelAbort ()
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ABSTRACT_REC, "PersistenceRecord::topLevelAbort() for "
					+ order());
		}

		Uid uid = null;
		String type = null;

		if (shadowMade) // state written by StateManager instance
		{
			uid = order();
			type = getTypeOfObject();
		}
		else
		{
			if (topLevelState == null) // hasn't been prepared, so no state
			{
				return nestedAbort();
			}
			else
			{
				uid = topLevelState.stateUid();
				type = topLevelState.type();
			}
		}

		try
		{
			if (!store.remove_uncommitted(uid, type))
			{
				if (tsLogger.arjLoggerI18N.isWarnEnabled())
				{
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_19");
				}

				return TwoPhaseOutcome.FINISH_ERROR;
			}
		}
		catch (ObjectStoreException e)
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
				tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_20", new Object[]
				{ e });
			}

			e.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}

		return nestedAbort();
	}

	/**
	 * commit the state saved during the prepare phase.
	 */

	public int topLevelCommit ()
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ABSTRACT_REC, "PersistenceRecord::topLevelCommit() for "
					+ order());
		}

		if (tsLogger.arjLoggerI18N.debugAllowed())
		{
			tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ABSTRACT_REC, "com.arjuna.ats.arjuna.PersistenceRecord_1", new Object[]
			{ order(), getTypeOfObject() });
		}

		if (tsLogger.arjLogger.debugAllowed())
		{
			if (store != null)
			{
				tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ABSTRACT_REC, ", store = "
						+ store + "(" + store.typeIs() + ")");
			}
			else
			{
				tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ABSTRACT_REC, "");
			}
		}

		boolean result = false;

		if (store != null)
		{
			try
			{
				if (shadowMade)
				{
					result = store.commit_state(order(), super.getTypeOfObject());

					if (!result)
					{
						if (tsLogger.arjLoggerI18N.isWarnEnabled())
						{
							tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_2", new Object[]
							{ order() });
						}
					}
				}
				else
				{
					if (topLevelState != null)
					{
						result = store.write_committed(order(), super.getTypeOfObject(), topLevelState);
					}
					else
					{
						if (tsLogger.arjLoggerI18N.isWarnEnabled())
							tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_3");
					}
				}
			}
			catch (ObjectStoreException e)
			{
				if (tsLogger.arjLoggerI18N.isWarnEnabled())
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_4", new Object[]
					{ e });

				result = false;
			}
		}
		else
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
				tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_5");
		}

		if (!result)
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
				tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_6");
		}

		super.forgetAction(true);

		return ((result) ? TwoPhaseOutcome.FINISH_OK
				: TwoPhaseOutcome.FINISH_ERROR);
	}

	/**
	 * topLevelPrepare attempts to save the object. It will either do this in
	 * the action intention list or directly in the object store by using the
	 * 'deactivate' function of the object depending upon the size of the state.
	 * To ensure that objects are correctly hidden while they are in an
	 * uncommitted state if we use the abbreviated protocol then we write an
	 * EMPTY object state as the shadow state - THIS MUST NOT BE COMMITTED.
	 * Instead we write_committed the one saved in the intention list. If the
	 * store cannot cope with being given an empty state we revert to the old
	 * protocol.
	 */

	public int topLevelPrepare ()
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ABSTRACT_REC, "PersistenceRecord::topLevelPrepare() for "
					+ order());
		}

		int result = TwoPhaseOutcome.PREPARE_NOTOK;
		StateManager sm = super.objectAddr;

		if ((sm != null) && (store != null))
		{
			topLevelState = new OutputObjectState(sm.get_uid(), sm.type());

			if (writeOptimisation
					|| (!store.fullCommitNeeded()
							&& (sm.save_state(topLevelState, ObjectType.ANDPERSISTENT)) && (topLevelState.size() <= PersistenceRecord.MAX_OBJECT_SIZE)))
			{
				if (PersistenceRecord.classicPrepare)
				{
					OutputObjectState dummy = new OutputObjectState(
							Uid.nullUid(), null);

					/*
					 * Write an empty shadow state to the store to indicate one
					 * exists, and to prevent bogus activation in the case where
					 * crash recovery hasn't run yet.
					 */

					try
					{
						store.write_uncommitted(sm.get_uid(), sm.type(), dummy);
						result = TwoPhaseOutcome.PREPARE_OK;
					}
					catch (ObjectStoreException e)
					{
						tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_21", e);
					}

					dummy = null;
				}
				else
				{
					result = TwoPhaseOutcome.PREPARE_OK;
				}
			}
			else
			{
				if (sm.deactivate(store.getStoreName(), false))
				{
					shadowMade = true;

					result = TwoPhaseOutcome.PREPARE_OK;
				}
				else
				{
					if (tsLogger.arjLoggerI18N.isWarnEnabled())
						tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_7");
				}
			}
		}
		else
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
				tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_8");
		}

		return result;
	}

	/**
	 * topLevelCleanup must leave the persistent state that was written in the
	 * object store during the processing of topLevelPrepare intact. Crash
	 * recovery will take care of its resolution
	 */

	public int topLevelCleanup ()
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ABSTRACT_REC, "PersistenceRecord::topLevelCleanup() for "
					+ order());
		}

		return TwoPhaseOutcome.FINISH_OK;
	}

	/**
	 * @return <code>true</code>
	 */

	public boolean doSave ()
	{
		return true;
	}

	public boolean restore_state (InputObjectState os, int ot)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ABSTRACT_REC, "PersistenceRecord::restore_state() for "
					+ order());
		}

		boolean res = false;
		int objStoreType = 0;

		try
		{
			objStoreType = os.unpackInt();

			if (tsLogger.arjLoggerI18N.debugAllowed())
			{
				tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ABSTRACT_REC, "com.arjuna.ats.arjuna.PersistenceRecord_9", new Object[]
				{ Integer.toString(objStoreType) });
			}

			if (ObjectStoreType.valid(objStoreType))
			{
				/* discard old store before creating new */

				if (store == null)
					store = new ObjectStore(
							ObjectStoreType.typeToClassName(objStoreType));

				store.unpack(os);
				shadowMade = os.unpackBoolean();

				// topLevelState = null;

				if (!shadowMade)
				{
					topLevelState = new OutputObjectState(os);
					res = topLevelState.valid();
				}
				else
					res = true;

				return (res && super.restore_state(os, ot));
			}
		}
		catch (IOException e)
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
				tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_10");
		}

		return res;
	}

	public boolean save_state (OutputObjectState os, int ot)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ABSTRACT_REC, "PersistenceRecord::save_state() for "
					+ order());
		}

		boolean res = true;

		if (store != null)
		{
			if (!ObjectStoreType.valid(store.typeIs()))
			{
				if (tsLogger.arjLoggerI18N.isWarnEnabled())
				{
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_11");
				}

				res = false;
			}
			else
			{
				try
				{
					os.packInt(store.typeIs());

					if (tsLogger.arjLoggerI18N.debugAllowed())
					{
						tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ABSTRACT_REC, "com.arjuna.ats.arjuna.PersistenceRecord_12", new Object[]
						{ Integer.toString(store.typeIs()) });
					}

					store.pack(os);

					if (tsLogger.arjLoggerI18N.debugAllowed())
					{
						tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ABSTRACT_REC, "com.arjuna.ats.arjuna.PersistenceRecord_13");
					}

					os.packBoolean(shadowMade);

					/*
					 * If we haven't written a shadow state, then pack the state
					 * into the transaction log.
					 */

					if (!shadowMade)
					{
						res = (topLevelState != null);

						if (res)
							topLevelState.packInto(os);
						else
						{
							if (tsLogger.arjLoggerI18N.isWarnEnabled())
								tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_14");
						}
					}
				}
				catch (IOException e)
				{
					res = false;

					if (tsLogger.arjLoggerI18N.isWarnEnabled())
						tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_15");
				}
			}
		}
		else
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
				tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_16");

			try
			{
				os.packString(null);
			}
			catch (IOException e)
			{
				res = false;
			}
		}

		return res && super.save_state(os, ot);
	}

	public void print (PrintWriter strm)
	{
		super.print(strm); /* bypass RecoveryRecord */

		strm.println("PersistenceRecord with state:\n" + super.state);
	}

	public String type ()
	{
		return "/StateManager/AbstractRecord/RecoveryRecord/PersistenceRecord";
	}

	public static AbstractRecord create ()
	{
		return new PersistenceRecord();
	}

	/**
	 * Creates a 'blank' persistence record. This is used during crash recovery
	 * when recreating the prepared list of a server atomic action.
	 */

	protected PersistenceRecord ()
	{
		super();

		if (tsLogger.arjLoggerI18N.debugAllowed())
		{
			tsLogger.arjLoggerI18N.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PROTECTED, FacilityCode.FAC_ABSTRACT_REC, "com.arjuna.ats.arjuna.PersistenceRecord_17");
		}

		shadowMade = false;
		store = null;
		topLevelState = null;
	}

	/**
	 * Cadaver records force write shadows. This operation supresses to
	 * abbreviated commit This should never return false
	 */

	protected boolean shadowForced ()
	{
		if (topLevelState == null)
		{
			shadowMade = true;

			return true;
		}

		/* I've already done the abbreviated protocol so its too late */

		return false;
	}

	// this value should really come from the object store implementation!

	public static final int MAX_OBJECT_SIZE = 4096; // block size

	protected boolean shadowMade;
	protected ObjectStore store;
	protected OutputObjectState topLevelState;
	protected static boolean classicPrepare = false;
	
	private static boolean writeOptimisation = false;

	static
	{
		String cp = arjPropertyManager.propertyManager.getProperty(com.arjuna.ats.arjuna.common.Environment.CLASSIC_PREPARE);

		if (cp != null)
		{
			if (cp.equals("YES"))
				classicPrepare = true;
		}

		String wo = arjPropertyManager.propertyManager.getProperty(com.arjuna.ats.arjuna.common.Environment.TRANSACTION_LOG_WRITE_OPTIMISATION);

		if (wo != null)
		{
			if (wo.equals("YES"))
			{
				writeOptimisation = true;
			}
		}
	}
}
