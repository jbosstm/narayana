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

package com.arjuna.ats.internal.arjuna.recovery ;

import java.util.*;

import com.arjuna.ats.arjuna.recovery.RecoveryActivator ;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.common.ClassloadingUtility;

/**
 * RecoveryActivators are dynamically loaded. The recoveryActivator to load
 * are specified by properties beginning with "recoveryActivator"
 * <P>
 * @author Malik Saheb
 * @since ArjunaTS 3.0
 */

public class RecActivatorLoader
{
    public RecActivatorLoader()
    {
        loadRecoveryActivators();
    }

    // These are loaded in list iteration order.
    private void loadRecoveryActivators ()
    {
        List<String> activatorNames = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryActivators();

        for(String activatorName : activatorNames) {
            RecoveryActivator recoveryActivator = ClassloadingUtility.loadAndInstantiateClass(RecoveryActivator.class, activatorName, null);
            if(recoveryActivator != null) {
                _recoveryActivators.add(recoveryActivator);
            }
        }
    }

    public void startRecoveryActivators() throws RuntimeException
    {
        tsLogger.i18NLogger.info_recovery_RecActivatorLoader_6();

        for(RecoveryActivator recoveryActivator : _recoveryActivators)
        {
            if(!recoveryActivator.startRCservice()) {
                throw new RuntimeException( tsLogger.i18NLogger.get_recovery_RecActivatorLoader_initfailed(recoveryActivator.getClass().getCanonicalName()));
            }
        }
    }

    // this refers to the recovery activators specified in the recovery manager
    // property file which are dynamically loaded.
    private final List<RecoveryActivator> _recoveryActivators = new ArrayList<RecoveryActivator>();
}









