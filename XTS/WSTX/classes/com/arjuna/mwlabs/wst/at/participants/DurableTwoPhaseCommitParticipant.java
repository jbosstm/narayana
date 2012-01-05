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
 * Copyright (C) 2002, 2003, 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DurableTwoPhaseCommitParticipant.java,v 1.1.2.2 2004/08/09 12:34:24 nmcl Exp $
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
     * @return the participant's vote or a cancel vote the aprticipant is null
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
		//	catch (com.arjuna.mw.wst.exceptions.WrongStateException ex)
		catch (com.arjuna.wst.WrongStateException ex)
		{
			throw new WrongStateException(ex.toString());
		}
		/*
		 * catch (com.arjuna.mw.wst.exceptions.HeuristicHazardException ex {
		 * throw new HeuristicHazardException(ex.toString()); } catch
		 * (com.arjuna.mw.wst.exceptions.HeuristicMixedException ex) { throw new
		 * HeuristicMixedException(ex.toString()); }
		 */
		//	catch (com.arjuna.mw.wst.exceptions.SystemException ex)
		catch (com.arjuna.wst.SystemException ex)
		{
            if(ex instanceof com.arjuna.wst.stub.SystemCommunicationException) {
                // log an error here or else the participant may be left hanging
                // waiting for a prepare
                wstxLogger.i18NLogger.warn_mwlabs_wst_at_participants_DurableTwoPhaseCommitParticipant_prepare_1(_id);
                throw new SystemCommunicationException(ex.toString());
            } else {
			throw new SystemException(ex.toString());
            }
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
			//	    catch (com.arjuna.mw.wst.exceptions.WrongStateException ex)
			catch (com.arjuna.wst.WrongStateException ex)
			{
				throw new WrongStateException(ex.toString());
			}
			/*
			 * catch (com.arjuna.mw.wst.exceptions.HeuristicHazardException ex) {
			 * throw new HeuristicHazardException(ex.toString()); } catch
			 * (com.arjuna.mw.wst.exceptions.HeuristicMixedException ex) { throw
			 * new HeuristicMixedException(ex.toString()); } catch
			 * (com.arjuna.mw.wst.exceptions.HeuristicRollbackException ex) {
			 * throw new HeuristicCancelException(ex.toString()); }
			 */
			//	    catch (com.arjuna.mw.wst.exceptions.SystemException ex)
			catch (com.arjuna.wst.SystemException ex)
			{
				if(ex instanceof com.arjuna.wst.stub.SystemCommunicationException) {
                    // log an error here -- we will end up writing a heuristic transaction record too
                    wstxLogger.i18NLogger.warn_mwlabs_wst_at_participants_DurableTwoPhaseCommitParticipant_confirm_1(_id);
                    throw new SystemCommunicationException(ex.toString());
                } else {
                    throw new SystemException(ex.toString());
                }
            }
		}
		else
			throw new InvalidParticipantException();
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
			//	    catch (com.arjuna.mw.wst.exceptions.WrongStateException ex)
			catch (com.arjuna.wst.WrongStateException ex)
			{
				throw new WrongStateException(ex.toString());
			}
			/*
			 * catch (com.arjuna.mw.wst.exceptions.HeuristicHazardException ex) {
			 * throw new HeuristicHazardException(ex.toString()); } catch
			 * (com.arjuna.mw.wst.exceptions.HeuristicMixedException ex) { throw
			 * new HeuristicMixedException(ex.toString()); } catch
			 * (com.arjuna.mw.wst.exceptions.HeuristicCommitException ex) {
			 * throw new HeuristicConfirmException(ex.toString()); }
			 */
			//	    catch (com.arjuna.mw.wst.exceptions.SystemException ex)
			catch (com.arjuna.wst.SystemException ex)
			{
                if(ex instanceof com.arjuna.wst.stub.SystemCommunicationException) {
                    // log an error here -- if the participant is dead it will retry anyway
                    wstxLogger.i18NLogger.error_mwlabs_wst_at_participants_DurableTwoPhaseCommitParticipant_cancel_1(_id);
                    throw new SystemCommunicationException(ex.toString());
                } else {
                    throw new SystemException(ex.toString());
                }
			}
		}
		else
			throw new InvalidParticipantException();
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
                
                ex.printStackTrace();

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
					catch (HeuristicHazardException ex)
					{
						throw ex;
					}
					catch (HeuristicMixedException ex)
					{
						throw ex;
					}
					catch (HeuristicCancelException ex)
					{
						throw ex;
					}
					catch (Exception ex)
					{
						throw new HeuristicHazardException();
					}
				}
				else
				{
					cancel(); // TODO error

					throw new HeuristicHazardException();
				}
			}
		}
		else
			throw new InvalidParticipantException();
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
