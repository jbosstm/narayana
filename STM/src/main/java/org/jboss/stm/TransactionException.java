/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm;

/**
 * Thrown if an error occurs during transaction processing, e.g., commit fails.
 * 
 * @author marklittle
 *
 */

public class TransactionException extends RuntimeException
{
    public TransactionException (String reason, int status)
    {
        super(reason);
        
        _status = status;
    }
    
    public final int getStatus ()
    {
        return _status;
    }
    
    private int _status;
    
    private static final long serialVersionUID = 1L;
}