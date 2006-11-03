/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: TwoPhaseOutcome.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.coordinator;

import java.io.PrintWriter;

/*
 * If Java had proper reference parameter passing and/or allowed
 * the wrappers for basic types to modify the contents, then we
 * would not have to do this! This class should only be an "enum".
 */

/**
 * The outcomes which can be generated when a transaction
 * attempts to prepare/commit/rollback.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: TwoPhaseOutcome.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class TwoPhaseOutcome
{

    public static final int PREPARE_OK = 0;
    public static final int PREPARE_NOTOK = 1;
    public static final int PREPARE_READONLY = 2;
    public static final int HEURISTIC_ROLLBACK = 3;
    public static final int HEURISTIC_COMMIT = 4;
    public static final int HEURISTIC_MIXED = 5;
    public static final int HEURISTIC_HAZARD = 6;
    public static final int FINISH_OK = 7;
    public static final int FINISH_ERROR = 8;
    public static final int NOT_PREPARED = 9;
    public static final int ONE_PHASE_ERROR = 10;
    public static final int INVALID_TRANSACTION = 11;

    public TwoPhaseOutcome (int outcome)
    {
	_outcome = outcome;
    }

    public void setOutcome (int outcome)
    {
	_outcome = outcome;
    }

    public int getOutcome ()
    {
	return _outcome;
    }

    /**
     * @return <code>String</code> representation of the status.
     */

    public static String stringForm (int res)
    {
	switch (res)
	{
	case PREPARE_OK:
	    return "TwoPhaseOutcome.PREPARE_OK";
	case PREPARE_NOTOK:
	    return "TwoPhaseOutcome.PREPARE_NOTOK";
	case PREPARE_READONLY:
	    return "TwoPhaseOutcome.PREPARE_READONLY";
	case HEURISTIC_ROLLBACK:
	    return "TwoPhaseOutcome.HEURISTIC_ROLLBACK";
	case HEURISTIC_COMMIT:
	    return "TwoPhaseOutcome.HEURISTIC_COMMIT";
	case HEURISTIC_MIXED:
	    return "TwoPhaseOutcome.HEURISTIC_MIXED";
	case HEURISTIC_HAZARD:
	    return "TwoPhaseOutcome.HEURISTIC_HAZARD";
	case FINISH_OK:
	    return "TwoPhaseOutcome.FINISH_OK";
	case FINISH_ERROR:
	    return "TwoPhaseOutcome.FINISH_ERROR";
	case NOT_PREPARED:
	    return "TwoPhaseOutcome.NOT_PREPARED";
	case ONE_PHASE_ERROR:
	    return "TwoPhaseOutcome.ONE_PHASE_ERROR";
	case INVALID_TRANSACTION:
	    return "TwoPhaseOutcome.INVALID_TRANSACTION";
	default:
	    return "Unknown";
	}
    }
    
    public static void print (PrintWriter strm, int res)
    {
	strm.print(TwoPhaseOutcome.stringForm(res));
	strm.flush();
    }

    private int _outcome;
	
}
