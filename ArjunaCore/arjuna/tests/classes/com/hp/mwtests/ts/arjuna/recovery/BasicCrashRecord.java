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
 * $Id: BasicCrashRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.recovery;

import java.io.*;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

public class BasicCrashRecord extends AbstractRecord
{   

    public BasicCrashRecord ()
    {
    }

    public int typeIs ()
    {
	return RecordType.USER_DEF_FIRST0;
    }
   
    public boolean doSave ()
    {
	return false;
    }

    public String type ()
    {
	return "/StateManager/AbstractRecord/com.hp.mwtests.ts.arjuna.recovery.BasicCrashRecord";
    }
   
    public boolean save_state (OutputObjectState os, int i)
    {
	return false;
    }

    public boolean restore_state (InputObjectState os, int i)
    {
	return false;
    }
   
    public Object value ()
    {
	return _id;
    }

    public void setValue (Object object)
    {
    }

    public int nestedAbort()
    {
	return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedCommit()
    {
	return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedPrepare()
    {
	return TwoPhaseOutcome.PREPARE_OK;
    }

    public int topLevelAbort()
    {
	return TwoPhaseOutcome.FINISH_OK;
    }
    
    public int topLevelCommit()
    {
	throw new com.arjuna.ats.arjuna.exceptions.FatalError();
    }

    public int topLevelPrepare()
    {
	return TwoPhaseOutcome.PREPARE_OK;
    }

    public void alter(AbstractRecord abstractRecord)
    {
    }

    public void merge(AbstractRecord abstractRecord)
    {
    }

    public boolean shouldAdd(AbstractRecord abstractRecord)
    {
	return false;
    }

    public boolean shouldAlter(AbstractRecord abstractRecord)
    {
	return false;
    }

    public boolean shouldMerge(AbstractRecord abstractRecord)
    {
	return false;
    }

    public boolean shouldReplace(AbstractRecord abstractRecord)
    {
	return false;
    }

    private Uid _id = new Uid();
    
}
