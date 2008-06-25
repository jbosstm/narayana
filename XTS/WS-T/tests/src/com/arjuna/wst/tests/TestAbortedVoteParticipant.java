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
 * Copyright (c) 2003, 2004,
 *
 * Arjuna Technologies Limited.
 *
 * $Id: TestAbortedVoteParticipant.java,v 1.1.2.1 2005/11/22 10:37:41 kconner Exp $
 */

package com.arjuna.wst.tests;

import com.arjuna.wst.Participant;
import com.arjuna.wst.Aborted;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;

public class TestAbortedVoteParticipant implements Participant
{
    public Vote prepare()
        throws WrongStateException, SystemException
    {
        return new Aborted();
    }

    public void commit()
        throws WrongStateException, SystemException
    {
        throw new SystemException();
    }

    public void rollback()
        throws WrongStateException, SystemException
    {
        throw new SystemException();
    }

    public void commitOnePhase()
        throws TransactionRolledBackException, WrongStateException, SystemException
    {
        throw new SystemException();
    }

    public void unknown()
        throws SystemException
    {
        throw new SystemException();
    }

    public void error()
        throws SystemException
    {
        throw new SystemException();
    }
}
