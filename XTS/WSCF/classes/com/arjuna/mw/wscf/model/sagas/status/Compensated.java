/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.sagas.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * The coordinator has prepared.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Compensated.java,v 1.2 2004/03/15 13:25:06 nmcl Exp $
 * @since 1.0.
 */

public class Compensated implements Status
{

    public static Compensated instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "org.w3c.wscf.sagas.status.Compensated";
    }

    private Compensated ()
    {
    }

    private static final Compensated _instance = new Compensated();
    
}