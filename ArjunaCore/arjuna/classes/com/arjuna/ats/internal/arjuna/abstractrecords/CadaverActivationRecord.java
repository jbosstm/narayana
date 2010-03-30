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
 * $Id: CadaverActivationRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.abstractrecords;

import com.arjuna.ats.arjuna.ObjectStatus;
import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.logging.tsLogger;

import com.arjuna.ats.arjuna.coordinator.*;

/*
 * This constructor is used to create a new instance of an
 * CadaverActivationRecord.
 */

public class CadaverActivationRecord extends ActivationRecord
{

    public CadaverActivationRecord (StateManager sm)
    {
	super(ObjectStatus.PASSIVE, sm, null);
	    
	if (tsLogger.arjLogger.isDebugEnabled()) {
        tsLogger.arjLogger.debug("CadaverActivationRecord::CadaverActivationRecord(" + sm.get_uid() + ")");
    }
    }
    
    public boolean propagateOnAbort ()
    {
	return true;
    }
    
    /*
     * Supress all atomic action ops for deleted object
     */
    
    public int nestedAbort ()
    {
	if (tsLogger.arjLogger.isDebugEnabled()) {
        tsLogger.arjLogger.debug("CadaverActivationRecord::nestedAbort() for " + get_uid());
    }
	
	super.nestedAbort();
	
	return TwoPhaseOutcome.FINISH_OK;
    }
    
    public int nestedCommit ()
    {
	if (tsLogger.arjLogger.isDebugEnabled()) {
        tsLogger.arjLogger.debug("CadaverActivationRecord::nestedCommit() for " + get_uid());
    }
	
	return TwoPhaseOutcome.FINISH_OK;	
    }
    
    public int nestedPrepare ()
    {
	if (tsLogger.arjLogger.isDebugEnabled()) {
        tsLogger.arjLogger.debug("CadaverActivationRecord::nestedPrepare() for " + get_uid());
    }
	
	super.nestedPrepare();
	
	return TwoPhaseOutcome.PREPARE_READONLY;
    }
    
    public int topLevelAbort ()
    {
	if (tsLogger.arjLogger.isDebugEnabled()) {
        tsLogger.arjLogger.debug("CadaverActivationRecord::topLevelAbort() for " + get_uid());
    }
	
	super.topLevelAbort();
	
	return TwoPhaseOutcome.FINISH_OK;
    }
    
    public int topLevelCommit ()
    {
	if (tsLogger.arjLogger.isDebugEnabled()) {
        tsLogger.arjLogger.debug("CadaverActivationRecord::topLevelCommit() for " + get_uid());
    }
	
	super.topLevelCommit();
	
	return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelPrepare ()
    {
	if (tsLogger.arjLogger.isDebugEnabled()) {
        tsLogger.arjLogger.debug("CadaverActivationRecord::topLevelPrepare() for " + get_uid());
    }
	
	// make sure SM instance forgets about action
	
	super.topLevelCommit();
	
	return TwoPhaseOutcome.PREPARE_READONLY;
    }
    
    public String type ()
    {
	return "/StateManager/AbstractRecord/CadaverActivationRecord";
    }
    
    /*
     * shouldMerge and should_replace are invoked by the record list manager
     * to determine if two records should be merged together or if the
     * 'newer' should replace the older.
     * shouldAdd determines if the new record should be added in addition
     * to the existing record and is currently only invoked if both of
     * shouldMerge and shouldReplace return false
     * Default implementations here always return false - ie new records
     * do not override old
     */
    
    public boolean shouldReplace (AbstractRecord ar)
    {
	return  (((order().equals(ar.order())) &&
		  ar.typeIs() == RecordType.ACTIVATION ) ? true : false);
    }
    
    protected CadaverActivationRecord ()
    {
	super();

	if (tsLogger.arjLogger.isDebugEnabled()) {
        tsLogger.arjLogger.debug("CadaverActivationRecord::CadaverActivationRecord ()");
    }
    }
    
}
