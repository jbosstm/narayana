/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wstx.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * There is no transaction associated with the current thread.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: NoTransaction.java,v 1.1 2002/11/25 11:00:53 nmcl Exp $
 * @since 1.0.
 */

public class NoTransaction implements Status
{

    public static NoTransaction instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "Status.NoTransaction";
    }

    private NoTransaction ()
    {
    }

    private static final NoTransaction _instance = new NoTransaction();
    
}