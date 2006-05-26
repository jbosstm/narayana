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
 * $Id: ParticipantRecord.java,v 1.5 2005/05/19 12:13:33 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;

import com.arjuna.mw.wscf.model.as.coordinator.*;

import com.arjuna.mw.wscf.model.as.coordinator.twophase.outcomes.*;
import com.arjuna.mw.wscf.model.as.coordinator.twophase.messages.*;
import com.arjuna.mw.wscf.model.as.coordinator.Participant;

import com.arjuna.mw.wscf.model.twophase.common.TwoPhaseResult;
import com.arjuna.mw.wscf.model.twophase.outcomes.*;

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wscf.common.Qualifier;

import com.arjuna.mw.wsas.exceptions.*;

import java.io.PrintWriter;

/**
 * Arjuna abstract record to handle two-phase participants.
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ParticipantRecord.java,v 1.5 2005/05/19 12:13:33 nmcl Exp $
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
	 * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_1
	 *          [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_1] -
	 *          ParticipantRecord {0} - null participant provided!
	 */

	public ParticipantRecord (Participant theResource, Uid id, int priority, Qualifier[] quals)
	{
		super(id, null, ObjectType.ANDPERSISTENT);

		_resourceHandle = new ParticipantWrapper(theResource);
		_timeout = 0;
		_priority = priority;
		_quals = quals;
		_coordId = new CoordinatorIdImple(id);
		_state = ActionStatus.RUNNING;

		if (theResource == null)
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_1", new Object[]
			{ order() });
	}

	public void finalize () throws Throwable
	{
		_resourceHandle = null;

		super.finalize();
	}

	public final void setResponse (Message notification, Outcome response)
			throws WrongStateException
	{
		boolean ok = false;

		if ((notification instanceof TopLevelPrepare)
				|| (notification instanceof NestedPrepare))
		{
			if (((_state == ActionStatus.PREPARING) && (response instanceof Vote))
					|| (_state == ActionStatus.RUNNING))
			{
				ok = true;
			}
		}

		if ((notification instanceof TopLevelConfirm)
				|| (notification instanceof NestedConfirm))
		{
			if (_state == ActionStatus.COMMITTING)
				ok = true;
		}

		if ((notification instanceof TopLevelCancel)
				|| (notification instanceof NestedCancel))
		{
			if ((_state == ActionStatus.ABORTING)
					|| (_state == ActionStatus.RUNNING))
			{
				ok = true;
			}
		}

		if (ok)
			_resourceHandle.setResponse(notification, response);
		else
			throw new WrongStateException();
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
		return _priority;
	}

	/**
	 * The class name for this record.
	 */

	public ClassName className ()
	{
		return new ClassName(toString());
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
	 * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_2
	 *          [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_2] -
	 *          ParticipantRecord.setValue() called illegally.
	 */

	public void setValue (Object o)
	{
		wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_2");
	}

	/**
	 * The record is being driven through nested rollback.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_3
	 *          [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_3] -
	 *          ParticipantRecord.nestedAbort {0} caught: {1}
	 */

	public int nestedAbort ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				_state = ActionStatus.ABORTING;

				Outcome res = _resourceHandle.processMessage(new NestedCancel(
						_coordId));

				_state = ActionStatus.ABORTED;

				if (res != null)
				{
					if (res instanceof CoordinationOutcome)
					{
						return convertResult(((CoordinationOutcome) res).result());
					}
					else
						return TwoPhaseOutcome.FINISH_ERROR;
				}

				return TwoPhaseOutcome.FINISH_OK;
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (SystemException ex1)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_3", new Object[]
			{ order(), ex1 });

			return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (Exception ex6)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_3", new Object[]
			{ order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record is being driven through nested commit.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_4
	 *          [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_4] -
	 *          ParticipantRecord.nestedCommit {0} caught: {1}
	 */

	public int nestedCommit ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				_state = ActionStatus.COMMITTING;

				Outcome res = _resourceHandle.processMessage(new NestedConfirm(
						_coordId));

				if (res != null)
				{
					if (res instanceof CoordinationOutcome)
					{
						return convertResult(((CoordinationOutcome) res).result());
					}
					else
						return TwoPhaseOutcome.FINISH_ERROR;
				}

				return TwoPhaseOutcome.FINISH_OK;
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (SystemException ex1)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_4", new Object[]
			{ order(), ex1 });

			return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (Exception ex6)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_4", new Object[]
			{ order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record is being driven through nested prepare.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_5
	 *          [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_5] -
	 *          ParticipantRecord.nestedPrepare {0} caught: {1}
	 */

	public int nestedPrepare ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				_state = ActionStatus.PREPARING;

				Outcome res = _resourceHandle.processMessage(new NestedPrepare(
						_coordId));

				if (res != null)
				{
					if (res instanceof VoteConfirm)
					{
						return TwoPhaseOutcome.PREPARE_OK;
					}
					else
					{
						if (res instanceof VoteReadOnly)
						{
							return TwoPhaseOutcome.PREPARE_READONLY;
						}
						else
						{
							return TwoPhaseOutcome.PREPARE_NOTOK;
						}
					}
				}
				else
					return TwoPhaseOutcome.FINISH_ERROR;
			}
			else
				return TwoPhaseOutcome.PREPARE_NOTOK;
		}
		catch (SystemException e1)
		{
			/*
			 * Assume that this exception is thrown to indicate a communication
			 * failure or some other system-like exception. In which case, crash
			 * recovery should try to recover for us.
			 */

			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_5", new Object[]
			{ order(), e1 });

			return TwoPhaseOutcome.PREPARE_NOTOK;
		}
		catch (Exception e6)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_5", new Object[]
			{ order(), e6 });

			e6.printStackTrace();

			return TwoPhaseOutcome.HEURISTIC_HAZARD;
		}
	}

	/**
	 * The record is being driven through top-level rollback.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_6
	 *          [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_6] -
	 *          ParticipantRecord.topLevelAbort {0} caught: {1}
	 */

	public int topLevelAbort ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				_state = ActionStatus.ABORTING;

				Outcome res = _resourceHandle.processMessage(new TopLevelCancel(
						_coordId));

				if (res != null)
				{
					if (res instanceof CoordinationOutcome)
					{
						return convertResult(((CoordinationOutcome) res).result());
					}
					else
						return TwoPhaseOutcome.FINISH_ERROR;
				}

				return TwoPhaseOutcome.FINISH_OK;
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (SystemException ex1)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_6", new Object[]
			{ order(), ex1 });

			return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (Exception ex6)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_6", new Object[]
			{ order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record is being driven through top-level commit.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_7
	 *          [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_7] -
	 *          ParticipantRecord.topLevelCommit {0} caught: {1}
	 */

	public int topLevelCommit ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				_state = ActionStatus.COMMITTING;

				Outcome res = _resourceHandle.processMessage(new TopLevelConfirm(
						_coordId));

				if (res != null)
				{
					if (res instanceof CoordinationOutcome)
					{
						return convertResult(((CoordinationOutcome) res).result());
					}
					else
						return TwoPhaseOutcome.FINISH_ERROR;
				}

				return TwoPhaseOutcome.FINISH_OK;
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (SystemException ex1)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_7", new Object[]
			{ order(), ex1 });

			return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (Exception ex6)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_7", new Object[]
			{ order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record is being driven through top-level prepare.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_8
	 *          [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_8] -
	 *          ParticipantRecord.topLevelPrepare {0} caught: {1}
	 */

	public int topLevelPrepare ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				_state = ActionStatus.PREPARING;

				Outcome res = _resourceHandle.processMessage(new TopLevelPrepare(
						_coordId));

				if (res != null)
				{
					if (res instanceof VoteConfirm)
					{
						return TwoPhaseOutcome.PREPARE_OK;
					}
					else
					{
						if (res instanceof VoteReadOnly)
						{
							return TwoPhaseOutcome.PREPARE_READONLY;
						}
						else
						{
							return TwoPhaseOutcome.PREPARE_NOTOK;
						}
					}
				}
				else
					return TwoPhaseOutcome.FINISH_ERROR;
			}
			else
				return TwoPhaseOutcome.PREPARE_NOTOK;
		}
		catch (SystemException e1)
		{
			/*
			 * Assume that this exception is thrown to indicate a communication
			 * failure or some other system-like exception. In which case, crash
			 * recovery should try to recover for us.
			 */

			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_8", new Object[]
			{ order(), e1 });

			return TwoPhaseOutcome.PREPARE_NOTOK;
		}
		catch (Exception e6)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_8", new Object[]
			{ order(), e6 });

			e6.printStackTrace();

			return TwoPhaseOutcome.HEURISTIC_HAZARD;
		}
	}

	/**
	 * The record is being driven through nested commit and is the only
	 * resource.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_9
	 *          [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_9] -
	 *          ParticipantRecord.nestedOnePhaseCommit {0} caught: {1}
	 */

	public int nestedOnePhaseCommit ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				_state = ActionStatus.COMMITTING;

				Outcome res = _resourceHandle.processMessage(new NestedOnePhaseCommit(
						_coordId));

				if (res != null)
				{
					if (res instanceof CoordinationOutcome)
					{
						return convertResult(((CoordinationOutcome) res).result());
					}
					else
						return TwoPhaseOutcome.FINISH_ERROR;
				}

				return TwoPhaseOutcome.FINISH_OK;
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (SystemException ex1)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_9", new Object[]
			{ order(), ex1 });

			return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (Exception ex6)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_9", new Object[]
			{ order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record is being driven through top-level commit and is the only
	 * resource.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_10
	 *          [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_10] -
	 *          ParticipantRecord.topLevelOnePhaseCommit {0} caught: {1}
	 */

	public int topLevelOnePhaseCommit ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				_state = ActionStatus.COMMITTING;

				Outcome res = _resourceHandle.processMessage(new TopLevelOnePhaseCommit(
						_coordId));

				if (res != null)
				{
					if (res instanceof CoordinationOutcome)
					{
						return convertResult(((CoordinationOutcome) res).result());
					}
					else
						return TwoPhaseOutcome.FINISH_ERROR;
				}

				return TwoPhaseOutcome.FINISH_OK;
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (SystemException ex1)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_10", new Object[]
			{ order(), ex1 });

			return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (Exception ex6)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_10", new Object[]
			{ order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record generated a heuristic and can now forget about it.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_11
	 *          [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_11] -
	 *          ParticipantRecord.forgetHeuristic for {0} called without a
	 *          resource!
	 * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_12
	 *          [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_12] -
	 *          ParticipantRecord.forgetHeuristic {0} caught exception: {1}
	 */

	public boolean forgetHeuristic ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				// _state = ActionStatus.COMMITTING;

				Outcome res = _resourceHandle.processMessage(new ForgetHeuristic(
						_coordId));

				if (res != null)
				{
					if (res instanceof CoordinationOutcome)
					{
						return true;
					}
					else
						return false;
				}
				else
					return true;
			}
			else
			{
				wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_11", new Object[]
				{ order() });
			}
		}
		catch (Exception e)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_12", new Object[]
			{ order(), e });

			e.printStackTrace();
		}

		return false;
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
	 * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_13
	 *          [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_13] -
	 *          ParticipantRecord.restore_state:
	 */

	public boolean restore_state (InputObjectState os, int t)
	{
		boolean result = super.restore_state(os, t);

		if (result)
		{
			try
			{
				result = _resourceHandle.unpackState(os);

				_priority = os.unpackInt();
				_timeout = os.unpackLong();

				/*
				 * TODO: unpack qualifiers and coord id.
				 */
			}
			catch (Exception ex)
			{
				wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_13", new Object[]
				{ ex });

				result = false;
			}
		}

		return result;
	}

	/**
	 * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_14
	 *          [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_14] -
	 *          ParticipantRecord.save_state:
	 */

	public boolean save_state (OutputObjectState os, int t)
	{
		boolean result = super.save_state(os, t);

		if (result)
		{
			try
			{
				result = _resourceHandle.packState(os);

				os.packInt(_priority);
				os.packLong(_timeout);

				/*
				 * TODO: pack qualifiers and coord id.
				 */
			}
			catch (Exception ex)
			{
				wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.ParticipantRecord_14", new Object[]
				{ ex });

				result = false;
			}
		}

		return result;
	}

	public String type ()
	{
		return "/StateManager/AbstractRecord/WSCF/ArjunaCore/ParticipantRecord";
	}

	public boolean doSave ()
	{
		return true;
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

	/*
	 * Protected constructor used by crash recovery.
	 */

	protected ParticipantRecord ()
	{
		super();

		_resourceHandle = null;
		_timeout = 0;
		_priority = 0;
		_quals = null;
		_coordId = null;
		_state = ActionStatus.RUNNING;
	}

	private final int convertResult (int result)
	{
		switch (result)
		{
		case TwoPhaseResult.PREPARE_OK:
			return TwoPhaseOutcome.PREPARE_OK;
		case TwoPhaseResult.PREPARE_NOTOK:
			return TwoPhaseOutcome.PREPARE_NOTOK;
		case TwoPhaseResult.PREPARE_READONLY:
			return TwoPhaseOutcome.PREPARE_READONLY;
		case TwoPhaseResult.HEURISTIC_CANCEL:
			return TwoPhaseOutcome.HEURISTIC_ROLLBACK;
		case TwoPhaseResult.HEURISTIC_CONFIRM:
			return TwoPhaseOutcome.HEURISTIC_COMMIT;
		case TwoPhaseResult.HEURISTIC_MIXED:
			return TwoPhaseOutcome.HEURISTIC_MIXED;
		case TwoPhaseResult.HEURISTIC_HAZARD:
			return TwoPhaseOutcome.HEURISTIC_HAZARD;
		case TwoPhaseResult.FINISH_OK:
		case TwoPhaseResult.CANCELLED:
		case TwoPhaseResult.CONFIRMED:
			return TwoPhaseOutcome.FINISH_OK;
		case TwoPhaseResult.NOT_PREPARED:
			return TwoPhaseOutcome.NOT_PREPARED;
		case TwoPhaseResult.FINISH_ERROR:
		default:
			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	private ParticipantWrapper _resourceHandle;
	private long _timeout;
	private int _priority;
	private Qualifier[] _quals;
	private CoordinatorIdImple _coordId;
	private int _state;

}
