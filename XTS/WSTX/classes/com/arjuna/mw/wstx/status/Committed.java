/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wstx.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * The transaction has committed.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Committed.java,v 1.1 2002/11/25 11:00:53 nmcl Exp $
 * @since 1.0.
 */

public class Committed implements Status
{

    public static Committed instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "Status.Committed";
    }

    private Committed ()
    {
    }

    private static final Committed _instance = new Committed();
    
}