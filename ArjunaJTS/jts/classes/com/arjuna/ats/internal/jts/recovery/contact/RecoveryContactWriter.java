/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * Copyright (C) 2001
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoveryContactWriter.java 2342 2006-03-30 13:06:17Z  $
 *
 */

package com.arjuna.ats.internal.jts.recovery.contact;

import com.arjuna.ats.jts.logging.*;

import com.arjuna.ArjunaOTS.*;

import com.arjuna.orbportability.event.*;


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




