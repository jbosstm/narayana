/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wstx.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * The transaction is in a state which means that the only eventual outcome
 * is for it to rollback.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: RollbackOnly.java,v 1.1 2002/11/25 11:00:53 nmcl Exp $
 * @since 1.0.
 */

public class RollbackOnly implements Status
{

    public static RollbackOnly instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "Status.RollbackOnly";
    }

    private RollbackOnly ()
    {
    }

    private static final RollbackOnly _instance = new RollbackOnly();
    
}