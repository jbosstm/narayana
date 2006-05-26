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
 * Copyright (C) 2002, 2003, 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: VolatileTwoPhaseCommitParticipant.java,v 1.1.2.2 2004/08/09 12:34:24 nmcl Exp $
 */

package com.arjuna.mwlabs.wst.at.participants;

import com.arjuna.wst.Volatile2PCParticipant;

import com.arjuna.mw.wscf.model.twophase.common.*;
import com.arjuna.mw.wscf.model.twophase.participants.*;
import com.arjuna.mw.wscf.model.twophase.exceptions.*;

import com.arjuna.mw.wscf.exceptions.*;

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
						throw new SystemException();
				}
			}
			else
				throw new SystemException();
		}
		catch (SystemException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
			throw new SystemException(ex.toString());
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
				throw new SystemException(ex.toString());
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
			// catch (com.arjuna.mw.wst.exceptions.SystemException ex)
			catch (com.arjuna.wst.SystemException ex)
			{
				throw new SystemException(ex.toString());
			}
		}
		else
			throw new InvalidParticipantException();
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
			// catch (com.arjuna.mw.wst.exceptions.SystemException ex)
			catch (com.arjuna.wst.SystemException ex)
			{
				throw new SystemException(ex.toString());
			}
		}
		else
			throw new InvalidParticipantException();
	}

	private Volatile2PCParticipant _resource = null;

	private boolean _readonly = false;

}
