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

package com.arjuna.ats.internal.arjuna.abstractrecords;

import java.io.IOException;
import java.io.PrintWriter;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

/**
 * A PersistenceRecord is created whenever a persistent object is
 * created/read/modified within the scope of a transaction. It is responsible
 * for ensuring that state changes are committed or rolled back on behalf of the
 * object depending upon the outcome of the transaction.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: PersistenceRecord.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class PersistenceRecord extends RecoveryRecord
{

	/**
	 * This constructor is used to create a new instance of PersistenceRecord.
	 */

	public PersistenceRecord (OutputObjectState os, ParticipantStore participantStore, StateManager sm)
	{
		super(os, sm);

		if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("PersistenceRecord::PersistenceRecord("
                    + os + ", " + sm.get_uid() + ")");
        }

		shadowMade = false;
		this.targetParticipantStore = participantStore;
		topLevelState = null;
	}

	/**
	 * Redefintions of abstract functions inherited from RecoveryRecord.
	 */

	public int typeIs ()
	{
		return RecordType.PERSISTENCE;
	}

	/**
	 * topLevelAbort may have to remove the persistent state that was written
	 * into the object store during the processing of topLevelPrepare. It then
	 * does the standard abort processing.
	 */

	public int topLevelAbort ()
	{
	    if (tsLogger.logger.isTraceEnabled()) {
	        tsLogger.logger.trace("PersistenceRecord::topLevelAbort() for "
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
	        if (!targetParticipantStore.remove_uncommitted(uid, type)) {
	            tsLogger.i18NLogger.warn_PersistenceRecord_19();

	            return TwoPhaseOutcome.FINISH_ERROR;
	        }
	    }
	    catch (ObjectStoreException e) {
	        tsLogger.i18NLogger.warn_PersistenceRecord_20(e);

	        return TwoPhaseOutcome.FINISH_ERROR;
	    }

	    return nestedAbort();
	}

	/**
	 * commit the state saved during the prepare phase.
	 */

	public int topLevelCommit ()
	{
		if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("PersistenceRecord::topLevelCommit() : About to commit state, "+
                    "uid = "+order()+", ObjType = "+getTypeOfObject());
        }

		if (tsLogger.logger.isTraceEnabled())
		{
			if (targetParticipantStore != null) {
                tsLogger.logger.trace(", store = "
                        + targetParticipantStore + "(" + targetParticipantStore.getClass().getCanonicalName() + ")");
            }
			else {
                tsLogger.logger.trace("");
            }
		}

		boolean result = false;

		if (targetParticipantStore != null)
		{
			try
			{
				if (shadowMade)
				{
					result = targetParticipantStore.commit_state(order(), super.getTypeOfObject());

					if (!result) {
                        tsLogger.i18NLogger.warn_PersistenceRecord_2(order());
                    }
				}
				else
				{
					if (topLevelState != null)
					{
						result = targetParticipantStore.write_committed(order(), super.getTypeOfObject(), topLevelState);
					}
					else {
                        tsLogger.i18NLogger.warn_PersistenceRecord_3();
                    }
				}
			}
			catch (ObjectStoreException e) {
                tsLogger.i18NLogger.warn_PersistenceRecord_4(e);

                result = false;
            }
		}
		else {
            tsLogger.i18NLogger.warn_PersistenceRecord_5();
        }

		if (!result) {
            tsLogger.i18NLogger.warn_PersistenceRecord_6();
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
		if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("PersistenceRecord::topLevelPrepare() for "
                    + order());
        }

		int result = TwoPhaseOutcome.PREPARE_NOTOK;
		StateManager sm = super.objectAddr;

		if ((sm != null) && (targetParticipantStore != null))
		{
		    /*
		     * Get ready to create our state to be saved. At this stage we're not
		     * sure if the state will go into its own log or be written into the
		     * transaction log for improved performance.
		     */
		    
			topLevelState = new OutputObjectState(sm.get_uid(), sm.type());

			if (writeOptimisation
					&& (!targetParticipantStore.fullCommitNeeded()
							&& (sm.save_state(topLevelState, ObjectType.ANDPERSISTENT)) && (topLevelState.size() <= PersistenceRecord.MAX_OBJECT_SIZE)))
			{
			    /*
			     * We assume that crash recovery will always run before
			     * the object can be reactivated!
			     */

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
						if (targetParticipantStore.write_uncommitted(sm.get_uid(), sm.type(), dummy))
						    result = TwoPhaseOutcome.PREPARE_OK;
						else
						{
						    result = TwoPhaseOutcome.PREPARE_NOTOK;
						}
					}
					catch (ObjectStoreException e) {
                        tsLogger.i18NLogger.warn_PersistenceRecord_21(e);
                    }

					dummy = null;
				}
				else
				{
				    /*
				     * Don't write anything as our state will go into the log.
				     */
				    
					result = TwoPhaseOutcome.PREPARE_OK;
				}
			}
			else
			{
			    if (sm.deactivate(targetParticipantStore.getStoreName(), false))
			    {
			        shadowMade = true;

			        result = TwoPhaseOutcome.PREPARE_OK;
			    }
			    else 
			    {
			        topLevelState = null;
			        
			        tsLogger.i18NLogger.warn_PersistenceRecord_7();
			    }
			}
		}
		else {
            tsLogger.i18NLogger.warn_PersistenceRecord_8();
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
		if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("PersistenceRecord::topLevelCleanup() for "
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
		if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("PersistenceRecord::restore_state() for "
                    + order());
        }

		boolean res = false;
		topLevelState = null;

        try
        {
            shadowMade = os.unpackBoolean();

            // topLevelState = null;

            if (!shadowMade)
            {
                topLevelState = new OutputObjectState(os);
                res = topLevelState.valid();
            }
            else
                res = true;

            res = (res && super.restore_state(os, ot));

            // Note: we don't persist the targetParticipantStore, instead assuming the
            // default one present at recovery time will be equivalent. Changing the
            // objectstore config when records exist in the tx store is therefore a Bad Thing.
            targetParticipantStore = getStore();

            return res;
        }
        catch (final Exception e) {
            tsLogger.i18NLogger.warn_PersistenceRecord_10();
        }

		return res;
	}

	public boolean save_state (OutputObjectState os, int ot)
	{
		if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("PersistenceRecord::save_state() for "
                    + order());
        }

		boolean res = true;

		if (targetParticipantStore != null)
		{
            // Note: we don't persist the targetParticipantStore, instead assuming the
            // default one present at recovery time will be equivalent. Changing the
            // objectstore config when records exist in the tx store is therefore a Bad Thing.

            try
            {
                os.packBoolean(shadowMade);

                /*
                         * If we haven't written a shadow state, then pack the state
                         * into the transaction log. There MUST be a state at this
                         * point.
                         */

                if (!shadowMade)
                {
                    res = (topLevelState != null);

                    if (res)
                        topLevelState.packInto(os);
                    else {
                        tsLogger.i18NLogger.warn_PersistenceRecord_14();
                    }
                }
            }
            catch (IOException e) {
                res = false;

                tsLogger.i18NLogger.warn_PersistenceRecord_15();
            }

		}
		else {
            tsLogger.i18NLogger.warn_PersistenceRecord_16();

            try {
                os.packString(null);
            }
            catch (IOException e) {
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

	/**
	 * Creates a 'blank' persistence record. This is used during crash recovery
	 * when recreating the prepared list of a server atomic action.
	 */

	public PersistenceRecord ()
	{
		super();

		if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("PersistenceRecord::PersistenceRecord() - crash recovery constructor");
        }

		shadowMade = false;
		targetParticipantStore = null;
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
	protected ParticipantStore targetParticipantStore;
	protected OutputObjectState topLevelState;
	protected static boolean classicPrepare = false;
	
	private static boolean writeOptimisation = false;

	static
	{
        classicPrepare = arjPropertyManager.getCoordinatorEnvironmentBean().isClassicPrepare();

        writeOptimisation = arjPropertyManager.getCoordinatorEnvironmentBean().isWriteOptimisation();
	}
}
