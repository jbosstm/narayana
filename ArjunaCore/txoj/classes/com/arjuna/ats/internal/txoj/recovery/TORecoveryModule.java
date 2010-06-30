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
 * Copyright (C) 2000, 2001,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TORecoveryModule.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.txoj.recovery;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.txoj.logging.txojLogger;

import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;

import java.util.*;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

import java.io.IOException;

/**
 * This class is a plug-in module for the recovery manager. This class is
 * responsible for the recovery of Transactional Objects (aka AIT objects),
 * i.e., objects that derive from LockManager and StateManager.
 */

public class TORecoveryModule implements RecoveryModule
{

    /**
     * Create the module to scan in the default location for object states. Any
     * modifications to locations must occur in the properties file.
     */

    @SuppressWarnings("unchecked")
    public TORecoveryModule()
    {
        if (txojLogger.logger.isDebugEnabled()) {
            txojLogger.logger.debug("TORecoveryModule created");
        }

        /*
         * Where are TO's stored. Default.
         */
      
        try
        {
            Class osc = Class.forName(arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreType());

            _objectStore = (ObjectStore) osc.newInstance();
        }
        catch (final Throwable ex)
        {
            throw new FatalError(txojLogger.i18NLogger.get_recovery_TORecoveryModule_osproblem(), ex);
        }
    }

    public void periodicWorkFirstPass ()
    {
        txojLogger.i18NLogger.info_recovery_TORecoveryModule_3();

        // Build a hashtable of uncommitted transactional objects
        _uncommittedTOTable = new Hashtable();

        try
        {
            InputObjectState types = new InputObjectState();

            // find all the types of transactional object (in this ObjectStore)
            if (_objectStore.allTypes(types))
            {
                String theName = null;

                try
                {
                    boolean endOfList = false;

                    while (!endOfList)
                    {
                        // extract a type
                        theName = types.unpackString();

                        if (theName.compareTo("") == 0)
                            endOfList = true;
                        else
                        {
                            InputObjectState uids = new InputObjectState();

                            // find the uids of anything with an uncommitted
                            // entry in the object store
                            if (_objectStore.allObjUids(theName, uids,
                                    StateStatus.OS_UNCOMMITTED))
                            {
                                Uid theUid = null;

                                try
                                {
                                    boolean endOfUids = false;

                                    while (!endOfUids)
                                    {
                                        // extract a uid
                                        theUid = UidHelper.unpackFrom(uids);

                                        if (theUid.equals(Uid.nullUid()))
                                            endOfUids = true;
                                        else
                                        {
                                            String newTypeString = new String(
                                                    theName);
                                            Uid newUid = new Uid(theUid);
                                            
                                            _uncommittedTOTable.put(newUid,newTypeString);
                                            
                                            if (txojLogger.logger.isDebugEnabled()) {
                                                txojLogger.logger.debug("TO currently uncommitted "+newUid+" is a "+newTypeString);
                                            }
                                        }
                                    }
                                }
                                catch (Exception e)
                                {
                                    // end of uids!
                                }
                            }
                        }
                    }
                }
                catch (IOException ex)
                {
                    // nothing there.
                }
                catch (Exception e)
                {
                    txojLogger.i18NLogger.warn_recovery_TORecoveryModule_5(e);
                }
            }
        }
        catch (Exception e)
        {
            txojLogger.i18NLogger.warn_recovery_TORecoveryModule_5(e);
        }

    }

    public void periodicWorkSecondPass ()
    {
        txojLogger.i18NLogger.info_recovery_TORecoveryModule_6();

        Enumeration uncommittedObjects = _uncommittedTOTable.keys();

        while (uncommittedObjects.hasMoreElements())
        {
            Uid objUid = (Uid) uncommittedObjects.nextElement();
            String objType = (String) _uncommittedTOTable.get(objUid);

            try
            {
                if (_objectStore.currentState(objUid, objType) == StateStatus.OS_UNCOMMITTED)
                {
                    recoverObject(objUid, objType);
                }
                else
                {
                    if (txojLogger.logger.isDebugEnabled()) {
                        txojLogger.logger.debug("Object ("+objUid+", "+objType+") is no longer uncommitted.");
                    }
                }
            }
            catch (ObjectStoreException ose)
            {
                if (txojLogger.logger.isDebugEnabled()) {
                    txojLogger.logger.debug("Object ("+objUid+", "+objType+") no longer exists.");
                }
            }
        }
    }

    /**
     * Set-up routine.
     */

    protected void initialise ()
    {
        if (txojLogger.logger.isDebugEnabled()) {
            txojLogger.logger.debug("TORecoveryModule.initialise()");
        }
    }

    private final void recoverObject (Uid objUid, String objType)
    {
        if (txojLogger.logger.isDebugEnabled()) {
            txojLogger.logger.debug("TORecoveryModule.recoverObject(" + objUid + ", "
                    + objType + ")");
        }

        /*
         * Get a shell of the TO and find out which transaction it was that got
         * it uncommitted.
         */

        RecoveredTransactionalObject recoveredTO = new RecoveredTransactionalObject(
                objUid, objType, _objectStore);

        /*
         * Tell it to replayPhase2, in whatever way it does (in fact it won't do
         * anything unless it determines the transaction rolled back).
         */

        recoveredTO.replayPhase2();
    }

    private Hashtable _uncommittedTOTable;

    private static ObjectStore _objectStore = null;

}
