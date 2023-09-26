/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.resources;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

public class BasicObject extends StateManager
{

    public BasicObject()
    {
        super(ObjectType.ANDPERSISTENT);

        state = 0;

        System.out.println("Created basic object.");

        activate();
        modified();
        deactivate();
    }

    public BasicObject(Uid u)
    {
        super(u, ObjectType.ANDPERSISTENT);

        state = -1;

        activate();
    }

    public void incr(int value)
    {
        modified();

        state += value;
    }

    public void set(int value)
    {
        modified();

        state = value;
    }

    public int get()
    {
        try
        {
            lockMutex();
            
            if (activate())
                return state;
            else
                return -1;
        }
        finally
        {
            getMutex().unlock();
        }
    }

    public String type()
    {
        return super.type()+"/BasicObject";
    }

    public boolean deactivate()
    {
        return super.deactivate();
    }

    public boolean activate()
    {
        return super.activate();
    }

    public boolean save_state(OutputObjectState os, int type)
    {
        try {
            os.packInt(state);
            os.packBytes(moreState);
        }
        catch (Exception ex) {
            return false;
        }

        return super.save_state(os, type);
    }

    public boolean restore_state(InputObjectState os, int type)
    {
        try {
            state = -1;
            moreState = null;

            state = os.unpackInt();
            moreState = os.unpackBytes();

            if ((moreState[0] == 'a') && (moreState[1] == 'b')
                    && (moreState[2] == 'c') && (moreState[3] == 'd')) {
                // ok
            } else
                return false;
        }
        catch (Exception ex) {
            return false;
        }

        return super.restore_state(os, type);
    }

    private int state;
    private byte[] moreState = {'a', 'b', 'c', 'd'};
}