/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;

import jakarta.transaction.NotSupportedException;
import jakarta.transaction.Status;
import jakarta.transaction.Transaction;
import jakarta.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import com.arjuna.ats.internal.jta.utils.ReadOnlyTransactionSupport;

/**
 * ReadOnlyTransactionSupport reflects on the jakarta.transaction interfaces,
 * so its behaviour depends on the API version on the classpath: with the
 * read-only API (jakarta.transaction-api 2.0.2+) it reports the real flags
 * and can begin read-only transactions; without it every query degrades to
 * false and a read-only begin is refused with NotSupportedException.
 *
 * Each test detects the API level at runtime and asserts the matching
 * contract, so the suite stays green both before and after the API bump.
 */
public class ReadOnlyTransactionSupportTest
{
    /** True when the read-only API (2.0.2+) is on the classpath. */
    private static final boolean READ_ONLY_API = hasMethod(Transactional.class, "isReadOnly");

    private TransactionManagerImple tm;

    @Before
    public void setUp ()
    {
        ThreadActionData.purgeActions();

        tm = (TransactionManagerImple) com.arjuna.ats.jta.TransactionManager.transactionManager();
    }

    @Test
    public void testIsReadOnlyTransactional ()
    {
        // the literal's isReadOnly() returns true, but the shim only sees it
        // when the Transactional interface itself declares the method
        assertEquals(READ_ONLY_API, ReadOnlyTransactionSupport.isReadOnly(new ReadOnlyTransactionalLiteral()));
    }

    @Test
    public void testIsReadOnlyTransaction () throws Exception
    {
        TransactionImple tx = new TransactionImple(0, true);

        try
        {
            assertTrue(tx.isReadOnly());
            assertEquals(READ_ONLY_API, ReadOnlyTransactionSupport.isReadOnly((Transaction) tx));
        }
        finally
        {
            tx.rollback();

            // Transaction.rollback() does not pop the thread association
            ThreadActionData.purgeActions();
        }

        tm.begin();

        try
        {
            assertFalse(ReadOnlyTransactionSupport.isReadOnly(tm.getTransaction()));
        }
        finally
        {
            tm.rollback();
        }
    }

    @Test
    public void testIsReadOnlySynchronizationRegistry () throws Exception
    {
        TransactionSynchronizationRegistryImple tsr = new TransactionSynchronizationRegistryImple();

        tm.begin(true);

        try
        {
            assertTrue(tsr.isReadOnly());
            assertEquals(READ_ONLY_API, ReadOnlyTransactionSupport.isReadOnly(tsr));
        }
        finally
        {
            tm.rollback();
        }
    }

    @Test
    public void testBeginNotReadOnlyDelegates () throws Exception
    {
        ReadOnlyTransactionSupport.begin(tm, false);

        TransactionImple tx = (TransactionImple) tm.getTransaction();

        assertEquals(Status.STATUS_ACTIVE, tx.getStatus());
        assertFalse(tx.isReadOnly());

        tm.commit();
    }

    @Test
    public void testBeginReadOnly () throws Exception
    {
        if (READ_ONLY_API)
        {
            ReadOnlyTransactionSupport.begin(tm, true);

            try
            {
                assertTrue(((TransactionImple) tm.getTransaction()).isReadOnly());
            }
            finally
            {
                tm.rollback();
            }
        }
        else
        {
            try
            {
                ReadOnlyTransactionSupport.begin(tm, true);

                fail("read-only begin without the read-only API must throw NotSupportedException");
            }
            catch (final NotSupportedException ex)
            {
                assertTrue(ex.getMessage().contains("2.0.2"));
            }

            assertNull(tm.getTransaction());
        }
    }

    private static boolean hasMethod (Class<?> type, String name, Class<?>... parameterTypes)
    {
        try
        {
            type.getMethod(name, parameterTypes);

            return true;
        }
        catch (NoSuchMethodException e)
        {
            return false;
        }
    }

    /**
     * A Transactional literal whose isReadOnly() returns true. The method is
     * deliberately not annotated with @Override so this compiles whether or
     * not the interface declares it (it only does from 2.0.2).
     */
    private static class ReadOnlyTransactionalLiteral implements Transactional
    {
        @Override
        public Class<? extends Annotation> annotationType ()
        {
            return Transactional.class;
        }

        @Override
        public TxType value ()
        {
            return TxType.REQUIRED;
        }

        @Override
        public Class[] rollbackOn ()
        {
            return new Class[] {};
        }

        @Override
        public Class[] dontRollbackOn ()
        {
            return new Class[] {};
        }

        public boolean isReadOnly ()
        {
            return true;
        }
    }
}
