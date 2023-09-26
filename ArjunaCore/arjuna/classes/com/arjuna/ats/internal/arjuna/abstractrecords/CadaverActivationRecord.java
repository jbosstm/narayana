/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.abstractrecords;

import com.arjuna.ats.arjuna.ObjectStatus;
import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.logging.tsLogger;

/*
 * This constructor is used to create a new instance of an
 * CadaverActivationRecord.
 */

public class CadaverActivationRecord extends ActivationRecord
{

    public CadaverActivationRecord (StateManager sm)
    {
	super(ObjectStatus.PASSIVE, sm, null);
	    
	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("CadaverActivationRecord::CadaverActivationRecord(" + sm.get_uid() + ")");
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
	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("CadaverActivationRecord::nestedAbort() for " + get_uid());
    }
	
	super.nestedAbort();
	
	return TwoPhaseOutcome.FINISH_OK;
    }
    
    public int nestedCommit ()
    {
	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("CadaverActivationRecord::nestedCommit() for " + get_uid());
    }
	
	return TwoPhaseOutcome.FINISH_OK;	
    }
    
    public int nestedPrepare ()
    {
	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("CadaverActivationRecord::nestedPrepare() for " + get_uid());
    }
	
	super.nestedPrepare();
	
	return TwoPhaseOutcome.PREPARE_READONLY;
    }
    
    public int topLevelAbort ()
    {
	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("CadaverActivationRecord::topLevelAbort() for " + get_uid());
    }
	
	super.topLevelAbort();
	
	return TwoPhaseOutcome.FINISH_OK;
    }
    
    public int topLevelCommit ()
    {
	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("CadaverActivationRecord::topLevelCommit() for " + get_uid());
    }
	
	super.topLevelCommit();
	
	return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelPrepare ()
    {
	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("CadaverActivationRecord::topLevelPrepare() for " + get_uid());
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

	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("CadaverActivationRecord::CadaverActivationRecord ()");
    }
    }
    
}