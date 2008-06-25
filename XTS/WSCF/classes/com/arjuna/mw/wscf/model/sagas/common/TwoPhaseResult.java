/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TwoPhaseResult.java,v 1.2 2004/03/15 13:25:03 nmcl Exp $
 */

package com.arjuna.mw.wscf.model.sagas.common;

/**
 * The outcomes which can be generated when a two-phase coordinator
 * attempts to prepare/commit/rollback.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TwoPhaseResult.java,v 1.2 2004/03/15 13:25:03 nmcl Exp $
 * @since 1.0.
 */

public class TwoPhaseResult
{

    public static final int PREPARE_OK = 0;
    public static final int PREPARE_NOTOK = 1;
    public static final int PREPARE_READONLY = 2;
    public static final int HEURISTIC_CANCEL = 3;
    public static final int HEURISTIC_CONFIRM = 4;
    public static final int HEURISTIC_MIXED = 5;
    public static final int HEURISTIC_HAZARD = 6;
    public static final int FINISH_OK = 7;
    public static final int FINISH_ERROR = 8;
    public static final int NOT_PREPARED = 9;
    public static final int CANCELLED = 10;
    public static final int CONFIRMED = 11;
    
    /**
     * @return a human-readable version of the outcome.
     */

    public static String stringForm (int res)
    {
	switch (res)
	{
	case PREPARE_OK:
	    return "TwoPhaseResult.PREPARE_OK";
	case PREPARE_NOTOK:
	    return "TwoPhaseResult.PREPARE_NOTOK";
	case PREPARE_READONLY:
	    return "TwoPhaseResult.PREPARE_READONLY";
	case HEURISTIC_CANCEL:
	    return "TwoPhaseResult.HEURISTIC_CANCEL";
	case HEURISTIC_CONFIRM:
	    return "TwoPhaseResult.HEURISTIC_CONFIRM";
	case HEURISTIC_MIXED:
	    return "TwoPhaseResult.HEURISTIC_MIXED";
	case HEURISTIC_HAZARD:
	    return "TwoPhaseResult.HEURISTIC_HAZARD";
	case FINISH_OK:
	    return "TwoPhaseResult.FINISH_OK";
	case FINISH_ERROR:
	    return "TwoPhaseResult.FINISH_ERROR";
	case NOT_PREPARED:
	    return "TwoPhaseResult.NOT_PREPARED";
	case CANCELLED:
	    return "TwoPhaseResult.CANCELLED";
	case CONFIRMED:
	    return "TwoPhaseResult.CONFIRMED";
	default:
	    return "Unknown - "+res;
	}
    }
	
}
