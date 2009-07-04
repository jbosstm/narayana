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
import com.arjuna.ats.arjuna.common.Environment;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.recovery.RecoveryEnvironment;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionScanner;

import org.junit.Test;
import static org.junit.Assert.*;

public class LogMoveTest
{
    @Test
    public void test()
    {
        ObjectStore os = TxControl.getStore();
        OutputObjectState fluff = new OutputObjectState();
        Uid kungfuTx = new Uid();
        boolean passed = false;
        final String tn = new AtomicAction().type();

        try {
            kungfuTx.pack(fluff);

            System.err.println("Creating dummy log");

            os.write_committed(kungfuTx, tn, fluff);

            if (os.currentState(kungfuTx, tn) == ObjectStore.OS_COMMITTED) {
                System.err.println("Wrote dummy transaction " + kungfuTx);

                // quicker to deal with scanner directly
                
                ExpiredTransactionScanner scanner = new ExpiredTransactionScanner(tn, "/StateManager/ExpiredEntries");
                
                scanner.scan();
                
                scanner.scan();

                if (os.currentState(kungfuTx, tn) == ObjectStore.OS_COMMITTED)
                    System.err.println("Transaction log not moved!");
                else {
                    System.err.println("Transaction log moved!");

                    passed = true;
                }
            } else
                System.err.println("State is not committed!");
        }
        catch (final Exception ex) {
            ex.printStackTrace();
        }

        assertTrue(passed);
    }
}
