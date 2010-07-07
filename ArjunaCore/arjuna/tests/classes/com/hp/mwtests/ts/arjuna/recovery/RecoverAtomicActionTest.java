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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoveryManagerTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.recovery;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoverAtomicAction;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

import org.junit.Test;
import static org.junit.Assert.*;

public class RecoverAtomicActionTest
{
    @Test
    public void test ()
    {
        RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
        OutputObjectState fluff = new OutputObjectState();
        Uid kungfuTx = new Uid();
        boolean passed = false;
        final String tn = new AtomicAction().type();

        try
        {
            UidHelper.packInto(kungfuTx, fluff);

            System.err.println("Creating dummy log");

            recoveryStore.write_committed(kungfuTx, tn, fluff);

            if (recoveryStore.currentState(kungfuTx, tn) == StateStatus.OS_COMMITTED)
            {
                System.err.println("Wrote dummy transaction " + kungfuTx);

                RecoverAtomicAction rAA = new RecoverAtomicAction(kungfuTx, ActionStatus.COMMITTED);
                
                // activate should fail!
                
                if (!rAA.activate())
                {
                    rAA.replayPhase2();
                    
                    // state should have been moved
                    
                    if (recoveryStore.currentState(kungfuTx, tn) == StateStatus.OS_UNKNOWN)
                        passed = true;
                }
            }
            else
                System.err.println("State is not committed!");
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
        }

        assertTrue(passed);
    }
}
