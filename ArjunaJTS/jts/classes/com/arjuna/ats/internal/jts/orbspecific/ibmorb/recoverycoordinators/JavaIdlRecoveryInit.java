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
 * Copyright (C) 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: JacOrbRecoveryInit.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific.ibmorb.recoverycoordinators;

import com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCreator;
import com.arjuna.ats.internal.jts.recovery.recoverycoordinators.RcvCoManager;
import com.arjuna.ats.jts.logging.jtsLogger;

/**
 * Initialises Java IDL orb RecoveryCoordinator IOR creation mechanism
 *
 * An instance of this class is constructed by RecoveryEnablement and 
 * registered as an OAAttribute whose initialise method is called after
 * root POA is set up
 *
 * All orbs are likely to be the same, constructing a GenericRecoveryCreator,
 * but with an orb-specific manager
 *
 */

public class JavaIdlRecoveryInit
{
    public JavaIdlRecoveryInit()
    {
        RcvCoManager theManager = new JavaIdlRCManager();

        // and register it (which will cause creation of a GenericRecoveryCreator
        // and it's registration with CosTransactions)
        GenericRecoveryCreator.register(theManager);

        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("ibmorb RecoveryCoordinator creator setup");
        }
    }

}


