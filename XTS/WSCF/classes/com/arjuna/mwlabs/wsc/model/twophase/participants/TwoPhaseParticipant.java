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
 * $Id: TwoPhaseParticipant.java,v 1.6.4.1 2005/11/22 10:34:11 kconner Exp $
 */

package com.arjuna.mwlabs.wsc.model.twophase.participants;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wscf.exceptions.InvalidParticipantException;
import com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicCancelException;
import com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicConfirmException;
import com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicHazardException;
import com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicMixedException;
import com.arjuna.mw.wscf.model.twophase.participants.Participant;
import com.arjuna.mw.wscf.model.twophase.vote.Vote;
import com.arjuna.mw.wscf.model.twophase.vote.VoteConfirm;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TwoPhaseParticipant.java,v 1.6.4.1 2005/11/22 10:34:11 kconner Exp $
 * @since 1.0.
 * 
 * @message com.arjuna.mwlabs.wsc.model.twophase.participants.TwoPhaseParticipant_1
 *          [com.arjuna.mwlabs.wsc.model.twophase.participants.TwoPhaseParticipant_1] -
 *          {0}
 */

public class TwoPhaseParticipant implements Participant
{

	public TwoPhaseParticipant (EndpointReferenceType address, String id)
	{
		_address = address;
		_id = id;
	}

	public Vote prepare () throws InvalidParticipantException,
			WrongStateException, HeuristicHazardException,
			HeuristicMixedException, SystemException
	{
		wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.twophase.participants.TwoPhaseParticipant_1", new Object[]
		{ "TwoPhaseParticipant.prepare" });

		return new VoteConfirm();
	}

	public void confirm () throws InvalidParticipantException,
			WrongStateException, HeuristicHazardException,
			HeuristicMixedException, HeuristicCancelException, SystemException
	{
		wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.twophase.participants.TwoPhaseParticipant_1", new Object[]
		{ "TwoPhaseParticipant.confirm" });
	}

	public void cancel () throws InvalidParticipantException,
			WrongStateException, HeuristicHazardException,
			HeuristicMixedException, HeuristicConfirmException, SystemException
	{
		wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.twophase.participants.TwoPhaseParticipant_1", new Object[]
		{ "TwoPhaseParticipant.cancel" });
	}

	public void confirmOnePhase () throws InvalidParticipantException,
			WrongStateException, HeuristicHazardException,
			HeuristicMixedException, HeuristicCancelException, SystemException
	{
		wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.twophase.participants.TwoPhaseParticipant_1", new Object[]
		{ "TwoPhaseParticipant.confirmOnePhase" });
	}

	public void forget () throws InvalidParticipantException,
			WrongStateException, SystemException
	{
		wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.twophase.participants.TwoPhaseParticipant_1", new Object[]
		{ "TwoPhaseParticipant.forget" });
	}

	public String id () throws SystemException
	{
		return _id;
	}

	private EndpointReferenceType _address;

	private String _id;

}
