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
package com.hp.mwtests.ts.jta.jts.recovery;

import java.util.Arrays;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.recovery.ExpiryScanner;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryModule;

public class ExpiryScannerStartupUnitTest {
    @Test
    public void testExpiryBackground() throws Exception {
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryModuleClassNames(Arrays.asList(new String[] { XARecoveryModule.class.getName() }));
        recoveryPropertyManager.getRecoveryEnvironmentBean().setExpiryScanners(Arrays.asList(new ExpiryScanner[] { new ExpiryScanner() {

            @Override
            public void scan() {
                AbstractRecord.create(172);
            }

            @Override
            public boolean toBeUsed() {
                return true;
            }
        } }));

        recoveryPropertyManager.getRecoveryEnvironmentBean().setPeriodicRecoveryPeriod(1);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);

        RecoveryManager rm = RecoveryManager.manager();

        rm.terminate(false);
    }
}
