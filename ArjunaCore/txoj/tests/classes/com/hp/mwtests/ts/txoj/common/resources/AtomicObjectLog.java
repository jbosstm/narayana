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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: AtomicObjectLog.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.txoj.common.resources;

import java.io.IOException;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.coordinator.AppendLogTransaction;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;
import com.hp.mwtests.ts.txoj.common.exceptions.TestException;

public class AtomicObjectLog extends LockManager
{

    public AtomicObjectLog()
    {
        super(ObjectType.ANDPERSISTENT);

        state = 0;

        act = new AppendLogTransaction();

        act.begin();

        if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
        {
            if (act.commit() == ActionStatus.COMMITTED)
            {
                // System.out.println("Created persistent object " + get_uid());
            }
            else
                System.out.println("Action.commit error.");
        }
        else
        {
            act.abort();

            System.out.println("setlock error.");
        }

        String debug = System.getProperty("DEBUG", null);

        if (debug != null)
            printDebug = true;
    }

    public AtomicObjectLog(Uid u)
    {
        super(u);

        state = -1;

        AtomicAction A = new AtomicAction();

        A.begin();

        if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
        {
            System.out.println("Recreated object " + u);
            A.commit();
        }
        else
        {
            System.out.println("Error recreating object " + u);
            A.abort();
        }

        String debug = System.getProperty("DEBUG", null);

        if (debug != null)
            printDebug = true;
    }

    public void finalize () throws Throwable
    {
        super.terminate();
        super.finalize();
    }

    public AppendLogTransaction getTransaction ()
    {
        return (AppendLogTransaction) act;
    }

    public void incr (int value) throws TestException
    {
        AtomicAction A = new AtomicAction();

        A.begin();

        if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
        {
            state += value;

            if (A.commit() != ActionStatus.COMMITTED)
                throw new TestException("Action commit error.");
            else
                return;
        }
        else
        {
            if (printDebug)
                System.out.println("Error - could not set write lock.");
        }

        A.abort();

        throw new TestException("Write lock error.");
    }

    public void set (int value) throws TestException
    {
        AtomicAction A = new AtomicAction();

        A.begin();

        if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
        {
            state = value;

            if (A.commit() != ActionStatus.COMMITTED)
                throw new TestException("Action commit error.");
            else
                return;
        }
        else
        {
            if (printDebug)
                System.out.println("Error - could not set write lock.");
        }

        A.abort();

        throw new TestException("Write lock error.");
    }

    public int get () throws TestException
    {
        AtomicAction A = new AtomicAction();
        int value = -1;

        A.begin();

        if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
        {
            value = state;

            if (A.commit() == ActionStatus.COMMITTED)
                return value;
            else
                throw new TestException("Action commit error.");
        }
        else
        {
            if (printDebug)
                System.out.println("Error - could not set read lock.");
        }

        A.abort();

        throw new TestException("Read lock error.");
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
        return "/StateManager/LockManager/AtomicObjectLog";
    }

    private int state;

    private boolean printDebug;

    private AtomicAction act;

};
