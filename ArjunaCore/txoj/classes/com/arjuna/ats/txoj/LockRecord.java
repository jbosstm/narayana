/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
 * $Id: LockRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.txoj;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.common.Uid;

import com.arjuna.ats.txoj.logging.txojLogger;
import com.arjuna.ats.txoj.logging.FacilityCode;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.txoj.TxOJNames;
import com.arjuna.ats.txoj.common.*;
import java.io.*;

/**
 * @message com.arjuna.ats.txoj.LockRecord_1 [com.arjuna.ats.txoj.LockRecord_1] - LockRecord::set_value() called illegally
 * @message com.arjuna.ats.txoj.LockRecord_2 [com.arjuna.ats.txoj.LockRecord_2] - LockRecord - release failed for action {0}
 * @message com.arjuna.ats.txoj.LockRecord_3 [com.arjuna.ats.txoj.LockRecord_3] - LockRecord::nestedAbort - no current action
 * @message com.arjuna.ats.txoj.LockRecord_4 [com.arjuna.ats.txoj.LockRecord_4] - LockRecord::nestedCommit - no current action
 * @message com.arjuna.ats.txoj.LockRecord_5 [com.arjuna.ats.txoj.LockRecord_5] - LockRecord - release failed for action {0}
 * @message com.arjuna.ats.txoj.LockRecord_6 [com.arjuna.ats.txoj.LockRecord_6] - LockRecord::topLevelCommit - no current action
 * @message com.arjuna.ats.txoj.LockRecord_7 [com.arjuna.ats.txoj.LockRecord_7] - Invocation of LockRecord::restore_state for {0} inappropriate - ignored for {1}
 */


/*
 * Default visibility.
 */

class LockRecord extends AbstractRecord
{

    public LockRecord (LockManager lm, BasicAction currAct)
    {
	super(lm.get_uid(), lm.type(), ObjectType.ANDPERSISTENT);

	actionHandle = currAct;

	managerAddress = lm;
	readOnly = false;
	managerType = lm.type();
    }
    
    public LockRecord (LockManager lm, boolean rdOnly, BasicAction currAct)
    {
	super(lm.get_uid(), lm.type(), ObjectType.ANDPERSISTENT);

	if (txojLogger.aitLogger.isDebugEnabled())
	{
	    txojLogger.aitLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				     (FacilityCode.FAC_CONCURRENCY_CONTROL | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC),
				     "LockRecord::LockRecord("+lm.get_uid()+", "
				       +(readOnly ? "PREPARE_READONLY" : "WRITEABLE")+")");
	}
	
	actionHandle = currAct;

