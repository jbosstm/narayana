/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mwlabs.wst.at.participants;

import com.arjuna.wst.Durable2PCParticipant;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.mw.wscf.model.twophase.participants.*;
import com.arjuna.mw.wscf.model.twophase.exceptions.*;
import com.arjuna.mw.wscf.model.twophase.vote.*;

import com.arjuna.mw.wscf.exceptions.*;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.SystemCommunicationException;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mwlabs.wst.util.PersistableParticipantHelper;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: DurableTwoPhaseCommitParticipant.java,v 1.1.2.2 2004/08/09
 *          12:34:24 nmcl Exp $
 * @since 1.0.
 */

public class DurableTwoPhaseCommitParticipant implements Participant
{
    // default ctor for crash recovery
    public DurableTwoPhaseCommitParticipant() {
    }

    public DurableTwoPhaseCommitParticipant (Durable2PCParticipant resource, String identifier)
	{
		_resource = resource;
		_id = identifier;
	}

    /**
     * tell the participant to prepare
     *
     * @return the participant's vote or a cancel vote the participant is null
     *
     */
    public Vote prepare () throws InvalidParticipantException,
			WrongStateException, HeuristicHazardException,
			HeuristicMixedException, SystemException
	{
		try
		{
			if (_resource != null)
			{
				com.arjuna.wst.Vote vt = _resource.prepare();

				if (vt instanceof com.arjuna.wst.ReadOnly)
				{
					_readonly = true;

					return new VoteReadOnly();
				}
				else
				{
					if (vt instanceof com.arjuna.wst.Prepared)
						return new VoteConfirm();
					else
					{
						_rolledback = true;

						return new VoteCancel();
					}
				}
			}
			else
				return new VoteCancel();
		}
		catch (com.arjuna.wst.WrongStateException ex)
		{
			wstxLogger.i18NLogger.error_wst_at_participants_Durable2PC_prepare(_id, _resource, ex);
			WrongStateException wse = new WrongStateException(ex.toString());
			wse.addSuppressed(ex);
			throw wse;
		}
		catch (com.arjuna.wst.stub.SystemCommunicationException ex) {
		    wstxLogger.i18NLogger.warn_mwlabs_wst_at_participants_DurableTwoPhaseCommitParticipant_prepare_1(_id, _resource);
		    SystemCommunicationException sce = new SystemCommunicationException(ex.toString());
		    sce.addSuppressed(ex);
		    throw sce;
		}
		catch (com.arjuna.wst.SystemException ex)
		{
		    wstxLogger.i18NLogger.error_wst_at_participants_Durable2PC_prepare(_id, _resource, ex);
		    SystemException se = new SystemException(ex.toString());
		    se.addSuppressed(ex);
		    throw se;
        }
	}

    /**
     * attempt to commit the participant
     *
     */
    public void confirm () throws InvalidParticipantException,
			WrongStateException, HeuristicHazardException,
			HeuristicMixedException, HeuristicCancelException, SystemException
	{
		if (_resource != null)
		{
			try
			{
				if (!_readonly)
				{
					_resource.commit();
				}
			}
			catch (com.arjuna.wst.WrongStateException ex)
			{
				wstxLogger.i18NLogger.error_wst_at_participants_Durable2PC_confirm(_id, _resource, ex);
				WrongStateException wse = new WrongStateException(ex.toString());
				wse.addSuppressed(wse);
				throw wse;
			}
			catch (com.arjuna.wst.stub.SystemCommunicationException ex) {
                // log an error here -- we will end up writing a heuristic transaction record too
                wstxLogger.i18NLogger.warn_mwlabs_wst_at_participants_DurableTwoPhaseCommitParticipant_confirm_1(_id, _resource);
                SystemCommunicationException sce = new SystemCommunicationException(ex.toString());
                sce.addSuppressed(ex);
                throw sce;
			}
			catch (com.arjuna.wst.SystemException ex)
			{
			    wstxLogger.i18NLogger.error_wst_at_participants_Durable2PC_confirm(_id, _resource, ex);
			    SystemException se = new SystemException(ex.toString());
			    se.addSuppressed(ex);
			    throw se;
            }
		}
		else
		{
			wstxLogger.i18NLogger.error_wst_at_participants_Durable2PC_confirm_is_null(_id);
			throw new InvalidParticipantException("participant " + _id + " to confirm is null");
		}
	}

