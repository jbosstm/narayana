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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: BasicRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.resources;

import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;


public class CrashRecord extends AbstractRecord
{
    public enum CrashLocation { NoCrash, CrashInPrepare, CrashInCommit, CrashInAbort };
    public enum CrashType { Normal, HeuristicHazard };

    public CrashRecord (CrashLocation cl, CrashType ct)
    {
        super(new Uid());
        
        _cl = cl;
        _ct = ct;
    }

    public int typeIs()
    {
        return RecordType.USER_DEF_FIRST0;
    }

    public int nestedAbort()
    {
        return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedCommit()
    {
        return TwoPhaseOutcome.FINISH_ERROR;
    }

    public int nestedPrepare()
    {
        return TwoPhaseOutcome.PREPARE_NOTOK;
    }

    public int topLevelAbort()
    {
        if (_cl == CrashLocation.CrashInAbort)
        {
            if (_ct == CrashType.Normal)
                return TwoPhaseOutcome.FINISH_ERROR;
            else
                return TwoPhaseOutcome.HEURISTIC_HAZARD;
        }
        else
            return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelCommit()
    {
        if (_cl == CrashLocation.CrashInCommit)
        {
            if (_ct == CrashType.Normal)
                return TwoPhaseOutcome.FINISH_ERROR;
            else
                return TwoPhaseOutcome.HEURISTIC_HAZARD;
        }
        else
            return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelPrepare()
    {
        if (_cl == CrashLocation.CrashInAbort)
        {
            if (_ct == CrashType.Normal)
                return TwoPhaseOutcome.PREPARE_NOTOK;
            else
                return TwoPhaseOutcome.HEURISTIC_HAZARD;
        }
        else
            return TwoPhaseOutcome.PREPARE_OK;
    }

    public boolean doSave()
    {
        return true;
    }

    public boolean save_state(OutputObjectState os, int ot)
    {
        return true;
    }

    public boolean restore_state(InputObjectState os, int ot)
    {
        return true;
    }

    public String type()
    {
        return "/StateManager/AbstractRecord/CrashRecord";
    }

    public boolean shouldAdd(AbstractRecord a)
    {
        return false;
    }

    public boolean shouldMerge(AbstractRecord a)
    {
        return false;
    }

    public boolean shouldReplace(AbstractRecord a)
    {
        return false;
    }

    public boolean shouldAlter(AbstractRecord a)
    {
        return false;
    }

    public void merge(AbstractRecord a)
    {
    }

    public void alter(AbstractRecord a)
    {
    }

    /**
     * @return <code>Object</code> to be used to order.
     */

    public Object value()
    {
        return null;
    }

    public void setValue(Object o)
    {
    }

    private CrashLocation _cl;
    private CrashType _ct;
}