	managerAddress = lm;
	readOnly = rdOnly;
	managerType = lm.type();
    }

    public int typeIs ()
    {
	return RecordType.LOCK;
    }

    public Object value ()
    {
	return (Object) managerAddress;
    }

    public void setValue (Object o)
    {
	if (txojLogger.aitLoggerI18N.isWarnEnabled())
	    txojLogger.aitLoggerI18N.warn("com.arjuna.ats.txoj.LockRecord_1");
    }

    public int nestedAbort ()
    {
	if (txojLogger.aitLogger.isDebugEnabled())
	{
	    txojLogger.aitLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				       (FacilityCode.FAC_CONCURRENCY_CONTROL | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC),
				       "LockRecord::nestedAbort() for "+order());
	}
	
	/* default constructor problem. */

	if (managerAddress == null)
	    return TwoPhaseOutcome.FINISH_ERROR;

	if (actionHandle != null)
	{
	    Uid toRelease = actionHandle.get_uid();
	    
	    actionHandle = actionHandle.parent();
	    
	    if (!managerAddress.releaseAll(toRelease))
	    {
		if (txojLogger.aitLoggerI18N.isWarnEnabled())
		{
		    txojLogger.aitLoggerI18N.warn("com.arjuna.ats.txoj.LockRecord_2",
						new Object[]{toRelease});
		}

		return TwoPhaseOutcome.FINISH_ERROR;
	    }
	}
	else
	    if (txojLogger.aitLoggerI18N.isWarnEnabled())
		txojLogger.aitLoggerI18N.warn("com.arjuna.ats.txoj.LockRecord_3");

	return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedCommit ()
    {
	if (txojLogger.aitLogger.isDebugEnabled())
	{
	    txojLogger.aitLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     (FacilityCode.FAC_CONCURRENCY_CONTROL | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC),
				       "LockRecord::nestedCommit() for "+order());
	}
	
	/* default constructor problem. */

	if (managerAddress == null)
	    return TwoPhaseOutcome.FINISH_ERROR;

	if (actionHandle != null)
	{
	    Uid toRelease = actionHandle.get_uid();
	    
	    actionHandle = actionHandle.parent();
	    
	    return (managerAddress.propagate(toRelease, actionHandle.get_uid()) ? TwoPhaseOutcome.FINISH_OK : TwoPhaseOutcome.FINISH_ERROR);
	}
	else
	{
	    if (txojLogger.aitLoggerI18N.isWarnEnabled())
		txojLogger.aitLoggerI18N.warn("com.arjuna.ats.txoj.LockRecord_4");
	}

	return TwoPhaseOutcome.FINISH_ERROR;
    }

    public int nestedPrepare ()
    {
	if (txojLogger.aitLogger.isDebugEnabled())
	{
	    txojLogger.aitLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     (FacilityCode.FAC_CONCURRENCY_CONTROL | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC),
				       "LockRecord::nestedPrepare() for "+order());
	}
	
	return TwoPhaseOutcome.PREPARE_OK;
    }

    public int topLevelAbort ()
    {
	if (txojLogger.aitLogger.isDebugEnabled())
	{
	    txojLogger.aitLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     (FacilityCode.FAC_CONCURRENCY_CONTROL | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC),
				       "LockRecord::topLevelAbort() for "+order());
	}
	
	return nestedAbort();
    }

    public int topLevelCommit ()
    {
	if (txojLogger.aitLogger.isDebugEnabled())
	{
	    txojLogger.aitLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     (FacilityCode.FAC_CONCURRENCY_CONTROL | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC),
				       "LockRecord::topLevelCommit() for "+order());
	}
	
	/* default constructor problem. */

	if (managerAddress == null)
	    return TwoPhaseOutcome.FINISH_ERROR;

	if (actionHandle != null)
	{
	    if (!managerAddress.releaseAll(actionHandle.get_uid()))
	    {
		if (txojLogger.aitLoggerI18N.isWarnEnabled())
		{
		    txojLogger.aitLoggerI18N.warn("com.arjuna.ats.txoj.LockRecord_5", 
						new Object[]{actionHandle.get_uid()});
		}

		return TwoPhaseOutcome.FINISH_ERROR;
	    }
	}
	else
	{
	    if (txojLogger.aitLoggerI18N.isWarnEnabled())
		txojLogger.aitLoggerI18N.warn("com.arjuna.ats.txoj.LockRecord_6");

	    return TwoPhaseOutcome.FINISH_ERROR;
	}

	return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelPrepare ()
    {
	if (txojLogger.aitLogger.isDebugEnabled())
	{
	    txojLogger.aitLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     (FacilityCode.FAC_CONCURRENCY_CONTROL | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC),
				       "LockRecord::topLevelPrepare() for "+order());
	}
	
	if (readOnly)
	{
	    if (topLevelCommit() == TwoPhaseOutcome.FINISH_OK)
		return TwoPhaseOutcome.PREPARE_READONLY;
	    else
		return TwoPhaseOutcome.PREPARE_NOTOK;
	}
    
	return TwoPhaseOutcome.PREPARE_OK;
    }

    public String toString ()
    {
	StringWriter strm = new StringWriter();

	print(new PrintWriter(strm));
	
	return strm.toString();
    }
    
    public void print (PrintWriter strm)
    {
	super.print(strm);
	strm.println("LockRecord");
    }

    /*
     * restore_state and save_state for LockRecords doesn't generally
     * apply due to object pointers.
     */

    public boolean restore_state (InputObjectState o, int t)
    {
	if (txojLogger.aitLoggerI18N.isWarnEnabled())
	{
	    txojLogger.aitLoggerI18N.warn("com.arjuna.ats.txoj.LockRecord_7",
					new Object[]{type(), order()});
	}

	return false;
    }

    public boolean save_state (OutputObjectState o, int t)
    {
	return true;
    }

    public String type ()
    {
	return "/StateManager/AbstractRecord/LockRecord";
    }

    public final boolean isReadOnly ()
    {
	return readOnly;
    }

    public final String lockType ()
    {
	return managerType;
    }
    
    public void merge (AbstractRecord a)
    {
    }

    public void alter (AbstractRecord a)
    {
    }

    public boolean shouldAdd (AbstractRecord a)
    {
	return false;
    }

    public boolean shouldAlter (AbstractRecord a)
    {
	return false;
    }

    public boolean shouldMerge (AbstractRecord a)
    {
	return false;
    }

    public boolean shouldReplace (AbstractRecord ar)
    {
	if ((order().equals(ar.order())) && typeIs() == ar.typeIs())
	{
	    /*
	     * The first test should ensure that ar is a LockRecord.
	     */
	    
	    if (((LockRecord) ar).isReadOnly() && !readOnly)
		return true;
	}
	
	return false;
    }
    
    protected LockRecord ()
    {
	super();

	if (txojLogger.aitLogger.isDebugEnabled())
	{
	    txojLogger.aitLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PROTECTED,
				     (FacilityCode.FAC_CONCURRENCY_CONTROL | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC),
				       "LockRecord::LockRecord()");
	}
	
	actionHandle = null;

	managerAddress = null;
	readOnly = false;
	managerType = null;
    }

    protected BasicAction actionHandle;  // must be changed if we propagate
    
    private LockManager managerAddress;
    private boolean     readOnly;
    private String      managerType;

}
