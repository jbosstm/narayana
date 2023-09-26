/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.heuristics;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.Current;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.Status;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.ats.jts.utils.Utility;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.hp.mwtests.ts.jts.orbspecific.resources.AtomicResource;
import com.hp.mwtests.ts.jts.orbspecific.resources.heuristic;
import com.hp.mwtests.ts.jts.utils.ResourceTrace;

public class HeuristicTest
{
    @Test
    public void test() throws Exception
    {
        boolean shouldCommit = true;
        boolean heuristicPrepare = false;

        Coordinator coord = null;

        ORB myORB = null;
        RootOA myOA = null;
        heuristic hImpl = null;

        try
        {
            myORB = ORB.getInstance("test");
            myOA = OA.getRootOA(myORB);

            myORB.initORB(new String[] {}, null);
            myOA.initOA();

            ORBManager.setORB(myORB);
            ORBManager.setPOA(myOA);

            Current current = OTSManager.get_current();
            hImpl = new heuristic(heuristicPrepare);
            Resource heuristicObject = hImpl.getReference();
            AtomicResource aImpl = new AtomicResource(shouldCommit);
            Resource atomicObject = aImpl.getReference();

            System.out.println("beginning top-level transaction.");

            current.begin();

            Control myControl = current.get_control();

            assertNotNull( myControl );

            System.out.println("getting coordinator");

            coord = myControl.get_coordinator();

            myControl = null;

            System.out.println("registering resources.");

            try
            {
                coord.register_resource(heuristicObject);
                coord.register_resource(atomicObject);
            }
            catch (Exception ex)
            {
                fail("Failed to register resources: "+ex);
                ex.printStackTrace(System.err);
            }

            System.out.println("committing top-level transaction.");

            current.commit(true);
        }
        catch (TRANSACTION_ROLLEDBACK  e1)
        {
            System.out.println("\nTransaction RolledBack exception");
        }
        catch (HeuristicMixed e2)
        {
            System.out.println("\nTransaction HeuristicMixed exception");
        }
        catch (HeuristicHazard e3)
        {
            System.out.println("\nTransaction HeuristicHazard exception");
        }
        catch (Exception e4)
        {
            fail("Caught unexpected exception: "+e4);
            e4.printStackTrace(System.err);
        }

        System.out.println("Trying to determing final transaction outcome.");

        org.omg.CosTransactions.Status status = Status.StatusUnknown;

        try
        {
            if (coord != null)
            {
                status = coord.get_status();

                coord = null;
            }
            else
            {
                fail("\nCould not determine action status.");
            }
        }
        catch (SystemException ex1)
        {
            // assume invalid reference - tx may have been garbage collected
        }
        catch (Exception e5)
        {
            fail("Caught unexpected exception:" +e5);
            e5.printStackTrace(System.err);
        }

        System.out.println("\nFinal action status: "+Utility.stringStatus(status));

        System.out.println("Test completed successfully.");

        ResourceTrace trace = hImpl.getTrace();

        if ( (!heuristicPrepare) && (shouldCommit) && (trace.getTrace() == ResourceTrace.ResourceTracePrepareCommitHeurisiticRollbackForget) )
        {
            //assertSuccess();
        }
        else
        {
            if ( (!heuristicPrepare) && (!shouldCommit) && (trace.getTrace() == ResourceTrace.ResourceTracePrepareRollback) )
            {
                //assertSuccess();
            }
            else
            {
                if ( (heuristicPrepare) && (shouldCommit) && (trace.getTrace() == ResourceTrace.ResourceTracePrepareHeuristicHazardForget) )
                {
                    //assertSuccess();
                }
                else
                {
                    fail();
                }
            }
        }

        myOA.destroy();
        myORB.shutdown();
    }
}