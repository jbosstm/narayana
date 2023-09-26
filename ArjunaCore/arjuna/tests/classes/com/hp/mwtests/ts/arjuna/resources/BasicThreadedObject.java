/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.resources;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.BasicAction;

public class BasicThreadedObject extends Thread
{

    public BasicThreadedObject(boolean start)
    {
        startAction = start;
        uid = new Uid();
    }

    public void run()
    {
        if (startAction) {
            BasicThreadedObject.A = new AtomicAction();

            System.out.println("BasicThreadedObject " + uid + " created action " + BasicThreadedObject.A.get_uid());

            BasicThreadedObject.A.begin();

            Thread.yield();
        } else {
            System.out.println("BasicThreadedObject " + uid + " adding to existing action");

            if (A != null)
                BasicThreadedObject.A.addThread();

            Thread.yield();
        }

        BasicAction act = BasicAction.Current();

        if (act != null)
            System.out.println("BasicThreadedObject " + uid + " current action " + act.get_uid());
        else
            System.out.println("BasicThreadedObject " + uid + " current action null");

        try {
            BasicThreadedObject.O.incr(4);

            Thread.yield();
        }
        catch (Exception e) {
        }

        if (startAction) {
            System.out.println("\nBasicThreadedObject " + uid + " committing action " + act.get_uid());
            BasicThreadedObject.A.commit();
            System.out.println("BasicThreadedObject " + uid + " action " + act.get_uid() + " committed\n");
        } else {
            System.out.println("\nBasicThreadedObject " + uid + " aborting action " + act.get_uid());
            BasicThreadedObject.A.abort();
            System.out.println("BasicThreadedObject " + uid + " action " + act.get_uid() + " aborted\n");
        }
    }

    private Uid uid;
    private boolean startAction;

    public static AtomicAction A = null;
    public static SimpleObject O = new SimpleObject();

};