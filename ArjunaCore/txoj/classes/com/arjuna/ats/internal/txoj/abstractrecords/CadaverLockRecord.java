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
 * $Id: CadaverLockRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.txoj.abstractrecords;

import java.io.PrintWriter;

import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.lockstore.LockStore;
import com.arjuna.ats.txoj.logging.txojLogger;

/*
 *
 * Cadaver Lock Record Class Implementation
 *
 * Instances of this record class are created by LockManager if the
 * object goes out of scope prior to the end of a manipulating action.
 * The intention is that the operations of this class will clean up
 * those locks that get left set as the object goes out of scope but
 * which must remain held until the action ends otherwise serialisability
 * is compromised
 *
 */ 

public class CadaverLockRecord extends LockRecord
{

    public CadaverLockRecord (LockStore store, LockManager lm, BasicAction currAct)
    {
	super(lm, currAct);
	
	cadaverLockStore = store;
	objectTypeName = new String(lm.type());
	
	if (lm.getObjectModel() == ObjectModel.SINGLE)
	{
	    doRelease = false;
	}
	else
	    doRelease = true;

	if (txojLogger.logger.isTraceEnabled())
	{
	    txojLogger.logger.trace("CadaverLockRecord::CadaverLockRecord("+store+
				       ", "+lm.get_uid()+")");
	}
    }
    
    /*
     * Public virtual functions. These are all re-implementations of inherited
     * functions 
     */
    
    public boolean propagateOnAbort ()
    {
	return true;
    }
    
    /*
     * Atomic action controlled functions. These functions create an instance
     * of CadaverLockManager to handle the lock manipulation that is needed and
     * then throw it away when done.
     */

    public int nestedAbort ()
    {
	if (txojLogger.logger.isTraceEnabled())
	{
	    txojLogger.logger.trace("CadaverLockRecord::nestedAbort() for "+order());
	}
	
	if (doRelease)
	{
	    CadaverLockManager manager = new CadaverLockManager(order(), objectTypeName);

	    if (super.actionHandle == null)
        {
            throw new FatalError(txojLogger.i18NLogger.get_CadaverLockRecord_1());
	    }
	    
	    return (manager.releaseAll(super.actionHandle.get_uid()) ? TwoPhaseOutcome.FINISH_OK : TwoPhaseOutcome.FINISH_ERROR);
	}
	else
	    return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedCommit ()
    {
	if (txojLogger.logger.isTraceEnabled())
	{
	    txojLogger.logger.trace("CadaverLockRecord::nestedCommit() for "+order());
	}
	
	if (doRelease)
	{
	    /*
	     * Need to change the owner of the locks from the current
	     * committing action to its parent. Since no genuine LockManager
	     * exists at this time create one to take care of this.
	     */
    
	    if (super.actionHandle == null)
	    {
		    throw new FatalError(txojLogger.i18NLogger.get_CadaverLockRecord_2());
	    }
    
	    CadaverLockManager manager = new CadaverLockManager(order(), objectTypeName);
	    
	    return (manager.propagate(super.actionHandle.get_uid(), super.actionHandle.parent().get_uid()) ? TwoPhaseOutcome.FINISH_OK : TwoPhaseOutcome.FINISH_ERROR);
	}
	else
	    return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelAbort ()
    {
	if (txojLogger.logger.isTraceEnabled())
	{
	    txojLogger.logger.trace("CadaverLockRecord::topLevelAbort() for "+order());
	}
	
	if (doRelease)
	{
	    if (super.actionHandle == null)
	    {
    		throw new FatalError(txojLogger.i18NLogger.get_CadaverLockRecord_3());
	    }

	    CadaverLockManager manager = new CadaverLockManager(order(), objectTypeName);

	    return (manager.releaseAll(super.actionHandle.get_uid()) ? TwoPhaseOutcome.FINISH_OK : TwoPhaseOutcome.FINISH_ERROR);
	}
	else
	    return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelCommit ()
    {
	if (txojLogger.logger.isTraceEnabled())
	{
	    txojLogger.logger.trace("CadaverLockRecord::topLevelCommit() for "+order());
	}
	
	if (doRelease)
	{
	    if (super.actionHandle == null)
	    {
    		throw new FatalError(txojLogger.i18NLogger.get_CadaverLockRecord_4());
	    }

	    CadaverLockManager manager = new CadaverLockManager(order(), objectTypeName);

	    return (manager.releaseAll(super.actionHandle.get_uid()) ? TwoPhaseOutcome.FINISH_OK : TwoPhaseOutcome.FINISH_ERROR);
	}
	else
	    return TwoPhaseOutcome.FINISH_OK;
    }

    public void print (PrintWriter strm)
    {
	strm.println("CadaverLockRecord : ");
	super.print(strm);
    }

    public String type ()
    {
	return "/StateManager/AbstractRecord/LockRecord/CadaverLockRecord";
    }
    
    public boolean shouldReplace (AbstractRecord ar)
    {
	return (((order().equals(ar.order())) &&
		 ar.typeIs() == RecordType.LOCK ) ? true : false);
    }
    
    /*
     * Already determined that ar is a LockRecord, otherwise replace would
     * not have been called.
     * So, get the type from it before it is deleted!
     */
    
    public void replace (AbstractRecord ar)
    {
	LockRecord lr = (LockRecord) ar;

	objectTypeName = lr.lockType();
    }
    
    protected CadaverLockRecord ()
    {
	super();
	
	cadaverLockStore = null;
	objectTypeName = null;
	doRelease = false;
	
	if (txojLogger.logger.isTraceEnabled())
	{
	    txojLogger.logger.trace("CadaverLockRecord::CadaverLockRecord ()");
	}
    }

    private LockStore   cadaverLockStore;
    private String      objectTypeName;
    private boolean     doRelease;

}
