/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.tools;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

public class TransactionMonitor
{
    
public static void main (String[] args)
    {
        String root = null;

        for (int i = 0; i < args.length; i++)
        {
            if (args[i].compareTo("-help") == 0)
            {
                usage();
                System.exit(0);
            }
            else
            {
                if (args[i].compareTo("-root") == 0)
                {
                    root = args[i+1];
                    i++;
                }
                else
                {
                    System.out.println("Unknown option "+args[i]);
                    usage();

                    System.exit(0);
                }
            }
        }

        /* Determine transaction (BasicAction) type name */
        BasicAction ba = new BasicAction();
        String baType = ba.type();
        if (baType.charAt(0) == '/') 
                baType = baType.substring(1);
        try
        {
            RecoveryStore recoveryStore = StoreManager.getRecoveryStore();

            InputObjectState types = new InputObjectState();

            if (recoveryStore.allTypes(types))
            {
                String theName = null;
                int count = 0;

                try
                {
                    boolean endOfList = false;

                    while (!endOfList)
                    {
                        theName = types.unpackString();

                        if (theName.compareTo("") == 0)
                            endOfList = true;
                        else if (theName.startsWith(baType))
                        {
                            count++;
            
                            System.out.println(count+": "+theName);

                            InputObjectState uids = new InputObjectState();

                            if (recoveryStore.allObjUids(theName, uids))
                            {
                                Uid theUid = new Uid(Uid.nullUid());

                                try
                                {
                                    boolean endOfUids = false;
                                    
                                    while (!endOfUids)
                                    {
                                        theUid = UidHelper.unpackFrom(uids);

                                        if (theUid.equals(Uid.nullUid()))
                                            endOfUids = true;
                                        else
                                        {
                                            System.out.print("\t"+theUid+" state is ");
                                            System.out.print(StateStatus.stateStatusString(recoveryStore.currentState(theUid, theName)));
                                            System.out.println();
                                        }
                                    }
                                }
                                catch (Exception e)
                                {
                                    // end of uids!
                                }
                            }

                            System.out.println();
                        }
                    }
                }
                catch (Exception e)
                {
                    System.err.println(e);
                    
                    // end of list!
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Caught unexpected exception: "+e);
        }
    }

private static void usage ()
    {
        System.out.println("Usage: TransactionMonitor [-root <store root>] [-help]");
    }
 
};