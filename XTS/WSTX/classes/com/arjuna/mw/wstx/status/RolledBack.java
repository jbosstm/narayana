/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wstx.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * The transaction has rolled back.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: RolledBack.java,v 1.1 2002/11/25 11:00:53 nmcl Exp $
 * @since 1.0.
 */

public class RolledBack implements Status
{

    public static RolledBack instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "Status.RolledBack";
    }

    private RolledBack ()
    {
    }

    private static final RolledBack _instance = new RolledBack();
    
}