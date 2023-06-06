/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.lastresource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
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
}