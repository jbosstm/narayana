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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: DisposeRecord.java 2342 2006-03-30 13:06:17Z  $
 */

/*
 *
 * Dipose Record Class.
 *
 */

package com.arjuna.ats.arjuna;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreType;
import java.io.PrintWriter;

import java.io.IOException;

import com.arjuna.common.util.logging.*;

public class DisposeRecord extends CadaverRecord
{

    public DisposeRecord (ObjectStore objStore, StateManager sm)
    {
	super(null, objStore, sm);
	
	store = objStore;
	
	if (sm != null)
	{
	    objectUid = sm.get_uid();
	    typeName = sm.type();
	}
	else
	{
	    objectUid = Uid.nullUid();
	    typeName = null;
	}

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, 
				     FacilityCode.FAC_ABSTRACT_REC,
				     "DisposeRecord::DisposeRecord("+objStore+", "+objectUid+")");
	}
    }

    public void finalize ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.DESTRUCTORS, VisibilityLevel.VIS_PUBLIC, 
				     FacilityCode.FAC_ABSTRACT_REC, 
				     "DisposeRecord.finalize for "+order());
	}
	
	store = null;
	typeName = null;

	super.finalize();
    }

    public boolean propagateOnAbort ()
    {
	return false;
    }

    public int typeIs ()
    {
	return RecordType.DISPOSE;
    }

    public ClassName className ()
    {
	return ArjunaNames.Implementation_AbstractRecord_CadaverRecord_DisposeRecord();
    }
    
    public int nestedAbort ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, "DisposeRecord::nestedAbort() for "+order());
	}
	
	return TwoPhaseOutcome.FINISH_OK;
    }
    
    public int nestedCommit ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, "DisposeRecord::nestedCommit() for "+order());
	}
	
	return TwoPhaseOutcome.FINISH_OK;
    }
    
    public int nestedPrepare ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, "DisposeRecord::nestedPrepare() for "+order());
	}
	
	if ((store != null) && (objectUid.notEquals(Uid.nullUid())))
	    return TwoPhaseOutcome.PREPARE_OK;
	else
	    return TwoPhaseOutcome.PREPARE_NOTOK;
    }
    
    public int topLevelAbort ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, "DisposeRecord::topLevelAbort() for "+order());
	}
	
	return TwoPhaseOutcome.FINISH_OK;
    }
    
    /**
     * At topLevelCommit we remove the state from the object store.
     *
     * @message com.arjuna.ats.arjuna.DisposeRecord_5 [com.arjuna.ats.arjuna.DisposeRecord_5] DisposeRecord::topLevelCommit - exception while deleting state {0}
     */
    
    public int topLevelCommit ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, "DisposeRecord::topLevelCommit() for "+order());
	}

	if ((store != null) && (objectUid.notEquals(Uid.nullUid())))
	{
	    try
	    {
		if (store.remove_committed(objectUid, typeName))
		{
		    // only valid if not doing recovery

		    if (super.objectAddr != null)
		    {
			super.objectAddr.destroyed();
		    }
		    
		    return TwoPhaseOutcome.FINISH_OK;
		}
	    }
	    catch (Exception e)
	    {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		{
		    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.DisposeRecord_5", 
						new Object[]{e});
		}
	    }
	}
	
	return TwoPhaseOutcome.FINISH_ERROR;
    }
    
    public int topLevelPrepare ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ABSTRACT_REC, 
				     "DisposeRecord::topLevelPrepare() for "+order());
	}
	
	if ((store != null) && (objectUid.notEquals(Uid.nullUid())))
	{
	    return TwoPhaseOutcome.PREPARE_OK;
	}
	else
	    return TwoPhaseOutcome.PREPARE_NOTOK;
    }
    
    public void print (PrintWriter strm)
    {
	strm.println("Dispose for:");
	super.print(strm);
    }
    
    public boolean doSave ()
    {
	//	return true;
	return false;
    }
    
    /**
     * @message com.arjuna.ats.arjuna.DisposeRecord_1 [com.arjuna.ats.arjuna.DisposeRecord_1] DisposeRecord::save_state - type of store is unknown
     * @message com.arjuna.ats.arjuna.DisposeRecord_2 [com.arjuna.ats.arjuna.DisposeRecord_2] DisposeRecord::save_state - failed
     * @message com.arjuna.ats.arjuna.DisposeRecord_3 [com.arjuna.ats.arjuna.DisposeRecord_3] DisposeRecord::save_state - no object store defined.
     * @message com.arjuna.ats.arjuna.DisposeRecord_4 [com.arjuna.ats.arjuna.DisposeRecord_4] DisposeRecord::restore_state - invalid store type {0}
     */

    public boolean save_state (OutputObjectState os, int ot)
    {
	boolean res = true;
	
	if ((store != null) && (objectUid.notEquals(Uid.nullUid())))
	{
	    if (!ObjectStoreType.valid(store.typeIs()))
	    {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.DisposeRecord_1");
			
		res = false;
	    }
	    else
	    {
		try
		{
		    os.packInt(store.typeIs());
		    store.pack(os);
				
		    objectUid.pack(os);
		    os.packString(typeName);
		}
		catch (IOException e)
		{
		    if (tsLogger.arjLoggerI18N.isWarnEnabled())
			tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.DisposeRecord_2");
		    res = false;
		}
	    }
	}
	else
	{
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.DisposeRecord_3");
		
	    res = false;
	}
	
	return res;
    }
    
    public boolean restore_state (InputObjectState os, int ot)
    {
	boolean res = true;
	int objStoreType = 0;
	
	try
	{
	    objStoreType = os.unpackInt();
		
	    if (ObjectStoreType.valid(objStoreType))
	    {
		store = null;
			
		store = new ObjectStore(ObjectStoreType.typeToClassName(objStoreType));
		store.unpack(os);
			
		objectUid.unpack(os);
		typeName = os.unpackString();
	    }
	    else
	    {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		{
		    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.DisposeRecord_4", 
						new Object[]{Integer.toString(objStoreType)});
		}

		res = false;
	    }
	}
	catch (IOException e)
	{
	    res = false;
	}
	
	return res;
    }
    
    public String type ()
    {
	return "/StateManager/AbstractRecord/RecoveryRecord/PersistenceRecord/CadaverRecord/DisposeRecord";
    }
    
    public boolean shouldAdd (AbstractRecord a)
    {
	return false;
    }
    
    public boolean shouldMerge (AbstractRecord a)
    {
	return false;
    }
    
    public boolean shouldReplace (AbstractRecord a)
    {
	return false;
    }
    
    public boolean shouldAlter (AbstractRecord a)
    {
	return false;
    }

    public static AbstractRecord create ()
    {
	return new DisposeRecord();
    }

    protected DisposeRecord ()
    {
	super();

	objectUid = new Uid(Uid.nullUid());
	typeName = null;
	store = null;
    }
    
    private Uid         objectUid;
    private String      typeName;
    private ObjectStore store;
 
}