    /**
     * attempt to cancel the participant
     *
     */
	public void cancel () throws InvalidParticipantException,
			WrongStateException, HeuristicHazardException,
			HeuristicMixedException, HeuristicConfirmException, SystemException
	{
		if (_resource != null)
		{
			try
			{
				if (!_rolledback)
					_resource.rollback();
			}
			catch (com.arjuna.wst.WrongStateException ex)
			{
				WrongStateException wse = new WrongStateException(ex.toString());
				wse.addSuppressed(ex);
				throw wse;
			}
			catch (com.arjuna.wst.stub.SystemCommunicationException ex) {
			    // log an error here -- if the participant is dead it will retry anyway
			    wstxLogger.i18NLogger.error_mwlabs_wst_at_participants_DurableTwoPhaseCommitParticipant_cancel_1(_id);
			    SystemCommunicationException sce = new SystemCommunicationException(ex.toString());
			    sce.addSuppressed(ex);
			    throw sce;
			}
			catch (com.arjuna.wst.SystemException ex)
			{
			    SystemException se = new SystemException(ex.toString());
			    se.addSuppressed(ex);
			    throw se;
			}
		}
		else
		{
			wstxLogger.i18NLogger.error_wst_at_participants_Durable2PC_cancel_is_null(_id);
			throw new InvalidParticipantException();
		}
	}

	// TODO mark ParticipantCancelledException explicitly?

    public void confirmOnePhase () throws InvalidParticipantException,
			WrongStateException, HeuristicHazardException,
			HeuristicMixedException, HeuristicCancelException, SystemException
	{
		if (_resource != null)
		{
			Vote v = null;

			try
			{
				v = prepare();
			}
            catch (Exception ex)
			{
                // either the prepare timed out or the participant was invalid or in an
                // invalid state
                
                wstxLogger.i18NLogger.warn_wst_at_participants_Durable2PC_commit_one_phase(_id, _resource, ex);

				v = new VoteCancel();
			}

                        if (v instanceof VoteReadOnly)
                        {
                            _readonly = true;
                        }
                        else if (v instanceof VoteCancel)
			{
				_rolledback = false;

                // TODO only do this if we didn't return VoteCancel

                try {
                    cancel();
                } catch (SystemCommunicationException sce) {
                    // if the rollback times out as well as the prepare we
                    // return an exception which indicates a failed transaction
                }
                wstxLogger.i18NLogger.warn_wst_at_participants_Durable2PC_canceled(_id, _resource);
                throw new ParticipantCancelledException();
			}
			else
			{
				if (v instanceof VoteConfirm)
				{
					try
					{
						confirm();
					}
					catch (HeuristicHazardException | HeuristicMixedException |  HeuristicCancelException ex)
					{
						wstxLogger.i18NLogger.error_wst_at_participants_Durable2PC_one_phase_failed(_id, _resource, ex);
						throw ex;
					}
					catch (Exception ex)
					{
						wstxLogger.i18NLogger.error_wst_at_participants_Durable2PC_one_phase_failed(_id, _resource, ex);
						HeuristicHazardException hhe = new HeuristicHazardException();
						hhe.addSuppressed(ex);
						throw ex;
					}
				}
				else
				{
					wstxLogger.i18NLogger.error_wst_at_participants_Durable2PC_one_phase_wrong_vote(_id, _resource, v.toString());
					cancel(); // TODO error

					throw new HeuristicHazardException();
				}
			}
		}
		else
		{
		    wstxLogger.i18NLogger.error_wst_at_participants_Durable2PC_confirm_one_phase_is_null(_id);
			throw new InvalidParticipantException();
		}
	}

	public void forget () throws InvalidParticipantException,
			WrongStateException, SystemException
	{
	}

	public void unknown () throws SystemException
	{
		/*
		 * If the transaction is unknown, then we assume it rolled back.
		 */

		try
		{
			cancel();
		}
		catch (Exception ex)
		{
			wstxLogger.i18NLogger.error_wst_at_participants_Durable2PC_unknown(_id, _resource, ex);
			// TODO
		}
	}

	public String id () throws SystemException
	{
		return _id;
	}

	public boolean save_state (OutputObjectState os)
	{
        return PersistableParticipantHelper.save_state(os, _resource) ;
	}

	public boolean restore_state (InputObjectState os)
	{
        _resource = (Durable2PCParticipant)PersistableParticipantHelper.restore_state(os) ;
        return true ;
	}

	private Durable2PCParticipant _resource;
	private String _id;
	private boolean _readonly;
	private boolean _rolledback;

}