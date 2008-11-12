/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ParticipantRecord.java,v 1.6 2005/05/19 12:13:37 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.sagas.arjunacore;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;

import com.arjuna.mw.wscf.model.sagas.participants.Participant;
import com.arjuna.mw.wscf.model.sagas.participants.ParticipantWithComplete;
import com.arjuna.mw.wscf.model.sagas.participants.RecoverableParticipant;
import com.arjuna.mw.wscf.model.sagas.exceptions.CompensateFailedException;
import com.arjuna.mw.wscf.model.sagas.exceptions.CancelFailedException;

import com.arjuna.mw.wsas.exceptions.*;

import com.arjuna.mw.wscf.exceptions.*;
import com.arjuna.webservices.util.ClassLoaderHelper;

import java.io.PrintWriter;

/**
 * Arjuna abstract record to handle two-phase participants.
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ParticipantRecord.java,v 1.6 2005/05/19 12:13:37 nmcl Exp $
 */

public class ParticipantRecord extends
		com.arjuna.ats.arjuna.coordinator.AbstractRecord
{

	/**
	 * Constructor.
	 * 
	 * @param theResource
	 *            is the proxy that allows us to call out to the object.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_1
	 *          [com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_1] -
	 *          ParticipantRecord {0} - null participant provided!
	 */

	public ParticipantRecord (RecoverableParticipant theResource, Uid id)
	{
		super(id, null, ObjectType.ANDPERSISTENT);

		_resourceHandle = theResource;
		_timeout = 0;
		_coordId = new CoordinatorIdImple(id);

		if (theResource == null)
			wscfLogger.arjLoggerI18N
					.warn(
							"com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_1",
							new Object[] { order() });
	}

	public void finalize () throws Throwable
	{
		_resourceHandle = null;

		super.finalize();
	}

	/**
	 * Override AbstractRecord.propagateOnCommit
	 */

	public boolean propagateOnCommit ()
	{
		return true;
	}

	/**
	 * The type of this abstract record.
	 */

	public int typeIs ()
	{
		// TODO add to record list
		
		return RecordType.XTS_WSBA_RECORD;
	}

	/**
	 * The class name for this record.
	 */

	public ClassName className ()
	{
		return new ClassName("WSBAParticipantRecord");
	}

	/**
	 * The internal value.
	 */

	public Object value ()
	{
		return _resourceHandle;
	}

	/**
	 * Set the internal value. Not allowed for this class.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_2
	 *          [com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_2] -
	 *          ParticipantRecord.setValue() called illegally.
	 */

	public void setValue (Object o)
	{
		wscfLogger.arjLoggerI18N
				.warn("com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_2");
	}

	/**
	 * The record is being driven through nested rollback.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_3
	 *          [com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_3] -
	 *          ParticipantRecord.nestedAbort {0} caught: {1}
	 */

	// TODO
	public int nestedAbort ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				return TwoPhaseOutcome.FINISH_ERROR;
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (Exception ex6)
		{
			wscfLogger.arjLoggerI18N
					.warn(
							"com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_3",
							new Object[] { order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record is being driven through nested commit.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_4
	 *          [com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_4] -
	 *          ParticipantRecord.nestedCommit {0} caught: {1}
	 */

	public int nestedCommit ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				return TwoPhaseOutcome.FINISH_ERROR;
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (Exception ex6)
		{
			wscfLogger.arjLoggerI18N
					.warn(
							"com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_4",
							new Object[] { order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record is being driven through nested prepare.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_5
	 *          [com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_5] -
	 *          ParticipantRecord.nestedPrepare {0} caught: {1}
	 */

	public int nestedPrepare ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				return TwoPhaseOutcome.FINISH_ERROR;
			}
			else
				return TwoPhaseOutcome.PREPARE_NOTOK;
		}
		catch (Exception e6)
		{
			wscfLogger.arjLoggerI18N
					.warn(
							"com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_5",
							new Object[] { order(), e6 });

			e6.printStackTrace();

			return TwoPhaseOutcome.HEURISTIC_HAZARD;
		}
	}

	/**
	 * The record is being driven through top-level rollback.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_6
	 *          [com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_6] -
	 *          ParticipantRecord.topLevelAbort {0} caught: {1}
	 */

	public int topLevelAbort ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				try
				{
					if (!_exited)
					{
						if (_completed) _resourceHandle.compensate();
						else
							_resourceHandle.cancel();
					}
				}
				catch (InvalidParticipantException ex)
				{
					return TwoPhaseOutcome.FINISH_ERROR;
				}
                catch (WrongStateException ex)
                {
                    // this indicates a fail occured and was detected during cancel (or compensation?) so we return a
                    // HEURISTIC_HAZARD which will place the participant in the heuristic list
                    return TwoPhaseOutcome.HEURISTIC_HAZARD;
                }
                catch (CancelFailedException ex)
                {
                    // this indicates a fail occured and was detected during cancel so we return a HEURISTIC_HAZARD
                    // which will place the participant in the heuristic list
                    return TwoPhaseOutcome.HEURISTIC_HAZARD;
                }
                catch (CompensateFailedException ex)
                {
                    // this indicates a fail occured during compensation so we return a HEURISTIC_HAZARD
                    // which will place the participant in the heuristic list
                    return TwoPhaseOutcome.HEURISTIC_HAZARD;
                }
				catch (SystemException ex)
				{
                    // this indicates a comms failure so we return FINISH_ERROR which will place
                    // the participant in the failed list and cause a retry of the close
                    return TwoPhaseOutcome.FINISH_ERROR;
				}

                // we are not guaranteed to detect all state transitions so we still have to
                // make sure we did not fail and then end while we were trying to cancel or
                // compensate

                if (_failed) {
                    return TwoPhaseOutcome.HEURISTIC_HAZARD;
                }

				return TwoPhaseOutcome.FINISH_OK;
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (Exception ex6)
		{
			wscfLogger.arjLoggerI18N
					.warn(
							"com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_6",
							new Object[] { order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record is being driven through top-level commit.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_7
	 *          [com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_7] -
	 *          ParticipantRecord.topLevelCommit {0} caught: {1}
	 */

	public int topLevelCommit ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				try
				{
					if (!_exited) _resourceHandle.close();
				}
				catch (InvalidParticipantException ex)
				{
					return TwoPhaseOutcome.FINISH_ERROR;
				}
				catch (WrongStateException ex)
				{
                    // this indicates a failure to close so we notify a heuristic hazard
					return TwoPhaseOutcome.HEURISTIC_HAZARD;
				}
                catch (SystemException ex)
				{
                    // this indicates a comms failure so we return FINISH_ERROR which will place
                    // the participant in the failed list and cause a retry of the close
                    return TwoPhaseOutcome.FINISH_ERROR;
				}

                // if we have failed we notify a heuristic hazard to ensure that the
                // participant is placed in the heuristic list and the transaction is logged

                if (_failed) {
                    return TwoPhaseOutcome.HEURISTIC_HAZARD;
                }

                return TwoPhaseOutcome.FINISH_OK;
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (Exception ex6)
		{
			wscfLogger.arjLoggerI18N
					.warn(
							"com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_7",
							new Object[] { order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record is being driven through top-level prepare.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_8
	 *          [com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_8] -
	 *          ParticipantRecord.topLevelPrepare {0} caught: {1}
	 */

	public int topLevelPrepare ()
	{
		try
		{
            boolean result;
            // only complete if we have not exited
            if (!_exited) {
                result = complete();
            } else {
                result = false;
            }
            // if we have failed we return heuristic hazard so the participant is added to
            // the heuristic list and the transaction is logged

            if (_failed) {
                return TwoPhaseOutcome.HEURISTIC_HAZARD;
            } else if (_exited) {
                return TwoPhaseOutcome.PREPARE_READONLY;
            } else {
                return (result ? TwoPhaseOutcome.PREPARE_OK: TwoPhaseOutcome.PREPARE_NOTOK);
            }
        }
		catch (Exception e6)
		{
			wscfLogger.arjLoggerI18N
					.warn(
							"com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_8",
							new Object[] { order(), e6 });

			e6.printStackTrace();

			return TwoPhaseOutcome.PREPARE_OK;
		}
	}

	/**
	 * The record is being driven through nested commit and is the only
	 * resource.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_9
	 *          [com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_9] -
	 *          ParticipantRecord.nestedOnePhaseCommit {0} caught: {1}
	 */

	public int nestedOnePhaseCommit ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				return TwoPhaseOutcome.FINISH_ERROR;
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (Exception ex6)
		{
			wscfLogger.arjLoggerI18N
					.warn(
							"com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_9",
							new Object[] { order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record is being driven through top-level commit and is the only
	 * resource.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_10
	 *          [com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_10] -
	 *          ParticipantRecord.topLevelOnePhaseCommit {0} caught: {1}
	 */

	public int topLevelOnePhaseCommit ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				try
				{
                    if (!_exited) _resourceHandle.close();
				}
				catch (InvalidParticipantException ex)
				{
					return TwoPhaseOutcome.FINISH_ERROR;
				}
				catch (WrongStateException ex)
				{
                    // this indicates a failure to close so we notify a heuristic hazard
					return TwoPhaseOutcome.HEURISTIC_HAZARD;
				}
				catch (SystemException ex)
				{
                    // this indicates a comms failure so we return FINISH_ERROR which will place
                    // the participant in the failed list and cause a retry of the close
                    return TwoPhaseOutcome.FINISH_ERROR;
				}

                // if we have failed we notify a heuristic hazard to ensure that the
                // participant is placed in the heuristic list and the transaction is logged

                if (_failed) {
                    return TwoPhaseOutcome.HEURISTIC_HAZARD;
                }
                // if we closed or we exited then all is ok

                return TwoPhaseOutcome.FINISH_OK;
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (Exception ex6)
		{
			wscfLogger.arjLoggerI18N
					.warn(
							"com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_10",
							new Object[] { order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record generated a heuristic and can now forget about it.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_11
	 *          [com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_11] -
	 *          ParticipantRecord.forgetHeuristic for {0} called without a
	 *          resource!
	 * @message com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_12
	 *          [com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_12] -
	 *          ParticipantRecord.forgetHeuristic {0} caught exception: {1}
	 */

	public boolean forgetHeuristic ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				try
				{
					if (!_exited) _resourceHandle.forget();
				}
				catch (InvalidParticipantException ex)
				{
					return false;
				}
				catch (WrongStateException ex)
				{
					return false;
				}
				catch (SystemException ex)
				{
					return false;
				}

				return true;
			}
			else
			{
				wscfLogger.arjLoggerI18N
						.warn(
								"com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_11",
								new Object[] { order() });
			}
		}
		catch (Exception e)
		{
			wscfLogger.arjLoggerI18N
					.warn(
							"com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_12",
							new Object[] { order(), e });

			e.printStackTrace();
		}

		return false;
	}

    /**
	 * @message com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_13
	 *          [com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_13] -
	 *          ParticipantRecord.complete {0} caught: {1}
	 */

	public boolean complete ()
	{
		boolean result = false;

		try
		{
			if (_resourceHandle != null)
			{
				try
				{
					if (isActive())
					{
						if (_resourceHandle instanceof ParticipantWithComplete)
						{
							((ParticipantWithComplete) _resourceHandle)
									.complete();
							 completed();
						}

						result = true;
					}
					else
					{
						// already completed, so this is a null op. just rtn
						// true.
						result = true;
					}
				}
				catch (InvalidParticipantException ex)
				{
				}
				catch (WrongStateException ex)
				{
				}
				catch (SystemException ex)
				{
				}
			}
		}
		catch (Exception ex6)
		{
			wscfLogger.arjLoggerI18N
					.warn(
							"com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_13",
							new Object[] { order(), ex6 });

			ex6.printStackTrace();
		}

		return result;
	}

	public static AbstractRecord create ()
	{
		return new ParticipantRecord();
	}

	public void remove (AbstractRecord toDelete)
	{
		toDelete = null;
	}

	public void print (PrintWriter strm)
	{
		super.print(strm);

		strm.print("ParticipantRecord");
		strm.print(_resourceHandle);
	}

	/**
	 * @message com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_14
	 *          [com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_14] -
	 *          ParticipantRecord.restore_state: {1}
	 */

	public boolean restore_state (InputObjectState os, int t)
	{
		boolean result = super.restore_state(os, t);

		if (result)
		{
			try
			{
                String resourcehandleImplClassName = os.unpackString();
                Class clazz = ClassLoaderHelper.forName(ParticipantRecord.class, resourcehandleImplClassName);
                _resourceHandle = (RecoverableParticipant)clazz.newInstance();

                result = _resourceHandle.restore_state(os);

				if (result) {
					_timeout = os.unpackLong();
                    _exited = os.unpackBoolean();
                    if (_exited) {
                        _failed = os.unpackBoolean();
                    }
                }
			}
			catch (Exception ex)
			{
				wscfLogger.arjLoggerI18N
						.warn(
								"com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_14",
								new Object[] { ex });

				result = false;
			}
		}

		return result;
	}

	/**
	 * @message com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_15
	 *          [com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_15] -
	 *          ParticipantRecord.save_state: {1}
	 */

	public boolean save_state (OutputObjectState os, int t)
	{
		boolean result = super.save_state(os, t);

		if (result)
		{
			try
			{
                os.packString(_resourceHandle.getClass().getName()); // TODO: a shorter value whould be more efficient.
				result = _resourceHandle.save_state(os);
                if (result) {
					os.packLong(_timeout);
                    os.packBoolean(_exited);
                    if (_exited) {
                        os.packBoolean(_failed);
                    }
                }

                /*
				 * TODO: pack qualifiers and coord id.
				 */
			}
			catch (Exception ex)
			{
				wscfLogger.arjLoggerI18N
						.warn(
								"com.arjuna.mwlabs.wscf.model.sagas.coordinator.arjunacore.ParticipantRecord_15",
								new Object[] { ex });

				result = false;
			}
		}

		return result;
	}

    /**
     * called during recovery activation to propagate the coordinator to the underlying stubs and their protocol
     * engines allowing the engines to establish a back channel to the cooridnator for flow of remote participant
     * requests.
     */
    
    public void setRecoveryCoordinator(ACCoordinator coordinator)
    {
        try {
            // try to propagate setCoordinator to the resource

            RecoverableParticipant recoverableParticipant = (RecoverableParticipant) _resourceHandle;
            recoverableParticipant.setCoordinator(coordinator);
        } catch (ClassCastException e) {
            // ignore as this is obviously not an instance of RecoverableParticipant
            // it must be a participant registered via the local API which means there is
            // no BAParticipantManager instance around to drive messages to the coordinator
        }
    }

    public String type ()
	{
		return "/StateManager/AbstractRecord/WSCF/ArjunaCore/ParticipantRecord";
	}

	public boolean doSave ()
	{
		/*
		 * If the participant has exited without failure, then we don't need to save anything
		 * about it in the transaction log. If it has not exited or it has exited with failure
		 * we do need to log it
		 */

		return (!_exited || _failed);
	}

	public void merge (AbstractRecord a)
	{
	}

	public void alter (AbstractRecord a)
	{
	}

	public boolean shouldAdd (AbstractRecord a)
	{
		return false;
	}

	public boolean shouldAlter (AbstractRecord a)
	{
		return false;
	}

	public boolean shouldMerge (AbstractRecord a)
	{
		return false;
	}

	public boolean shouldReplace (AbstractRecord rec)
	{
		return false;
	}

    /**
     * record the fact that this participant has exited
     *
     * @param failed true if the exit was because of a failure i.e. the participant may be in an unclean state
     */
    public final void delist (boolean failed)
	{
		_exited = true;
        _failed = failed;
    }

    /**
     * record the fact that this participant has completed
     */
    public final synchronized void completed ()
    {
        _completed = true;
    }

    /**
     * is the participant is still able to be sent a complete request
     *
     * @caveat it is only appropriate to call this if this is a CoordinatorCompletion participant
     * @return true if the participant is still able to be sent a complete request otherwise false
     */
    public final synchronized boolean isActive ()
    {
        return !_completed && !_exited;
    }

    /**
     * is this a ParticipantCompletion participant
     * @return true if this is a ParticipantCompletion participant otherwise false
     */
    public final boolean isParticipantCompletion ()
    {
        // n.b. this is ok if _resourceHandle is null
        return !(_resourceHandle instanceof ParticipantWithComplete);
    }

	/*
	 * Protected constructor used by crash recovery.
	 */

	protected ParticipantRecord ()
	{
		super();

		_resourceHandle = null;
		_timeout = 0;
		_coordId = null;
	}

	private RecoverableParticipant _resourceHandle;

	private long _timeout;

	private CoordinatorIdImple _coordId;

    private boolean _exited = false;

    private boolean _failed = false;

	private boolean _completed = false;

}
