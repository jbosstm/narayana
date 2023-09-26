/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.recovery.contact;

import com.arjuna.ArjunaOTS.ArjunaFactory;
import com.arjuna.ArjunaOTS.ArjunaFactoryHelper;
import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.orbportability.event.EventManager;


/**
 * An instance of this object is registered so as to be invoked when any 
 * CORBA object is constructed, but ignores everything except the first ArjunaFactory
 *
 * On seeing an ArjunaFactory, it causes a FactoryContactItem to be saved and
 * deregisters itself
 * (and thus relies on the fact (true for 2.1) that any ArjunaFactory can be used to 
 * find the status of any transaction.
 */

public class RecoveryContactWriter implements com.arjuna.orbportability.event.EventHandler
{
    private boolean _noted;

    public RecoveryContactWriter()
    {
        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("RecoveryContactWriter() created");
        }

        _noted = false;
    }

    public void connected (org.omg.CORBA.Object obj)
    {    
        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("RecoveryContactWriter.connected("+obj+")");
        }

        // only do this once - but shouldn't need this, since de-register on writing

        if (_noted) {
            return;
        }
        try {
            ArjunaFactory theFactory = ArjunaFactoryHelper.narrow(obj);

            if (theFactory != null) {
                // if that didn't blow, we have a hit
                if (jtsLogger.logger.isDebugEnabled()) {
                    jtsLogger.logger.debug("RecoveryContactWriter.connected - found ArjunaFactory");
                }

                FactoryContactItem.createAndSave(theFactory);
                // we've done our work, so set the flag and try to remove ourselves
                _noted = true;
                EventManager.getManager().removeHandler(this);
            }
        } catch ( Exception ex) {
            // oh well - it probably wasn't ours
        }
    }

    public void disconnected (org.omg.CORBA.Object obj)
    {
        // nothing to be done
    }

    public String name ()
    {
        return "RecoveryContactEventHandler";
    }
}