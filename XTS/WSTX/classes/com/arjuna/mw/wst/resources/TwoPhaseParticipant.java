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
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TwoPhaseParticipant.java,v 1.1 2003/02/03 11:01:27 nmcl Exp $
 */

package com.arjuna.mw.wst.resources;

import com.arjuna.mw.wst.vote.Vote;

import com.arjuna.mw.wst.exceptions.*;

/**
 * The TwoPhaseParticipant. As with all traditional TP implementations, the
 * one-phase commit optimisation is supported.
 */

public interface TwoPhaseParticipant
{

    public Vote prepare () throws WrongStateException, HeuristicHazardException, HeuristicMixedException, UnknownTransactionException, SystemException;

    public void commit () throws WrongStateException, HeuristicHazardException, HeuristicMixedException, HeuristicRollbackException, UnknownTransactionException, SystemException;

    public void rollback () throws WrongStateException, HeuristicHazardException, HeuristicMixedException, HeuristicCommitException, UnknownTransactionException, SystemException;

    public void commitOnePhase () throws WrongStateException, HeuristicHazardException, HeuristicMixedException, HeuristicRollbackException, UnknownTransactionException, SystemException;
    
}
