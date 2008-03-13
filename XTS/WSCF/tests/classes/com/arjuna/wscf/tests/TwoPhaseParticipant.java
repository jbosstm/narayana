/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
 * $Id: TwoPhaseParticipant.java,v 1.3 2005/01/15 21:21:06 kconner Exp $
 */

package com.arjuna.wscf.tests;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
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

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TwoPhaseParticipant.java,v 1.3 2005/01/15 21:21:06 kconner Exp $
 * @since 1.0.
 */

public class TwoPhaseParticipant implements Participant
{
    public TwoPhaseParticipant (String id)
    {
	_id = id;
    }
    
    public Vote prepare () throws InvalidParticipantException, WrongStateException, HeuristicHazardException, HeuristicMixedException, SystemException
    {
	System.out.println("TwoPhaseParticipant.prepare");
	
	return new VoteConfirm();
    }

    public void confirm () throws InvalidParticipantException, WrongStateException, HeuristicHazardException, HeuristicMixedException, HeuristicCancelException, SystemException
    {
	System.out.println("TwoPhaseParticipant.confirm");
    }
    
    public void cancel () throws InvalidParticipantException, WrongStateException, HeuristicHazardException, HeuristicMixedException, HeuristicConfirmException, SystemException
    {
	System.out.println("TwoPhaseParticipant.cancel");
    }
    
    public void confirmOnePhase () throws InvalidParticipantException, WrongStateException, HeuristicHazardException, HeuristicMixedException, HeuristicCancelException, SystemException
    {
	System.out.println("TwoPhaseParticipant.confirmOnePhase");
    }
    
    public void forget () throws InvalidParticipantException, WrongStateException, SystemException
    {
	System.out.println("TwoPhaseParticipant.forget");
    }

    public String id () throws SystemException
    {
	return _id;
    }
    
    public boolean save_state(OutputObjectState os)
    {
        return false ;
    }
    
    public boolean restore_state(InputObjectState os)
    {
        return false ;
    }

    private String _id;
    
}
