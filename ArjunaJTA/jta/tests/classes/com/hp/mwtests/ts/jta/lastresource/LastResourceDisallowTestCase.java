/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.lastresource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import org.junit.Test;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;

public class LastResourceDisallowTestCase
{
    @Test
    public void testDisallowed()
        throws SystemException, NotSupportedException, RollbackException
    {
        final LastOnePhaseResource firstResource = new LastOnePhaseResource() ;
        final LastOnePhaseResource secondResource = new LastOnePhaseResource() ;
        
        final TransactionManager tm = new TransactionManagerImple() ;
        tm.begin() ;
        try
        {
            final Transaction tx = tm.getTransaction() ;
            assertTrue("First resource enlisted", tx.enlistResource(firstResource)) ;
            assertFalse("Second resource enlisted", tx.enlistResource(secondResource)) ;
        }
        finally
        {
            tm.rollback() ;
        }
    }

    @Test
    public void testDisallowedWithCommit()
        throws SystemException, NotSupportedException, RollbackException
    {
        final LastOnePhaseResource firstResource = new LastOnePhaseResource() ;
        final LastOnePhaseResource secondResource = new LastOnePhaseResource() ;

        final TransactionManager tm = new TransactionManagerImple() ;

        tm.begin() ;

        final Transaction tx = tm.getTransaction() ;

        assertNotNull("transaction is not associated", tx);

        try
        {
            assertTrue("First resource enlisted", tx.enlistResource(firstResource)) ;
            assertFalse("Second resource enlisted", tx.enlistResource(secondResource)) ;

            // the second enlist failure should have marked the transaction rollback only
            assertEquals("transaction is not rollback only ", tx.getStatus(), Status.STATUS_MARKED_ROLLBACK);

            tm.commit();
            fail("transaction should have rolled back");
        } catch (RollbackException e) {
            Throwable cause = e.getCause();
            // the message should be the i18NLogger ARJUNA016154 message but don't assert it since the message can change
            // assertEquals(cause.getMessage(), jtaLogger.i18NLogger.warn_failed_to_enlist_one_phase_resource());
            assertNotNull("rollback exception should have had an initial cause", cause);
            assertTrue(cause.getMessage().startsWith("ARJUNA016154"));
        } catch (HeuristicRollbackException | HeuristicMixedException e) {
            fail("unexpected heuristic (should have rolled back): " + e.getMessage());
        } finally {
            assertEquals("transaction status should be rolled back", tx.getStatus(), Status.STATUS_ROLLEDBACK);
        }
    }
}