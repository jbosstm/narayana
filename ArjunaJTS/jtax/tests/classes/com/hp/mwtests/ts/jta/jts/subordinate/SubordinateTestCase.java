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
package com.hp.mwtests.ts.jta.jts.subordinate;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import org.junit.After;
import org.junit.Before;

import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.TransactionImple;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.hp.mwtests.ts.jta.subordinate.TestXAResource;

import org.junit.Test;

import jakarta.resource.spi.XATerminator;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * JTAX version of the Subordinate transaction tests.
 */
public class SubordinateTestCase extends com.hp.mwtests.ts.jta.subordinate.SubordinateTestCase
{
    // we mostly reuse the JTA version of the test class, but need to ensure correct config, orb init
    // and use of the appropriate tx impl class:
    
    private ORB orb ;
    private RootOA oa ;

    @Before
    public void setUp()
        throws Exception
    {
//        System.setProperty("com.arjuna.ats.jta.jtaTMImplementation", "com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple");
//        System.setProperty("com.arjuna.ats.jta.jtaUTImplementation", "com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple");
        
        orb = ORB.getInstance("test");
        oa = OA.getRootOA(orb);
        
        orb.initORB(new String[0], null);
        oa.initOA();

        ORBManager.setORB(orb);
        ORBManager.setPOA(oa);
    }

    @Override
    public void testFailOnCommitRetry() throws Exception {
        com.arjuna.ats.internal.jta.Implementationsx.initialise();
        super.testFailOnCommitRetry();
    }

    @After
    public void tearDown()
        throws Exception
    {
        if (oa != null)
        {
            oa.destroy();
        }
        if (orb != null)
        {
            orb.shutdown();
        }
//        System.clearProperty("com.arjuna.ats.jta.jtaTMImplementation");
//        System.clearProperty("com.arjuna.ats.jta.jtaUTImplementation");
    }

    @Test
    public void testPrepareRollback() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        assertEquals(TwoPhaseOutcome.PREPARE_READONLY, tm.doPrepare());
        try {
            tm.doRollback();
            fail("TransactionImple stub can't be sure why the transaction was committed it shouldn't massage");
        } catch (HeuristicMixedException e) {
            // TransactionImple stub can't be sure why the transaction was committed it shouldn't massage
            //  - this DOES NOT match doPhase2Abort in ServerTransaction which allows a massage
        }
    }

    @Override
    public SubordinateTransaction createTransaction() {
            return new TransactionImple(0); // implicit begin
    }

    /**
     * <p>
     * Behaviour of JTS is different to JTA. As data on participant is saved after each action run
     * for the participant in JTS. Object store data is store only after end
     * of the whole prepare phase in JTA.
     * </p>
     * <p>
     * RMFAIL means that recovery is expected to finish what was started.
     * </p>
     */
    @Override
    @Test
    public void testFailOnCommitRmFailTwoResourcesOnePhase () throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);

        final TestXAResource xaResource1 = new TestXAResource();
        final TestXAResource xaResource2 = new TestXAResource();
        xaResource2.setCommitException(new XAException(XAException.XAER_RMFAIL));

        t.enlistResource(xaResource1);
        t.enlistResource(xaResource2);

        final XATerminator xaTerminator = SubordinationManager.getXATerminator();

        xaTerminator.commit(xid, true);
        assertEquals(jakarta.transaction.Status.STATUS_COMMITTED, t.getStatus());
    }
}
