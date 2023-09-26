/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.resources;

import com.arjuna.ats.arjuna.ObjectStatus;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

public class ExtendedObject extends StateManager
{

    public ExtendedObject()
    {
        super(ObjectType.ANDPERSISTENT);

        state = 0;

        lock();
        
        activate();
        modified();
        deactivate();
        
        setStatus(status());
        
        unlock();
    }

    public ExtendedObject(Uid u)
    {
        super(u, ObjectType.ANDPERSISTENT);

        state = -1;
    }
    
    public void set_status ()
    {
        super.setStatus(ObjectStatus.PASSIVE);
    }
    
    public boolean lock ()
    {
        return super.tryLockMutex();
    }
    
    public boolean unlock ()
    {
        return super.unlockMutex();
    }

    public boolean remember (BasicAction act)
    {
        return super.rememberAction(act, RecordType.ACTIVATION, ObjectStatus.ACTIVE);
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
        if (activate())
            return state;
        else
            return -1;
    }

    public String type()
    {
        return super.type()+"/BasicObject";
    }

    public void toggle ()
    {
        super.disable();
        super.persist();
    }
    
    public void terminate ()
    {
        super.terminate();
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