/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wst;

/**
 * Thrown if there is a fault during complete or compensation.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: FaultedException.java,v 1.1 2004/04/21 15:57:04 nmcl Exp $
 * @since 1.0.
 */

public class FaultedException extends Exception
{
    
    public FaultedException ()
    {
	super();
    }

    public FaultedException (String s)
    {
	super(s);
    }

}