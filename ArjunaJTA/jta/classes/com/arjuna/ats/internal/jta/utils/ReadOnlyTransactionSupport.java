/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.transaction.NotSupportedException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;

/**
 * Reflective access to the read-only transaction API introduced in
 * jakarta.transaction-api 2.0.2 ({@code TransactionManager#begin(boolean)},
 * {@code Transaction#isReadOnly()}, {@code Transactional#isReadOnly()} and
 * {@code TransactionSynchronizationRegistry#isReadOnly()}).
 *
 * Narayana is compiled against jakarta.transaction-api 2.0.1, which does not
 * have any of that API, so it can never be referenced directly. All access
 * goes through this class: when the API is not present on the classpath the
 * read-only feature is simply unavailable and every query here reports
 * {@code false}.
 */
public final class ReadOnlyTransactionSupport
{
    private static final Method TRANSACTIONAL_IS_READ_ONLY = getMethod(Transactional.class, "isReadOnly");

    private static final Method TRANSACTION_IS_READ_ONLY = getMethod(Transaction.class, "isReadOnly");

    private static final Method TSR_IS_READ_ONLY = getMethod(TransactionSynchronizationRegistry.class, "isReadOnly");

    private static final Method TM_BEGIN_READ_ONLY = getMethod(TransactionManager.class, "begin", boolean.class);

    private ReadOnlyTransactionSupport()
    {
    }

    /**
     * The value of the annotation's {@code isReadOnly} attribute, or false if
     * the API in use does not have that attribute.
     */
    public static boolean isReadOnly (Transactional transactional)
    {
        if (TRANSACTIONAL_IS_READ_ONLY == null)
        {
            return false;
        }

        try
        {
            return (Boolean) TRANSACTIONAL_IS_READ_ONLY.invoke(transactional);
        }
        catch (InvocationTargetException e)
        {
            throw rethrowUnchecked(e.getCause());
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Whether the given transaction is read-only, or false if the API in use
     * does not support read-only transactions.
     */
    public static boolean isReadOnly (Transaction tx) throws SystemException
    {
        if (TRANSACTION_IS_READ_ONLY == null)
        {
            return false;
        }

        try
        {
            return (Boolean) TRANSACTION_IS_READ_ONLY.invoke(tx);
        }
        catch (InvocationTargetException e)
        {
            if (e.getCause() instanceof SystemException)
            {
                throw (SystemException) e.getCause();
            }

            throw rethrowUnchecked(e.getCause());
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Whether the transaction bound to the given registry's thread is
     * read-only, or false if the API in use does not support read-only
     * transactions.
     */
    public static boolean isReadOnly (TransactionSynchronizationRegistry tsr)
    {
        if (TSR_IS_READ_ONLY == null)
        {
            return false;
        }

        try
        {
            return (Boolean) TSR_IS_READ_ONLY.invoke(tsr);
        }
        catch (InvocationTargetException e)
        {
            throw rethrowUnchecked(e.getCause());
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Begin a transaction on the given transaction manager, read-only if
     * requested. A read-only transaction can only be requested when the
     * read-only API is present, otherwise NotSupportedException is thrown.
     */
    public static void begin (TransactionManager tm, boolean readOnly)
            throws NotSupportedException, SystemException
    {
        if (!readOnly)
        {
            tm.begin();

            return;
        }

        if (TM_BEGIN_READ_ONLY == null)
        {
            throw new NotSupportedException(
                    "Read-only transactions require jakarta.transaction-api 2.0.2 or later");
        }

        try
        {
            TM_BEGIN_READ_ONLY.invoke(tm, true);
        }
        catch (InvocationTargetException e)
        {
            if (e.getCause() instanceof NotSupportedException)
            {
                throw (NotSupportedException) e.getCause();
            }

            if (e.getCause() instanceof SystemException)
            {
                throw (SystemException) e.getCause();
            }

            throw rethrowUnchecked(e.getCause());
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static Method getMethod (Class<?> type, String name, Class<?>... parameterTypes)
    {
        try
        {
            return type.getMethod(name, parameterTypes);
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }
    }

    private static RuntimeException rethrowUnchecked (Throwable t)
    {
        if (t instanceof RuntimeException)
        {
            throw (RuntimeException) t;
        }

        if (t instanceof Error)
        {
            throw (Error) t;
        }

        throw new IllegalStateException(t);
    }
}
