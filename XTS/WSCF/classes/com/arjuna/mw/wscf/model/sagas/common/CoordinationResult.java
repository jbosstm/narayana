/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.sagas.common;

/**
 * The final coordination result.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: CoordinationResult.java,v 1.2 2004/03/15 13:25:03 nmcl Exp $
 * @since 1.0.
 */

public class CoordinationResult
{

    public static final int CONFIRMED = 0;
    public static final int CANCELLED = 1;
    public static final int COMPENSATED = 2;
    
    /**
     * @return a human-readable version of the outcome.
     */

    public static String stringForm (int res)
    {
	switch (res)
	{
	case CANCELLED:
	    return "CoordinationResult.CANCELLED";
	case CONFIRMED:
	    return "CoordinationResult.CONFIRMED";
	case COMPENSATED:
	    return "CoordinationResult.COMPENSATED";
	default:
	    return "Unknown - "+res;
	}
    }
	
}