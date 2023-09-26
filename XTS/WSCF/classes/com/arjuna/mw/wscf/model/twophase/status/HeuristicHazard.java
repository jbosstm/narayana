/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.twophase.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * Some of the coordinator's participants cancelled, some confirmed and the
 * status of others is indeterminate.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: HeuristicHazard.java,v 1.1 2003/01/07 10:33:46 nmcl Exp $
 * @since 1.0.
 */

public class HeuristicHazard implements Status
{

    public static HeuristicHazard instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "org.w3c.wscf.twophase.status.HeuristicHazard";
    }

    private HeuristicHazard ()
    {
    }

    private static final HeuristicHazard _instance = new HeuristicHazard();
    
}