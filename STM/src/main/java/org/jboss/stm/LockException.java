/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm;

/**
 * Thrown when an error occurs during lock acquisition or release.
 * 
 * @author marklittle
 *
 */

public class LockException extends RuntimeException
{
    public LockException (String reason)
    {
        super(reason);
    }
    
    private static final long serialVersionUID = 1L;
}