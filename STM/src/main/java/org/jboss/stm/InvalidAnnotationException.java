/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm;

/**
 * Typically thrown when save_state and restore_state are not present and they should be.
 * 
 * @author marklittle
 *
 */

public class InvalidAnnotationException extends Exception
{
    public InvalidAnnotationException (String reason)
    {
        super(reason);
    }
    
    private static final long serialVersionUID = 1L;
}