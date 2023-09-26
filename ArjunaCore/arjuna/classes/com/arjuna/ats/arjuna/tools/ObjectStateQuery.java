/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.tools;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

public class ObjectStateQuery
{

    @SuppressWarnings("unchecked")
    public static void main (String[] args)
    {
        String uid = null;
        String type = null;

        for (int i = 0; i < args.length; i++)
        {
            if (args[i].compareTo("-help") == 0)
            {
                usage();
                System.exit(0);
            }
            else
            {
                if (args[i].compareTo("-uid") == 0)
                {
                    uid = args[i + 1];
                    i++;
                }
                else
                {
                    if (args[i].compareTo("-type") == 0)
                    {
                        type = args[i + 1];
                        i++;
                    }
                    else
                    {
                        System.out.println("Unknown option " + args[i]);
                        usage();

                        System.exit(0);
                    }
                }
            }
        }

        try
        {
            RecoveryStore recoveryStore = StoreManager.getRecoveryStore();

            System.out.println("Status is "
                    + recoveryStore.currentState(new Uid(uid), type));

            InputObjectState buff = new InputObjectState();

            recoveryStore.allObjUids(type, buff, StateStatus.OS_UNCOMMITTED);

            Uid u = UidHelper.unpackFrom(buff);
            
            System.out.println("got "+u);
        }
        catch (Exception e)
        {
            System.err.println("Caught unexpected exception: "+e);
        }
    }

    private static void usage ()
    {
        System.out
                .println("Usage: ObjectStateQuery [-uid <state id>] [-type <state type>] [-help]");
    }

}