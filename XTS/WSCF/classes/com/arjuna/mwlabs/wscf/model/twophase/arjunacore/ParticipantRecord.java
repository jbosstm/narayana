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
 * $Id: ParticipantRecord.java,v 1.11 2005/06/09 09:41:27 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.twophase.arjunacore;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.mw.wscf.model.twophase.vote.*;
import com.arjuna.mw.wscf.model.twophase.participants.Participant;
import com.arjuna.mw.wscf.model.twophase.exceptions.*;

import com.arjuna.mw.wsas.exceptions.*;

import com.arjuna.mw.wscf.exceptions.*;
import com.arjuna.webservices.util.ClassLoaderHelper;

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
	 */

	public ParticipantRecord (Participant theResource, Uid id)
	{
		super(id, null, ObjectType.ANDPERSISTENT);

		_resourceHandle = theResource;
		_timeout = 0;
		_coordId = new CoordinatorIdImple(id);

        if (theResource == null)
            wscfLogger.i18NLogger.warn_model_twophase_arjunacore_ParticipantRecord_1(order());
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

		return RecordType.XTS_WSAT_RECORD;
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
	 */

	public void setValue (Object o)
	{
        wscfLogger.i18NLogger.warn_model_twophase_arjunacore_ParticipantRecord_2();
	}

	/**
	 * The record is being driven through nested rollback.
	 *
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
		catch (Exception ex6) {
            wscfLogger.i18NLogger.warn_model_twophase_arjunacore_ParticipantRecord_3(order(), ex6);

            ex6.printStackTrace();

            return TwoPhaseOutcome.FINISH_ERROR;
        }
	}

	/**
	 * The record is being driven through nested commit.
	 *
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
		catch (Exception ex6) {
            wscfLogger.i18NLogger.warn_model_twophase_arjunacore_ParticipantRecord_4(order(), ex6);

            return TwoPhaseOutcome.FINISH_ERROR;
        }
	}

	/**
	 * The record is being driven through nested prepare.
	 *
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
		catch (Exception e6) {
            wscfLogger.i18NLogger.warn_model_twophase_arjunacore_ParticipantRecord_5(order(), e6);

            return TwoPhaseOutcome.HEURISTIC_HAZARD;
        }
	}

	/**
	 * The record is being driven through top-level rollback.
	 *
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
		catch (Exception ex6) {
            wscfLogger.i18NLogger.warn_model_twophase_arjunacore_ParticipantRecord_6(order(), ex6);

            return TwoPhaseOutcome.FINISH_ERROR;
        }
	}

	/**
	 * The record is being driven through top-level commit.
	 *
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
					return TwoPhaseOutcome.NOT_PREPARED; // should be HEURISTIC_HAZARD?
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
                catch(SystemCommunicationException ex)
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
		catch (Exception ex6) {
            wscfLogger.i18NLogger.warn_model_twophase_arjunacore_ParticipantRecord_7(order(), ex6);

            return TwoPhaseOutcome.FINISH_ERROR;
        }
	}

	/**
	 * The record is being driven through top-level prepare.
	 *
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
                catch(SystemCommunicationException ex)
                {
                    // if prepare timed out then we return error so it goes back on the
                    // prepare list and is rolled back
                    return TwoPhaseOutcome.FINISH_ERROR;
                }
				catch (SystemException ex)
				{
					return TwoPhaseOutcome.HEURISTIC_HAZARD;
				}
			}
			else
				return TwoPhaseOutcome.PREPARE_NOTOK;
		}
		catch (Exception e6) {
            wscfLogger.i18NLogger.warn_model_twophase_arjunacore_ParticipantRecord_8(order(), e6);

            return TwoPhaseOutcome.PREPARE_NOTOK;
        }
	}

	/**
	 * The record is being driven through nested commit and is the only
	 * resource.
	 *
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
		catch (Exception ex6) {
            wscfLogger.i18NLogger.warn_model_twophase_arjunacore_ParticipantRecord_9(order(), ex6);

            return TwoPhaseOutcome.FINISH_ERROR;
        }
	}

	/**
	 * The record is being driven through top-level commit and is the only
	 * resource.
	 *
	 */

	public int topLevelOnePhaseCommit ()
	{
		try
		{
			if (_resourceHandle != null)
			{
				if (_rolledback)
					return TwoPhaseOutcome.ONE_PHASE_ERROR;

				if (_readonly)
					return TwoPhaseOutcome.FINISH_OK;

				try
				{
					_resourceHandle.confirmOnePhase();
				}
				catch (InvalidParticipantException ex)
				{
					return TwoPhaseOutcome.ONE_PHASE_ERROR;
				}
				catch (WrongStateException ex)
				{
					return TwoPhaseOutcome.ONE_PHASE_ERROR;
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
					return TwoPhaseOutcome.ONE_PHASE_ERROR;
				}
				catch (SystemException ex)
				{
					return TwoPhaseOutcome.HEURISTIC_HAZARD;
				}

				return TwoPhaseOutcome.FINISH_OK;
			}
			else
				return TwoPhaseOutcome.ONE_PHASE_ERROR;
		}
		catch (Exception ex6) {
            wscfLogger.i18NLogger.warn_model_twophase_arjunacore_ParticipantRecord_10(order(), ex6);

            return TwoPhaseOutcome.ONE_PHASE_ERROR;
        }
	}

	/**
	 * The record generated a heuristic and can now forget about it.
	 *
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
			else {
                wscfLogger.i18NLogger.warn_model_twophase_arjunacore_ParticipantRecord_11(order());
            }
		}
		catch (Exception e) {
            wscfLogger.i18NLogger.warn_model_twophase_arjunacore_ParticipantRecord_12(order(), e);

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


	public boolean restore_state (InputObjectState os, int t)
	{
		boolean result = super.restore_state(os, t);

		if (result)
		{
			try
			{
                String resourcehandleImplClassName = os.unpackString();
                Class clazz = ClassLoaderHelper.forName(ParticipantRecord.class, resourcehandleImplClassName);
                _resourceHandle = (Participant)clazz.newInstance();

                result = _resourceHandle.restore_state(os);

				if (result)
					_timeout = os.unpackLong();

				/*
				 * TODO: unpack qualifiers and coord id.
				 */
			}
			catch (Exception ex) {
                wscfLogger.i18NLogger.warn_model_twophase_arjunacore_ParticipantRecord_13(ex);

                result = false;
            }
		}

		return result;
	}


	public boolean save_state (OutputObjectState os, int t)
	{
		boolean result = super.save_state(os, t);

		if (result)
		{
			try
			{
                os.packString(_resourceHandle.getClass().getName()); // TODO: a shorter value whould be more efficient.
                result = _resourceHandle.save_state(os);

				if (result)
					os.packLong(_timeout);

				/*
				 * TODO: pack qualifiers and coord id.
				 */
			}
			catch (Exception ex) {
                wscfLogger.i18NLogger.warn_model_twophase_arjunacore_ParticipantRecord_14(ex);

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

	public ParticipantRecord ()
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
