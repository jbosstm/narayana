/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.twophase.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * The coordinator has prepared.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Prepared.java,v 1.1 2003/01/07 10:33:46 nmcl Exp $
 * @since 1.0.
 */

public class Prepared implements Status
{

    public static Prepared instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "org.w3c.wscf.twophase.status.Prepared";
    }

    private Prepared ()
    {
    }

    private static final Prepared _instance = new Prepared();
    
}