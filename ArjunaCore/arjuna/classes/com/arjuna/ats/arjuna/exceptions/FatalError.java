/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.exceptions;


/**
 * This error is thrown if a fatal error occurs which would prevent further
 * progress without possibly compromising consistency.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: FatalError.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.
 */

public class FatalError extends Error
{
    static final long serialVersionUID = -3878263281474052106L;

    /**
     * Constructs a new error with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public FatalError() {
        super();
    }

    /**
     * Constructs a new error with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public FatalError(String message) {
        super(message);
    }

    /**
     * Constructs a new error with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated in
     * this error's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <code>null</code> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public FatalError(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new error with the specified cause and a detail
     * message of <code>(cause==null ? null : cause.toString())</code> (which
     * typically contains the class and detail message of <code>cause</code>).
     * This constructor is useful for errors that are little more than
     * wrappers for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <code>null</code> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.4
     */
    public FatalError(Throwable cause) {
        super(cause);
    }
}