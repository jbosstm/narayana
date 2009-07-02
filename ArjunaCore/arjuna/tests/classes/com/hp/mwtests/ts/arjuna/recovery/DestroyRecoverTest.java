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
 * $Id: DestroyRecoverTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.recovery;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.*;

import com.hp.mwtests.ts.arjuna.resources.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class DestroyRecoverTest
{
    @Test
    public void test()
    {
        AtomicAction A = new AtomicAction();
        BasicObject bo = null;
        Uid txId = null;
        Uid objId = null;
        boolean passed = true;

        com.arjuna.ats.arjuna.common.Configuration.setAlternativeOrdering(true);

        try {
            A.begin();

            bo = new BasicObject();
            objId = bo.get_uid();

            A.removeThread();

            A.commit();
        }
        catch (Exception ex) {
            ex.printStackTrace();

            passed = false;
        }

        if (passed) {
            try {
                A = new AtomicAction();

                txId = A.get_uid();

                A.begin();

                bo.activate();

                bo.destroy();

                A.add(new BasicCrashRecord());

                A.removeThread();

                A.commit();
            }
            catch (com.arjuna.ats.arjuna.exceptions.FatalError ex) {
                // ignore
            }
            catch (Exception ex) {
                ex.printStackTrace();

                passed = false;
            }
        }

        if (passed) {
            try {
                passed = false;

                RecoveryTransaction tx = new RecoveryTransaction(txId);

                tx.doCommit();

                BasicObject recoveredObject = new BasicObject(objId);

                if (recoveredObject.get() == -1)
                    passed = true;
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        assertTrue(passed);
    }

}
