/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss Inc.
 */
package com.arjuna.ats.internal.jta.transaction.arjunacore.jca;

import javax.transaction.*;
import javax.transaction.xa.Xid;

/**
 * Subordinate transactions are those designed to be driven by a foreign controller,
 * so they expose methods for driving each of the termination phases individually.
 */
public interface SubordinateTransaction extends Transaction
{
	/**
	 * Drive the subordinate transaction through the prepare phase. Any
	 * enlisted participants will also be prepared as a result.
	 *
	 * @return a TwoPhaseOutcome representing the result.
	 */
    public int doPrepare();
    
	/**
	 * Drive the subordinate transaction to commit. It must have previously
	 * been prepared.
	 *
	 * @throws IllegalStateException thrown if the transaction has not been prepared
	 * or is unknown.
	 * @throws HeuristicMixedException thrown if a heuristic mixed outcome occurs
	 * (where some participants committed whilst others rolled back).
	 * @throws HeuristicRollbackException thrown if the transaction rolled back.
	 * @throws SystemException thrown if some other error occurs.
	 */
    public void doCommit () throws IllegalStateException,
			HeuristicMixedException, HeuristicRollbackException,
			SystemException;
        
	/**
	 * Drive the subordinate transaction to roll back. It need not have been previously
	 * prepared.
	 *
	 * @throws IllegalStateException thrown if the transaction is not known by the
	 * system or has been previously terminated.
	 * @throws HeuristicMixedException thrown if a heuristic mixed outcome occurs
	 * (can only happen if the transaction was previously prepared and then only if
	 * some participants commit whilst others roll back).
	 * @throws HeuristicCommitException thrown if the transaction commits (can only
	 * happen if it was previously prepared).
	 * @throws SystemException thrown if any other error occurs.
	 */
    public void doRollback () throws IllegalStateException,
            HeuristicMixedException, HeuristicCommitException, SystemException;
    
	/**
	 * Drive the transaction to commit. It should not have been previously
	 * prepared and will be the only resource in the global transaction.
	 *
	 * @throws IllegalStateException if the transaction has already terminated
	 * @throws javax.transaction.HeuristicRollbackException thrown if the transaction
	 * rolls back.
	 */
    public void doOnePhaseCommit () throws IllegalStateException,
			HeuristicRollbackException, SystemException, RollbackException;
    
	/**
	 * Called to tell the transaction to forget any heuristics.
	 *
	 * @throws IllegalStateException thrown if the transaction cannot
	 * be found.
	 */
    public void doForget () throws IllegalStateException;

    public boolean activated();
    
    public void recover();
    
    public Xid baseXid();
}
