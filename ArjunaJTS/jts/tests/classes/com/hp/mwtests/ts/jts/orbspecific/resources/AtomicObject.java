/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.resources;

import java.io.IOException;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;
import com.hp.mwtests.ts.jts.exceptions.TestException;
import org.jboss.logging.Logger;

public class AtomicObject extends LockManager
{
	public static final Logger logger = Logger.getLogger("AtomicObject");

    public AtomicObject ()
    {
	super(ObjectType.ANDPERSISTENT);

	CurrentImple current = OTSImpleManager.current();

	_value = 0;

	try
	{
	    current.begin();

	    if (setlock(new Lock(LockMode.WRITE), 5) == LockResult.GRANTED)
	    {
		_value = 0;

		current.commit(false);
	    }
	    else
		current.rollback();
	}
	catch (Exception e)
	{
	    logger.info("AtomicObject "+e);
	}
    }

    public AtomicObject (Uid u)
    {
	super(u);
    }

    public void finalize () throws Throwable
    {
	super.terminate();
	super.finalize();
    }

    public synchronized boolean incr (int value)
    {
	boolean res = false;
	CurrentImple current = OTSImpleManager.current();

	try
	{
	    current.begin();

	    if (setlock(new Lock(LockMode.WRITE), 5) == LockResult.GRANTED)
	    {
		_value = _value + value;

		current.commit(false);
		res = true;
	    }
	    else
		current.rollback();
	}
	catch (Exception e)
	{
	    logger.info(e);
	    logger.warn(e.getMessage(), e);;

	    res = false;
	}

	return res;
    }

    public synchronized boolean set (int value)
    {
	boolean res = false;
	CurrentImple current = OTSImpleManager.current();    

	try
	{
	    current.begin();

	    if (setlock(new Lock(LockMode.WRITE), 5) == LockResult.GRANTED)
	    {
		_value = value;

		current.commit(false);
		res = true;
	    }
	    else
		current.rollback();
	}
	catch (Exception e)
	{
	    logger.info(e);
	    logger.warn(e.getMessage(), e);

	    res = false;
	}

	return res;
    }

    public synchronized int get () throws TestException {
        CurrentImple current = OTSImpleManager.current();
        int value = -1;

        try {
            current.begin();

            if (setlock(new Lock(LockMode.READ), 5) == LockResult.GRANTED) {
                value = _value;
                current.commit(false);
                return value;
            } else {
                current.rollback();
                throw new TestException("Could not setlock");
            }
        } catch (Exception e) {
            throw new TestException(e);
        }
    }

    public boolean save_state (OutputObjectState os, int t)
    {
	boolean result = super.save_state(os, t);

	if (!result)
	    return false;

	try
	{
	    os.packInt(_value);
	}
	catch (IOException e)
	{
	    result = false;
	}

	return result;
    }

    public boolean restore_state (InputObjectState os, int t)
    {
	boolean result = super.restore_state(os, t);

	if (!result)
	    return false;

	try
	{
	    _value = os.unpackInt();
	}
	catch (IOException e)
	{
	    result = false;
	}

	return result;
    }

    public String type ()
    {
	return "/StateManager/LockManager/AtomicObject";
    }

    private int _value;

}