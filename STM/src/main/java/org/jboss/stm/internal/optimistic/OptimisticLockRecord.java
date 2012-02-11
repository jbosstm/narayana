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
 * $Id: LockRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package org.jboss.stm.internal.optimistic;

import com.arjuna.ats.arjuna.ObjectStatus;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.txoj.logging.txojLogger;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.internal.txoj.abstractrecords.LockRecord;

/*
 * Optimistic cc in operation. Grab a copy of the current state so we can check against
 * the state again when we commit.
 */

class OptimisticLockRecord extends LockRecord
{
    public OptimisticLockRecord (OptimisticLockManager lm, BasicAction currAct, boolean check)
    {
        super(lm, currAct);

        try
        {
            _state = lm.getStore().read_committed(lm.get_uid(), lm.type());
        }
        catch (final ObjectStoreException ex)
        {
            _state = null;
        }
        
	_status = lm.status();
	_check = check;
    }
    
    public OptimisticLockRecord (OptimisticLockManager lm, boolean rdOnly, BasicAction currAct, boolean check)
    {
        super(lm, rdOnly, currAct);
	
        try
        {
            _state = lm.getStore().read_committed(lm.get_uid(), lm.type());
        }
        catch (final ObjectStoreException ex)
        {
            _state = null;
        }
        
	_status = lm.status();
	_check = check;
    }

    public int typeIs ()
    {
        return RecordType.USER_DEF_FIRST0;
    }
    
    public int nestedAbort ()
    {
        if (txojLogger.logger.isTraceEnabled())
        {
            txojLogger.logger.trace("LockRecord::nestedAbort() for "+order());
        }
        
	/*
	 * Optimistic cc means we just throw away the state.
	 */

	_state = null;
	
	return super.nestedAbort();
    }

    public int topLevelPrepare ()
    {
        if (txojLogger.logger.isTraceEnabled())
        {
            txojLogger.logger.trace("LockRecord::nestedPrepare() for "+order());
        }
        
	if (value() == null)
            return TwoPhaseOutcome.PREPARE_NOTOK;
	
	if (checkState())
	    return super.topLevelPrepare();
	else
	    return TwoPhaseOutcome.PREPARE_NOTOK;
    }
    
    public int topLevelCommit ()
    {       
        boolean stateOK = checkState();

        if ((super.topLevelCommit() == TwoPhaseOutcome.FINISH_OK) && stateOK)
            return TwoPhaseOutcome.FINISH_OK;
        else
            return TwoPhaseOutcome.FINISH_ERROR;
    }

    public String type ()
    {
	return "/StateManager/AbstractRecord/LockRecord/OptimisticLockRecord";
    }
    
    public String toString ()
    {
        return _myUid.stringForm();
    }
    
    protected OptimisticLockRecord ()
    {
	super();
    }

    private boolean checkState ()
    {
        if ((_status == ObjectStatus.ACTIVE_NEW) || (!_check))
            return true;

        if (_state == null)
            return false;
        
        /*
         * If the object is recoverable then we can just check the local state.
         * If the object is persistent then we have to check the state on disk.
         */
        
        OutputObjectState tempState = new OutputObjectState();
        OptimisticLockManager man = (OptimisticLockManager) value();
        int objectType = man.objectType();
        
        synchronized (man)
        {
            /*
             * If we check the state now, it's possible that some other transactions may be
             * doing the same concurrently. We need to lock the object at this point. Or
             * suffer heuristic by checking during commit - though this still leaves a window
             * of vulnerability.
             */
            
            /*
             * Assume initially that this will only work if the objects are all in the same
             * address space, since sharing across spaces will impose performance overhead
             * anyway. In that case, we can maintain a list of all objects that are being
             * managed optimistically and check them directly as well as lock them.
             * 
             * Problem is that it's the state that needs to be checked and there may be
             * multiple instances of the state active in memory at the same time. So would
             * need to keep each instance per Uid.
             */
            
            /*
             * Could even make this specific to STM and in which case we have even more control.
             */
    
            if (objectType == ObjectType.RECOVERABLE)
            {
                if (man.save_state(tempState, objectType))
                {
                    boolean identical = true;
                    
                    if (tempState.length() == _state.length())
                    {
                        for (int i = 0; (i < tempState.length()) && identical; i++)
                        {   
                            if (tempState.buffer()[i] != _state.buffer()[i])
                                identical = false;
                        }
                        
                        if (identical)
                            return true;
                    }
                }
            }
            
            if (objectType == ObjectType.ANDPERSISTENT)
            {
                /*
                 * Don't need the state - could just check the time of the file update if we are using
                 * a file based object store.
                 */
                
                try
                {
                    InputObjectState s = man.getStore().read_committed(man.get_uid(), man.type());

                    if (s != null)
                    {
                        boolean identical = true;

                        if (s.length() == _state.length())
                        {
                            for (int i = 0; (i < s.length()) && identical; i++)
                            {
                                if (s.buffer()[i] != _state.buffer()[i])
                                    identical = false;
                            }
                            
                            if (identical)
                                return true;
                        }
                    }
                    else
                    {
                        return false;
                    }
                }
                catch (final Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        
        return false;
    }

    public boolean shouldReplace (AbstractRecord ar)
    {
        if (!super.shouldReplace(ar))
        {
            if ((order().equals(ar.order())) && typeIs() == ar.typeIs())
            {
                if (!((OptimisticLockRecord) ar)._check && _check)
                    return true;
            }
            
            return false;
        }
        else
            return true;
    }
    
    private InputObjectState _state = new InputObjectState();
    private int _status = ObjectStatus.ACTIVE_NEW;
    private boolean _check = true;
    private Uid _myUid = new Uid();
}
