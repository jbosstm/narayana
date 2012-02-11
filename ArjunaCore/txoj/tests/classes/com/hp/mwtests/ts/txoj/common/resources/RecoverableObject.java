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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: RecoverableObject.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.txoj.common.resources;

import java.io.IOException;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;

public class RecoverableObject extends LockManager
{

    public RecoverableObject()
    {
        AtomicAction A = new AtomicAction();

        A.begin();

        if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
        {
            state = 0;

            if (A.commit() == ActionStatus.COMMITTED)
                System.out.println("Created recoverable object " + get_uid());
            else
                System.out.println("Action.commit error.");
        }
        else
        {
            A.abort();

            System.out.println("setlock error.");
        }
    }

    public void finalize ()
    {
        super.terminate();

        try
        {
            super.finalize();
        }
        catch (Throwable e)
        {
        }
    }

    public boolean set (int value)
    {
        AtomicAction A = new AtomicAction();

        A.begin();

        if (setlock(new Lock(LockMode.WRITE)) == LockResult.GRANTED)
        {
            state = value;

            if (A.commit() == ActionStatus.COMMITTED)
                return true;
            else
                return false;
        }

        A.abort();

        return false;
    }

    public int get ()
    {
        AtomicAction A = new AtomicAction();
        int value = -1;

        A.begin();

        if (setlock(new Lock(LockMode.READ)) == LockResult.GRANTED)
        {
            value = state;

            if (A.commit() == ActionStatus.COMMITTED)
                return value;
            else
                return -1;
        }

        A.abort();

        return -1;
    }

    public boolean save_state (OutputObjectState os, int ot)
    {
        boolean result = super.save_state(os, ot);

        if (!result)
            return false;

        try
        {
            os.packInt(state);
        }
        catch (IOException e)
        {
            result = false;
        }

        return result;
    }

    public boolean restore_state (InputObjectState os, int ot)
    {
        boolean result = super.restore_state(os, ot);

        if (!result)
            return false;

        try
        {
            state = os.unpackInt();
        }
        catch (IOException e)
        {
            result = false;
        }

        return result;
    }

    public String type ()
    {
        return "/StateManager/LockManager/RecoverableObject";
    }

    private int state;

}
