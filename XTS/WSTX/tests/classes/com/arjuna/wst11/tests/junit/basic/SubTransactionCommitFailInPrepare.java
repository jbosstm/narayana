/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008,
 * @author JBoss Inc.
 */

package com.arjuna.wst11.tests.junit.basic;

import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserSubTransaction;
import com.arjuna.mw.wst.TxContext;
import com.arjuna.wst.tests.DemoDurableParticipant;
import com.arjuna.wst.tests.DemoVolatileParticipant;
import com.arjuna.wst.tests.FailureParticipant;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.mwlabs.wst11.at.remote.UserSubTransactionImple;
import junit.framework.TestCase;

/**
 * @author Andrew Dinn
 * @version $Id:$
 * @since 1.0.
 */

public class SubTransactionCommitFailInPrepare extends TestCase
{

    public static void testSubTransactionCommitFailInPrepare()
            throws Exception
    {
        final UserTransaction ut = UserTransaction.getUserTransaction();
        final UserTransaction ust = UserSubTransaction.getUserTransaction();
        final TransactionManager tm = TransactionManager.getTransactionManager();

        final DemoDurableParticipant p1 = new DemoDurableParticipant();
        final DemoVolatileParticipant p2 = new DemoVolatileParticipant();
        final FailureParticipant p3 = new FailureParticipant(FailureParticipant.FAIL_IN_PREPARE, FailureParticipant.WRONG_STATE);
        final DemoVolatileParticipant p4 = new DemoVolatileParticipant();

        ut.begin();
        final TxContext tx = tm.suspend();
        tm.resume(tx);
        tm.enlistForDurableTwoPhase(p1, p1.identifier());
        tm.enlistForVolatileTwoPhase(p2, p2.identifier());
        ust.begin();
        final TxContext stx = tm.suspend();
        tm.resume(stx);
        tm.enlistForDurableTwoPhase(p3, "failure in prepare");
        tm.enlistForVolatileTwoPhase(p4, p4.identifier());

        tm.resume(tx);
        try {
        ut.commit();
            fail("expecting TransactionRolledBackException");
        } catch (TransactionRolledBackException wse) {
            // expect this
        }
        assertTrue(p1.prepared() && p1.resolved() && !p1.passed());
        assertTrue(p2.prepared() && p2.resolved() && !p2.passed());
        assertTrue(!p3.passed());
        assertTrue(p4.prepared() && p4.resolved() && !p4.passed());
    }
}