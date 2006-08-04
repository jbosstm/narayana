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
 * $Id: ParticipantRecord.java,v 1.11 2005/06/09 09:41:27 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.twophase.arjunacore;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;

import com.arjuna.mw.wscf.model.twophase.vote.*;
import com.arjuna.mw.wscf.model.twophase.participants.Participant;
import com.arjuna.mw.wscf.model.twophase.exceptions.*;

import com.arjuna.mw.wsas.exceptions.*;

import com.arjuna.mw.wscf.exceptions.*;

import java.io.PrintWriter;

/**
 * Arjuna abstract record to handle two-phase participants.
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ParticipantRecord.java,v 1.11 2005/06/09 09:41:27 nmcl Exp $
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
	 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_1
	 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_1] -
	 *          ParticipantRecord {0} - null participant provided!
	 */

	public ParticipantRecord (Participant theResource, Uid id)
	{
		super(id, null, ObjectType.ANDPERSISTENT);

		_resourceHandle = theResource;
		_timeout = 0;
		_coordId = new CoordinatorIdImple(id);

		if (theResource == null)
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_1", new Object[]
			{ order() });
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
		// TODO add specific record type.
		
		return RecordType.USER_DEF_FIRST0;
	}

	/**
	 * The class name for this record.
	 */

	public ClassName className ()
	{
		return new ClassName("WSATParticipantRecord");
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
	 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_2
	 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_2] -
	 *          ParticipantRecord.setValue() called illegally.
	 */

	public void setValue (Object o)
	{
		wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_2");
	}

	/**
	 * The record is being driven through nested rollback.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_3
	 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_3] -
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
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_3", new Object[]
			{ order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record is being driven through nested commit.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_4
	 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_4] -
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
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_4", new Object[]
			{ order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record is being driven through nested prepare.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_5
	 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_5] -
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
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_5", new Object[]
			{ order(), e6 });

			e6.printStackTrace();

			return TwoPhaseOutcome.HEURISTIC_HAZARD;
		}
	}

	/**
	 * The record is being driven through top-level rollback.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_6
	 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_6] -
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
					if (!_rolledback)
						_resourceHandle.cancel();
				}
				catch (InvalidParticipantException ex)
				{
					return TwoPhaseOutcome.FINISH_ERROR;
				}
				catch (WrongStateException ex)
				{
					return TwoPhaseOutcome.FINISH_ERROR;
				}
				catch (HeuristicHazardException ex)
				{
					return TwoPhaseOutcome.HEURISTIC_HAZARD;
				}
				catch (HeuristicMixedException ex)
				{
					return TwoPhaseOutcome.HEURISTIC_MIXED;
				}
				catch (HeuristicConfirmException ex)
				{
					return TwoPhaseOutcome.HEURISTIC_COMMIT;
				}
				catch (SystemException ex)
				{
					return TwoPhaseOutcome.HEURISTIC_HAZARD;
				}

				return TwoPhaseOutcome.FINISH_OK;
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (Exception ex6)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_6", new Object[]
			{ order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record is being driven through top-level commit.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_7
	 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_7] -
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
					if (!_rolledback && !_readonly)
						_resourceHandle.confirm();

					if (_rolledback)
						throw new HeuristicHazardException();
				}
				catch (InvalidParticipantException ex)
				{
					return TwoPhaseOutcome.FINISH_ERROR;
				}
				catch (WrongStateException ex)
				{
					return TwoPhaseOutcome.NOT_PREPARED;
				}
				catch (HeuristicHazardException ex)
				{
					return TwoPhaseOutcome.HEURISTIC_HAZARD;
				}
				catch (HeuristicMixedException ex)
				{
					return TwoPhaseOutcome.HEURISTIC_MIXED;
				}
				catch (HeuristicCancelException ex)
				{
					return TwoPhaseOutcome.HEURISTIC_ROLLBACK;
				}
				catch (SystemException ex)
				{
					return TwoPhaseOutcome.HEURISTIC_HAZARD;
				}

				return TwoPhaseOutcome.FINISH_OK;
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (Exception ex6)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_7", new Object[]
			{ order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record is being driven through top-level prepare.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_8
	 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_8] -
	 *          ParticipantRecord.topLevelPrepare {0} caught: {1}
	 */

	public int topLevelPrepare ()
	{	
		try
		{
			if (_resourceHandle != null)
			{
				if (_rolledback)
					return TwoPhaseOutcome.PREPARE_NOTOK;

				if (_readonly)
					return TwoPhaseOutcome.PREPARE_READONLY;

				try
				{
					Vote res = _resourceHandle.prepare();

					if (res instanceof VoteConfirm)
					{
						return TwoPhaseOutcome.PREPARE_OK;
					}
					else
					{
						if (res instanceof VoteReadOnly)
						{
							_readonly = true;

							return TwoPhaseOutcome.PREPARE_READONLY;
						}
						else
						{
							_rolledback = true;

							return TwoPhaseOutcome.PREPARE_NOTOK;
						}
					}
				}
				catch (InvalidParticipantException ex)
				{
					return TwoPhaseOutcome.FINISH_ERROR;
				}
				catch (WrongStateException ex)
				{
					return TwoPhaseOutcome.FINISH_ERROR;
				}
				catch (HeuristicHazardException ex)
				{
					return TwoPhaseOutcome.HEURISTIC_HAZARD;
				}
				catch (HeuristicMixedException ex)
				{
					return TwoPhaseOutcome.HEURISTIC_MIXED;
				}
				catch (SystemException ex)
				{
					return TwoPhaseOutcome.HEURISTIC_HAZARD;
				}
			}
			else
				return TwoPhaseOutcome.PREPARE_NOTOK;
		}
		catch (Exception e6)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_8", new Object[]
			{ order(), e6 });

			e6.printStackTrace();

			return TwoPhaseOutcome.PREPARE_NOTOK;
		}
	}

	/**
	 * The record is being driven through nested commit and is the only
	 * resource.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_9
	 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_9] -
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
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_9", new Object[]
			{ order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record is being driven through top-level commit and is the only
	 * resource.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_10
	 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_10] -
	 *          ParticipantRecord.topLevelOnePhaseCommit {0} caught: {1}
	 */

	public int topLevelOnePhaseCommit ()
	{	
		try
		{		
			if (_resourceHandle != null)
			{			
				if (_rolledback)
					return TwoPhaseOutcome.FINISH_ERROR;

				if (_readonly)
					return TwoPhaseOutcome.FINISH_OK;

				try
				{
					_resourceHandle.confirmOnePhase();
				}
				catch (InvalidParticipantException ex)
				{
					return TwoPhaseOutcome.FINISH_ERROR;
				}
				catch (WrongStateException ex)
				{
					return TwoPhaseOutcome.FINISH_ERROR;
				}
				catch (HeuristicHazardException ex)
				{
					return TwoPhaseOutcome.HEURISTIC_HAZARD;
				}
				catch (HeuristicMixedException ex)
				{					
					return TwoPhaseOutcome.HEURISTIC_MIXED;
				}
				catch (HeuristicCancelException ex)
				{
					return TwoPhaseOutcome.HEURISTIC_ROLLBACK;
				}
				// TODO explicit in the signature
				catch (ParticipantCancelledException ex)  // a type of SystemException
				{
					return TwoPhaseOutcome.FINISH_ERROR;
				}
				catch (SystemException ex)
				{
					return TwoPhaseOutcome.HEURISTIC_HAZARD;
				}

				return TwoPhaseOutcome.FINISH_OK;
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (Exception ex6)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_10", new Object[]
			{ order(), ex6 });

			ex6.printStackTrace();

			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/**
	 * The record generated a heuristic and can now forget about it.
	 * 
	 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_11
	 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_11] -
	 *          ParticipantRecord.forgetHeuristic for {0} called without a
	 *          resource!
	 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_12
	 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_12] -
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
					_resourceHandle.forget();
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
				wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_11", new Object[]
				{ order() });
			}
		}
		catch (Exception e)
		{
			wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_12", new Object[]
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
	 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_13
	 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_13] -
	 *          ParticipantRecord.restore_state: {0}
	 */

	public boolean restore_state (InputObjectState os, int t)
	{
		boolean result = super.restore_state(os, t);

		if (result)
		{
			try
			{
				result = _resourceHandle.restore_state(os);
				
				if (result)
					_timeout = os.unpackLong();

				/*
				 * TODO: unpack qualifiers and coord id.
				 */
			}
			catch (Exception ex)
			{
				wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_13", new Object[]
				{ ex });

				result = false;
			}
		}

		return result;
	}

	/**
	 * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_14
	 *          [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_14] -
	 *          ParticipantRecord.save_state: {0}
	 */

	public boolean save_state (OutputObjectState os, int t)
	{
		boolean result = super.save_state(os, t);

		if (result)
		{
			try
			{
				result = _resourceHandle.save_state(os);
				
				if (result)
					os.packLong(_timeout);

				/*
				 * TODO: pack qualifiers and coord id.
				 */
			}
			catch (Exception ex)
			{
				wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_14", new Object[]
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

	public final void rolledback ()
	{
		_rolledback = true;
	}

	public final void readonly ()
	{
		_readonly = true;
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

	private Participant _resourceHandle;

	private long _timeout;

	private CoordinatorIdImple _coordId;

	private boolean _rolledback = false;

	private boolean _readonly = false;

}
