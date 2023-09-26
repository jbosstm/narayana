/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.recovery;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.hp.mwtests.ts.arjuna.resources.BasicObject;

public class DestroyRecoverTest
{
    @Test
    public void test()
    {
        arjPropertyManager.getCoordinatorEnvironmentBean().setAlternativeRecordOrdering(true);
        
        AtomicAction A = new AtomicAction();
        BasicObject bo = null;
        Uid txId = null;
        Uid objId = null;
        boolean passed = true;

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

                /*
                 * Committing the recovered transaction should have disposed of the
                 * user object, meaning activation will fail. Which for this test
                 * is a successful outcome!
                 */
                
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