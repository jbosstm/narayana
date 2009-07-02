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
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: CallbackRecoveryTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.recovery;

import com.arjuna.ats.arjuna.recovery.*;

import org.junit.Test;
import static org.junit.Assert.*;

class RecoveryScanImple implements RecoveryScan
{
    public void completed()
    {
        passed = true;
    }

    public boolean passed = false;

}

public class CallbackRecoveryTest
{
    @Test
    public void test()
    {
        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        DummyRecoveryModule module = new DummyRecoveryModule();
        RecoveryScanImple rs = new RecoveryScanImple();

        manager.addModule(module);

        manager.scan(rs);

        try {
            Thread.currentThread().sleep(120000);
        }
        catch (Exception ex) {
        }
        
        assertTrue(module.finished());
        assertTrue(rs.passed);
    }
}
