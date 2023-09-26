/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices.util;

/**
 * The exception thrown when an invalid enumeration value is encountered.
 * @author kevin
 */
public class InvalidEnumerationException extends Exception
{
    /**
     * Serial version UID for serialisation.
     */
    private static final long serialVersionUID = -2582965233382320132L ;

    /**
     * Construct an invalid enumeration exception with a specified message.
     * @param message The exception message.
     */
    public InvalidEnumerationException(final String message)
    {
        super(message) ;
    }
}