/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.jts.lastresource;

import static org.junit.Assert.assertTrue;

import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple;
import com.hp.mwtests.ts.jta.jts.JTSTestCase;

public class LastResourceAllowedTestCase extends JTSTestCase
{
    @Before
    public void setUp() throws Exception
    {
        arjPropertyManager.getCoreEnvironmentBean().setAllowMultipleLastResources(true);
        super.setUp() ;
    }
    
    @Test
    public void testAllowed()
        throws SystemException, NotSupportedException, RollbackException
    {
        final LastOnePhaseResource firstResource = new LastOnePhaseResource();
        final LastOnePhaseResource secondResource = new LastOnePhaseResource();
        final LastOnePhaseResource thirdResource = new LastOnePhaseResource();
        
        final TransactionManager tm = new TransactionManagerImple();
        tm.begin() ;
        try
        {
            final Transaction tx = tm.getTransaction();
            assertTrue("First resource enlisted", tx.enlistResource(firstResource));
            assertTrue("Second resource enlisted", tx.enlistResource(secondResource));
            assertTrue("Third resource enlisted", tx.enlistResource(thirdResource));
        }
        finally
        {
            tm.rollback();
        }
    }
}