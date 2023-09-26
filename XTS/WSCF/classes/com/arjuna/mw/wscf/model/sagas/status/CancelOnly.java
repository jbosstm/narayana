/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.sagas.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * The status of the coordination is such that it will eventually cancel.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: CancelOnly.java,v 1.2 2004/03/15 13:25:05 nmcl Exp $
 * @since 1.0.
 */

public class CancelOnly implements Status
{

    public static CancelOnly instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "org.w3c.wscf.sagas.status.CancelOnly";
    }

    private CancelOnly ()
    {
    }

    private static final CancelOnly _instance = new CancelOnly();
    
}