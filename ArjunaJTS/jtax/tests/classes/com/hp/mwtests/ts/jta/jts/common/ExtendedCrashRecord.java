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

package com.hp.mwtests.ts.jta.jts.common;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

import java.io.IOException;

// Mostly a duplication of com.hp.mwtests.ts.arjuna.resources.CrashRecord

public class ExtendedCrashRecord extends AbstractRecord // XAResourceRecord
{
    public enum CrashLocation { NoCrash, CrashInPrepare, CrashInCommit, CrashInAbort }
    public enum CrashType { Normal, HeuristicHazard }

    // Need a default constructor to keep the RecordTypeManager happy
    public ExtendedCrashRecord()
    {
        _cl = CrashLocation.NoCrash;
        _ct = CrashType.Normal;
    }

    public ExtendedCrashRecord(CrashLocation cl, CrashType ct)
    {
        super(new Uid());

        _cl = cl;
        _ct = ct;
    }

    public void forget()
    {
        _forgotten = true;
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
        if (!_forgotten && _cl == CrashLocation.CrashInAbort)
        {
            _forgotten = true;
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
        if (!_forgotten && _cl == CrashLocation.CrashInCommit)
        {
            _forgotten = true;
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
        if (!_forgotten && _cl == CrashLocation.CrashInAbort)
        {
            _forgotten = true;
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
        try {
            os.packBoolean(_forgotten);
        } catch (IOException e) {
            return false;
        }
        return super.save_state(os, ot);
    }

    public boolean restore_state(InputObjectState os, int ot)
    {
        try {
            _forgotten = os.unpackBoolean();
        } catch (IOException e) {
            return false;
        }

        return super.restore_state(os, ot);
    }

    public String type()
    {
        return "/StateManager/AbstractRecord/ExtendedCrashRecord";
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

    public String toString ()
    {
        return "CrashRecord. forgotten: " + _forgotten + ": " + super.toString();
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

    private boolean _forgotten;
    private CrashLocation _cl;
    private CrashType _ct;
}