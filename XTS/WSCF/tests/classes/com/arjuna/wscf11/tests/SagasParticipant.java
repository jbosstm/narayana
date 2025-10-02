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
 * $Id: TwoPhaseParticipant.java,v 1.3 2005/01/15 21:21:06 kconner Exp $
 */

package com.arjuna.wscf11.tests;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wscf.exceptions.InvalidParticipantException;
import com.arjuna.mw.wscf.model.sagas.exceptions.CompensateFailedException;
import com.arjuna.mw.wscf.model.sagas.participants.Participant;
import com.arjuna.mw.wscf.model.sagas.participants.ParticipantWithComplete;

import java.io.IOException;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TwoPhaseParticipant.java,v 1.3 2005/01/15 21:21:06 kconner Exp $
 * @since 1.0.
 */

public class SagasParticipant implements ParticipantWithComplete
{
    public SagasParticipant(String id)
    {
	_id = id;
    }

    public void close() throws InvalidParticipantException, WrongStateException, SystemException {
        System.out.println("SagasParticipant.close");
    }

    public void cancel () throws InvalidParticipantException, InvalidParticipantException, WrongStateException, SystemException
    {
        System.out.println("SagasParticipant.cancel");
    }

    public void compensate() throws CompensateFailedException, InvalidParticipantException, WrongStateException, SystemException
    {
        System.out.println("SagasParticipant.compensate");
    }

    public void forget() throws InvalidParticipantException, WrongStateException, SystemException {
        System.out.println("SagasParticipant.forget");
    }

    public void complete() throws InvalidParticipantException, WrongStateException, SystemException {
        System.out.println("SagasParticipant.complete");
    }
    public String id () throws SystemException
    {
	return _id;
    }

    public boolean save_state(OutputObjectState os)
    {
        try {
            os.packString(_id);
        } catch (IOException ioe) {
            return false;
        }
        return true ;
    }

    public boolean restore_state(InputObjectState os)
    {
        try {
            _id = os.unpackString();
        } catch (IOException e) {
            return false;
        }
        return true ;
    }

    private String _id;
}