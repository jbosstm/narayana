/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.exceptions;

/**
 * Thrown by TransactionSynchronizationRegistry implementation if the
 * transaction has rolled back. We have to do this because JTA 1.1 took the
 * decision to hide or ignore one of the exceptions that registerSynchronization
 * may otherwise throw!
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: NotImplementedException.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.2.4.
 */

public class RollbackException extends IllegalStateException
{
    static final long serialVersionUID = -2708657802308998286L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public RollbackException()
    {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized, and may subsequently be initialized by a call to
     * {@link #initCause}.
     * 
     * @param message
     *            the detail message. The detail message is saved for later
     *            retrieval by the {@link #getMessage()} method.
     */
    public RollbackException(String message)
    {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with <code>cause</code> is
     * <i>not</i> automatically incorporated in this exception's detail message.
     * 
     * @param message
     *            the detail message (which is saved for later retrieval by the
     *            {@link #getMessage()} method).
     * @param cause
     *            the cause (which is saved for later retrieval by the
     *            {@link #getCause()} method). (A <code>null</code> value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     * @since 1.4
     */
    public RollbackException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause and a detail message
     * of <code>(cause==null ? null : cause.toString())</code> (which typically
     * contains the class and detail message of <code>cause</code>). This
     * constructor is useful for exceptions that are little more than wrappers
     * for other throwables (for example,
     * {@link java.security.PrivilegedActionException}).
     * 
     * @param cause
     *            the cause (which is saved for later retrieval by the
     *            {@link #getCause()} method). (A <code>null</code> value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     * @since 1.4
     */
    public RollbackException(Throwable cause)
    {
        super(cause);
    }
}