/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.jts.twophase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import jakarta.transaction.NotSupportedException;
import jakarta.transaction.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionSynchronizationRegistryImple;
import com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

/**
 * JTS does not support read-only transactions: begin(true) must be refused
 * with NotSupportedException and isReadOnly() always reports false. These
 * assertions hold whichever version of jakarta.transaction-api is on the
 * classpath.
 */
public class ReadOnlyTransactionUnitTest
{
    @Test
    public void testUserTransactionReadOnlyBeginRefused () throws Exception
    {
        ThreadActionData.purgeActions();

        UserTransactionImple ut = new UserTransactionImple();

        assertFalse(ut.isReadOnly());

        try
        {
            ut.begin(true);

            fail("JTS read-only begin must throw NotSupportedException");
        }
        catch (final NotSupportedException ex)
        {
        }

        assertEquals(Status.STATUS_NO_TRANSACTION, ut.getStatus());
    }

    @Test
    public void testUserTransactionBeginFalseBehavesLikeBegin () throws Exception
    {
        ThreadActionData.purgeActions();

        UserTransactionImple ut = new UserTransactionImple();

        ut.begin(false);

        assertEquals(Status.STATUS_ACTIVE, ut.getStatus());
        assertFalse(ut.isReadOnly());

        ut.commit();

        assertEquals(Status.STATUS_NO_TRANSACTION, ut.getStatus());
    }

    @Test
    public void testTransactionManagerReadOnlyBeginRefused () throws Exception
    {
        ThreadActionData.purgeActions();

        TransactionManagerImple tmi = new TransactionManagerImple();

        try
        {
            tmi.begin(true);

            fail("JTS read-only begin must throw NotSupportedException");
        }
        catch (final NotSupportedException ex)
        {
        }

        assertNull(tmi.getTransaction());

        tmi.begin(false);

        assertFalse(((TransactionImple) tmi.getTransaction()).isReadOnly());

        tmi.rollback();
    }

    @Test
    public void testSynchronizationRegistryIsReadOnly () throws Exception
    {
        ThreadActionData.purgeActions();

        TransactionSynchronizationRegistryImple tsr = new TransactionSynchronizationRegistryImple();

        try
        {
            tsr.isReadOnly();

            fail("isReadOnly with no transaction must throw IllegalStateException");
        }
        catch (final IllegalStateException ex)
        {
        }

        TransactionManagerImple tmi = new TransactionManagerImple();

        tmi.begin();

        try
        {
            assertFalse(tsr.isReadOnly());
        }
        finally
        {
            tmi.rollback();
        }
    }

    @Test
    public void testCommitWithNoTransaction () throws Exception
    {
        ThreadActionData.purgeActions();

        UserTransactionImple ut = new UserTransactionImple();

        try
        {
            ut.commit();

            fail("commit with no transaction must throw IllegalStateException");
        }
        catch (final IllegalStateException ex)
        {
        }
    }

    @Before
    public void setUp () throws Exception
    {
        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);
    }

    @After
    public void tearDown () throws Exception
    {
        myOA.destroy();
        myORB.shutdown();
    }

    private ORB myORB = null;
    private RootOA myOA = null;
}
