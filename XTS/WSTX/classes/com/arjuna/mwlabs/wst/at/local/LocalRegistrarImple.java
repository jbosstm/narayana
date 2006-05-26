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
 * $Id: LocalRegistrarImple.java,v 1.8 2005/05/19 12:13:42 nmcl Exp $
 */

package com.arjuna.mwlabs.wst.at.local;

import com.arjuna.mw.wstx.logging.wstxLogger;

import com.arjuna.ats.arjuna.common.Uid;

import com.arjuna.mw.wscf.model.twophase.api.CoordinatorManager;

import com.arjuna.mwlabs.wst.at.participants.*;

import com.arjuna.wst.*;

import com.arjuna.mw.wst.common.Protocols;

import com.arjuna.wsc.AlreadyRegisteredException;
import com.arjuna.wsc.InvalidProtocolException;
import com.arjuna.wsc.InvalidStateException;
import com.arjuna.wsc.NoActivityException;

import com.arjuna.mw.wscf.model.twophase.CoordinatorManagerFactory;

/**
 * This class simulates the use of the real RegistrarImple, which we can't use
 * in a local environment simply because we do not have URIs to register: we
 * have real participants!
 */

public class LocalRegistrarImple
{

	public LocalRegistrarImple ()
	{
		try
		{
			_coordManager = CoordinatorManagerFactory.coordinatorManager();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Registers the interest of participant in a particular protocol.
	 * 
	 * @param participantProtocolServiceAddress
	 *            the address of the participant protocol service
	 * @param protocolIdentifier
	 *            the protocol identifier
	 * 
	 * @throws AlreadyRegisteredException
	 *             if the participant is already registered for this
	 *             coordination protocol under this activity identifier
	 * @throws InvalidProtocolException
	 *             if the coordination protocol is not supported
	 * @throws InvalidStateException
	 *             if the state of the coordinator no longer allows registration
	 *             for this coordination protocol
	 * @throws NoActivityException
	 *             if the activity does not exist.
	 * 
	 * @message com.arjuna.mwlabs.wst.at.local.LocalRegistrarImple_1
	 *          [com.arjuna.mwlabs.wst.at.local.LocalRegistrarImple_1] - Invalid
	 *          type URI: < {0} , {1} >
	 */

	public void register (Object participant, String protocolIdentifier)
			throws AlreadyRegisteredException, InvalidProtocolException,
			InvalidStateException, NoActivityException
	{
		// TODO check for AlreadyRegisteredException

		if (protocolIdentifier.equals(Protocols.DurableTwoPhaseCommit))
		{
			// enlist participant that wraps the requester URI.

			try
			{
				_coordManager.enlistParticipant(new DurableTwoPhaseCommitParticipant(
						(Durable2PCParticipant) participant,
						new Uid().toString()));
			}
			catch (Exception ex)
			{
				throw new InvalidStateException();
			}
		}
		else
		{
			if (protocolIdentifier.equals(Protocols.VolatileTwoPhaseCommit))
			{
				try
				{
					_coordManager.enlistSynchronization(new VolatileTwoPhaseCommitParticipant(
							(Volatile2PCParticipant) participant));
				}
				catch (Exception ex)
				{
					throw new InvalidStateException();
				}
			}
			else
			{
				wstxLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wst.at.local.LocalRegistrarImple_1", new Object[]
				{ Protocols.AtomicTransaction, protocolIdentifier });

				throw new InvalidProtocolException();
			}
		}
	}

	private CoordinatorManager _coordManager;

}
