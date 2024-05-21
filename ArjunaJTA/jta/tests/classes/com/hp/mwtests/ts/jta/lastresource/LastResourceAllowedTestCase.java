/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.lastresource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.arjuna.ats.arjuna.coordinator.TxStats;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class LastResourceAllowedTestCase {
    private XAResource xar;

    @Before
    public void setUp() throws Exception {
        arjPropertyManager.getCoreEnvironmentBean().setAllowMultipleLastResources(true);
        arjPropertyManager.getCoordinatorEnvironmentBean().setEnableStatistics(true);

        xar = new XAResource() {
            @Override
            public void commit(Xid xid, boolean b) throws XAException {
            }

            @Override
            public void end(Xid xid, int i) throws XAException {
            }

            @Override
            public void forget(Xid xid) throws XAException {
            }

            @Override
            public int getTransactionTimeout() throws XAException {
                return 0;
            }

            @Override
            public boolean isSameRM(XAResource xaResource) throws XAException {
                return false;
            }

            @Override
            public int prepare(Xid xid) throws XAException {
                return 0;
            }

            @Override
            public Xid[] recover(int i) throws XAException {
                return new Xid[0];
            }

            @Override
            public void rollback(Xid xid) throws XAException {
            }

            @Override
            public boolean setTransactionTimeout(int i) throws XAException {
                return false;
            }

            @Override
            public void start(Xid xid, int i) throws XAException {
            }
        };
    }

    @Test
    public void testAllowed()
        throws SystemException, NotSupportedException, RollbackException
    {
        final LastOnePhaseResource firstResource = new LastOnePhaseResource() ;
        final LastOnePhaseResource secondResource = new LastOnePhaseResource() ;
        final LastOnePhaseResource thirdResource = new LastOnePhaseResource() ;
        
        final TransactionManager tm = new TransactionManagerImple() ;
        tm.begin() ;
        try
        {
            final Transaction tx = tm.getTransaction() ;
            assertTrue("First resource enlisted", tx.enlistResource(firstResource)) ;
            assertTrue("Second resource enlisted", tx.enlistResource(secondResource)) ;
            assertTrue("Third resource enlisted", tx.enlistResource(thirdResource)) ;
        }
        finally
        {
            tm.rollback() ;
        }
    }

    @Test
    public void testHeuristicMixed() throws SystemException, NotSupportedException, RollbackException
    {
        // first last resource (LR) commits, second LR throws XA_HEURRB
        // then validate that the commit produces a HeuristicMixedException
        final LastOnePhaseResource firstResource = new LastOnePhaseResource(XAException.XA_HEURRB, 0);
        final LastOnePhaseResource secondResource = new LastOnePhaseResource(0, 0);

        // since both last resources will fail the expectation is rollback
        generateLastResourceFailures(true, HeuristicMixedException.class.getName(), xar, firstResource, secondResource);
    }

    @Test
    public void testRollback() throws SystemException, NotSupportedException, RollbackException
    {
        // both last resources throw XA_HEURRB
        // then validate that the commit produces a RollbackException
        final LastOnePhaseResource firstResource = new LastOnePhaseResource(XAException.XA_HEURRB, 0);
        final LastOnePhaseResource secondResource = new LastOnePhaseResource(XAException.XA_HEURRB, 0);

        // the prepare phase should fail on the first resource throws XA_HEURRB so the decision will switch to
        // rolling back the remaining resources and the overall outcome should be a RollbackException
        generateLastResourceFailures(true, RollbackException.class.getName(), xar, firstResource, secondResource);
    }

    @Test
    @Ignore // ignored for now because:
    // BasicAction.Abort turns off heuristic reporting, so we won't get the expected HeuristicMixedException
    // and the warning in the log does not include deferredThrowables (which would have helped)
    public void testHeuristicRollback() throws SystemException, NotSupportedException, RollbackException
    {
        // first LR rolls back ok, second LR throws XA_HEURCOM
        // then validate that the commit produces a HeuristicMixedException
        final LastOnePhaseResource firstResource = new LastOnePhaseResource(0, XAException.XA_HEURCOM);
        final LastOnePhaseResource secondResource = new LastOnePhaseResource(0, 0);

        // even though the test produces the correct heuristic there doesn't appear to be a way to report it on rollback
        generateLastResourceFailures(false, HeuristicMixedException.class.getName(), xar, firstResource, secondResource);
    }

    private void generateLastResourceFailures(boolean doCommit, String expectedExceptionClassName,
                                              XAResource xar, LastOnePhaseResource ... lastResources)
            throws SystemException, NotSupportedException, RollbackException
    {
        final TransactionManager tm = new TransactionManagerImple();
        long numberOfHeuristics = TxStats.getInstance().getNumberOfHeuristics();

        Transaction tx;

        tm.setTransactionTimeout(300); // useful for debugging
        tm.begin();

        try {
            tx = tm.getTransaction();

            for (int i = 0; i < lastResources.length; i++) {
                assertTrue("enlist resource " + i, tx.enlistResource(lastResources[i]));
            }

            if (xar != null) {
                assertTrue("XA resource enlisted", tx.enlistResource(xar));
            }
        } finally {
            try {
                if (doCommit) {
                    tm.commit();
                } else {
                    tm.rollback();
                }
                fail("commit or rollback should have thrown an exception of type " + expectedExceptionClassName);
            } catch (HeuristicMixedException e) {
                if (!expectedExceptionClassName.equals(e.getClass().getName())) {
                    fail("HeuristicMixedException unexpected");
                }
                // verify that the exception contains the correct suppressed XAException
                Throwable[] throwables = e.getSuppressed();
                assertNotEquals("expected a suppressed XA_HEURRB XAException", 0, throwables.length);
                Throwable t = throwables[0]; // the first one should be the resource exception
                assertEquals("Expected an XAException not " + t.getClass().getName(),
                        XAException.class, t.getClass());
                assertEquals("suppressed XAException should have been XA_HEURRB",
                        XAException.XA_HEURRB, ((XAException) t).errorCode);
                assertEquals("too few heuristics reported",
                        numberOfHeuristics + 1, TxStats.getInstance().getNumberOfHeuristics());
            } catch (HeuristicRollbackException e) {
                if (!expectedExceptionClassName.equals(e.getClass().getName())) {
                    fail("HeuristicRollbackException unexpected");
                }
            } catch (Exception e) {
                if (!expectedExceptionClassName.equals(e.getClass().getName())) {
                    fail("expected exception of type " +
                            expectedExceptionClassName +
                            " but got " +
                            e.getClass().getName());
                }
            }
        }
    }
}