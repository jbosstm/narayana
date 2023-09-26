/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.twophase.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * Some of the coordinator's participants cancelled and some confirmed.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: HeuristicMixed.java,v 1.1 2003/01/07 10:33:46 nmcl Exp $
 * @since 1.0.
 */

public class HeuristicMixed implements Status
{

    public static HeuristicMixed instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "org.w3c.wscf.twophase.status.HeuristicMixed";
    }

    private HeuristicMixed ()
    {
    }

    private static final HeuristicMixed _instance = new HeuristicMixed();
    
}