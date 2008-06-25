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
 * $Id: BusinessAgreementWithParticipantCompletionImple.java,v 1.1.2.2 2004/08/09 12:34:26 nmcl Exp $
 */

package com.arjuna.mwlabs.wst.ba.participants;

// import com.arjuna.mw.wst.resources.BusinessAgreement;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wscf.exceptions.InvalidParticipantException;
import com.arjuna.mw.wscf.model.sagas.exceptions.CompensateFailedException;
import com.arjuna.mwlabs.wst.util.PersistableParticipantHelper;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;

// TODO crash recovery (for EVERYTHING!!)

// TODO re-architect!!

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: BusinessAgreementWithParticipantCompletionImple.java,v 1.1.2.2
 *          2004/08/09 12:34:26 nmcl Exp $
 * @since 1.0.
 */

public class BusinessAgreementWithParticipantCompletionImple implements
		com.arjuna.mw.wscf.model.sagas.participants.Participant
{
//
	public BusinessAgreementWithParticipantCompletionImple (
			BusinessAgreementWithParticipantCompletionParticipant resource,
			String id)
	{
		_resource = resource;
		_identifier = id;
		_baParticipantManager = new com.arjuna.mwlabs.wst.ba.remote.BAParticipantManagerImple(
				id);
	}

	public void close () throws InvalidParticipantException,
			WrongStateException, SystemException
	{
		try
		{
			if (_resource != null)
			{
				_resource.close();
			}
			else
				throw new InvalidParticipantException();
		}
		catch (com.arjuna.wst.WrongStateException ex)
		{
			throw new WrongStateException(ex.toString());
		}
		catch (com.arjuna.wst.SystemException ex)
		{
			throw new SystemException(ex.toString());
		}
	}

	public void cancel () throws InvalidParticipantException,
			WrongStateException, SystemException
	{
		try
		{
			if (_resource != null)
			{
				_resource.cancel();
			}
			else
				throw new InvalidParticipantException();
		}
		catch (com.arjuna.wst.WrongStateException ex)
		{
			throw new WrongStateException(ex.toString());
		}
		catch (com.arjuna.wst.SystemException ex)
		{
			throw new SystemException(ex.toString());
		}
	}

	public void compensate () throws CompensateFailedException,
			InvalidParticipantException, WrongStateException, SystemException
	{
		try
		{
			if (_resource != null)
			{
				_resource.compensate();
			}
			else
				throw new InvalidParticipantException();
		}
		catch (com.arjuna.wst.FaultedException ex)
		{
			throw new CompensateFailedException();
		}
		catch (com.arjuna.wst.WrongStateException ex)
		{
			throw new WrongStateException(ex.toString());
		}
		catch (com.arjuna.wst.SystemException ex)
		{
			throw new SystemException(ex.toString());
		}
	}

	/**
	 * @return the status value.
	 */

	public String status () throws SystemException
	{
		try
		{
			if (_resource != null)
			{
				return _resource.status();
			}
			else
				throw new SystemException("InvalidParticipant");
		}
		catch (com.arjuna.wst.SystemException ex)
		{
			throw new SystemException(ex.toString());
		}
	}

	/**
	 * Inform the participant that is can forget the heuristic result.
	 * 
	 * @exception InvalidParticipantException
	 *                Thrown if the participant identity is invalid.
	 * @exception WrongStateException
	 *                Thrown if the participant is in an invalid state.
	 * @exception SystemException
	 *                Thrown in the event of a general fault.
	 */

	public void forget () throws InvalidParticipantException,
			WrongStateException, SystemException
	{
		// not supported by the IBM protocol.
	}

	public void unknown () throws SystemException
	{
		/*
		 * If the transaction is unknown, then we assume it cancelled.
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
		return _identifier;
	}

	public final com.arjuna.wst.BAParticipantManager participantManager ()
	{
		return _baParticipantManager;
	}

	public boolean save_state (OutputObjectState os)
	{
        return PersistableParticipantHelper.save_state(os, _resource) ;
	}
	
	public boolean restore_state (InputObjectState is)
	{
        final Object resource = PersistableParticipantHelper.restore_state(is) ;
        if (resource != null)
        {
            _resource = (BusinessAgreementWithParticipantCompletionParticipant)resource ;
            return true ;
        }
        else
        {
            return false ;
        }
	}

	protected BusinessAgreementWithParticipantCompletionParticipant _resource;

	private String _identifier = null;

	private com.arjuna.wst.BAParticipantManager _baParticipantManager = null;

}
