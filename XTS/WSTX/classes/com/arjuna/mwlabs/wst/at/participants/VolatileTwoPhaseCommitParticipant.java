/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mwlabs.wst.at.participants;

import com.arjuna.wst.Volatile2PCParticipant;

import com.arjuna.mw.wscf.model.twophase.common.*;
import com.arjuna.mw.wscf.model.twophase.participants.*;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mw.wscf.model.twophase.exceptions.*;

import com.arjuna.mw.wscf.exceptions.*;

import java.util.Arrays;
import java.util.List;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: VolatileTwoPhaseCommitParticipant.java,v 1.1.2.2 2004/08/09
 *          12:34:24 nmcl Exp $
 * @since 1.0.
 */

public class VolatileTwoPhaseCommitParticipant implements Synchronization
{

	public VolatileTwoPhaseCommitParticipant (Volatile2PCParticipant resource)
	{
		_resource = resource;
	}

	/**
	 * The transaction that the instance is enrolled with is about to commit.
	 * 
	 * @exception SystemException
	 *                Thrown if any error occurs. This will cause the
	 *                transaction to roll back.
	 */

	public void beforeCompletion () throws SystemException
	{
		try
		{
			if (_resource != null)
			{
				// com.arjuna.mw.wst.vote.Vote vt = _resource.prepare();
				com.arjuna.wst.Vote vt = _resource.prepare();

				if (vt instanceof com.arjuna.wst.ReadOnly)
					_readonly = true;
				else
				{
					if (vt instanceof com.arjuna.wst.Prepared)
					{
						// do nothing
					}
					else
					{
						List<Class<?>> expected = Arrays.asList(com.arjuna.wst.ReadOnly.class, com.arjuna.wst.Prepared.class);
						wstxLogger.i18NLogger.error_wst_at_participants_Volatile2PC_prepare_wrong_type(vt, _resource, expected);
						throw new SystemException("participant on before completion preparation resulted in wrong vote result " + vt);
					}
				}
			}
			else
			{
				wstxLogger.i18NLogger.error_wst_at_participants_Volatile2PC_prepare_is_null();
				throw new SystemException("participant to prepare is null");
			}
		}
		catch (SystemException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
			wstxLogger.i18NLogger.error_wst_at_participants_Volatile2PC_prepare(_resource, ex);
			SystemException se = new SystemException(ex.toString());
			se.addSuppressed(ex);
			throw se;
		}
	}

	/**
	 * The transaction that the instance is enrolled with has completed and the
	 * state in which is completed is passed as a parameter.
	 * 
	 * @param CompletionStatus
	 *            cs The state in which the transaction completed.
	 * 
	 * @exception SystemException
	 *                Thrown if any error occurs. This has no affect on the
	 *                outcome of the transaction.
	 */

	public void afterCompletion (int status) throws SystemException
	{
		if (!_readonly)
		{
			try
			{
				switch (status)
				{
				case CoordinationResult.CONFIRMED:
					confirm();
					break;
				default:
					cancel();
					break;
				}
			}
			catch (SystemException ex)
			{
				throw ex;
			}
			catch (Exception ex)
			{
				SystemException se = new SystemException(ex.toString());
				se.addSuppressed(ex);
				throw se;
			}
		}
	}

	private final void confirm () throws InvalidParticipantException,
			WrongStateException, HeuristicHazardException,
			HeuristicMixedException, HeuristicCancelException, SystemException
	{
		if (_resource != null)
		{
			try
			{
				_resource.commit();
			}
			// catch (com.arjuna.mw.wst.exceptions.WrongStateException ex)
			catch (com.arjuna.wst.WrongStateException ex)
			{
				WrongStateException wse = new WrongStateException(ex.toString());
				wse.addSuppressed(ex);
				throw wse;
			}
			/*
			 * catch (com.arjuna.mw.wst.exceptions.HeuristicHazardException ex) {
			 * throw new HeuristicHazardException(ex.toString()); } catch
			 * (com.arjuna.mw.wst.exceptions.HeuristicMixedException ex) { throw
			 * new HeuristicMixedException(ex.toString()); } catch
			 * (com.arjuna.mw.wst.exceptions.HeuristicRollbackException ex) {
			 * throw new HeuristicCancelException(ex.toString()); }
			 */
			// catch (com.arjuna.mw.wst.exceptions.SystemException ex)
			catch (com.arjuna.wst.SystemException ex)
			{
				SystemException se = new SystemException(ex.toString());
				se.addSuppressed(ex);
				throw se;
			}
		}
		else
		{
			wstxLogger.i18NLogger.error_wst_at_participants_Volatile2PC_confirm_is_null();
			throw new InvalidParticipantException("participant to confirm is null");
		}
	}

	private final void cancel () throws InvalidParticipantException,
			WrongStateException, HeuristicHazardException,
			HeuristicMixedException, HeuristicConfirmException, SystemException
	{
		if (_resource != null)
		{
			try
			{
				_resource.rollback();
			}
			// catch (com.arjuna.mw.wst.exceptions.WrongStateException ex)
			catch (com.arjuna.wst.WrongStateException ex)
			{
			    WrongStateException wse = new WrongStateException(ex.toString());
			    wse.addSuppressed(ex);
			    throw wse;
			}
			/*
			 * catch (com.arjuna.mw.wst.exceptions.HeuristicHazardException ex) {
			 * throw new HeuristicHazardException(ex.toString()); } catch
			 * (com.arjuna.mw.wst.exceptions.HeuristicMixedException ex) { throw
			 * new HeuristicMixedException(ex.toString()); } catch
			 * (com.arjuna.mw.wst.exceptions.HeuristicCommitException ex) {
			 * throw new HeuristicConfirmException(ex.toString()); }
			 */
			// catch (com.arjuna.mw.wst.exceptions.SystemException ex)
			catch (com.arjuna.wst.SystemException ex)
			{
			    SystemException se = new SystemException(ex.toString());
			    se.addSuppressed(ex);
			    throw se;
			}
		}
		else
		{
		    wstxLogger.i18NLogger.error_wst_at_participants_Volatile2PC_cancel_is_null();
			throw new InvalidParticipantException("praticipant to cancel is null");
		}
	}

	private Volatile2PCParticipant _resource = null;

	private boolean _readonly = false;

}