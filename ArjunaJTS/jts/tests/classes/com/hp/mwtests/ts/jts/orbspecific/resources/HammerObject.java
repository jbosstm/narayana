/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.resources;

import java.io.IOException;

import org.omg.CORBA.IntHolder;
import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.Control;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.jts.ExplicitInterposition;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;

public class HammerObject extends LockManager implements com.hp.mwtests.ts.jts.TestModule.HammerOperations
{
    
    public HammerObject ()
    {
	super(ObjectType.ANDPERSISTENT);

	CurrentImple current = OTSImpleManager.current();

	_value = 0;

	try
	{
	    current.begin();

	    if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
	    {
		_value = 0;
		current.commit(true);
	    }
	    else
		current.rollback();
	}
	catch (Exception e)
	{
	    System.err.println("HammerObject: "+e);
	}
    }

    public HammerObject (Uid u)
    {
	super(u);
    }

    public void finalize () throws Throwable
    {
	super.terminate();
	super.finalize();
    }

    public boolean incr (int value, Control control) throws SystemException
    {
	boolean res = false;
	ExplicitInterposition inter = new ExplicitInterposition();

	try
	{
	    inter.registerTransaction(control);
	}
	catch (Exception e)
	{
	    System.err.println("WARNING HammerObject.incr - could not do interposition");
	    return false;
	}

	CurrentImple current = OTSImpleManager.current();

	try
	{
	    current.begin();

	    if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
	    {
		_value = _value + value;

		current.commit(true);
		res = true;
	    }
	    else
		current.rollback();
	}
	catch (Exception e)
	{
	    System.err.println("HammerObject.incr: "+e);
	    
	    res = false;
	}

	inter.unregisterTransaction();
	
	return res;
    }

    public boolean set (int value, Control control) throws SystemException 
    {
	boolean res = false;
	ExplicitInterposition inter = new ExplicitInterposition();

	try
	{
	    inter.registerTransaction(control);
	}
	catch (Exception e)
	{
	    System.err.println("WARNING HammerObject.set - could not do interposition");
	    return false;
	}

	CurrentImple current = OTSImpleManager.current();    

	try
	{
	    current.begin();

	    if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
	    {
		_value = value;

		current.commit(true);
		res = true;
	    }
	    else
		current.rollback();
	}
	catch (Exception e)
	{
	    System.err.println("HammerObject.set: "+e);

	    res = false;
	}

	inter.unregisterTransaction();

	return res;
    }

    public boolean get (IntHolder value, Control control) throws SystemException
    {
	boolean res = false;
	ExplicitInterposition inter = new ExplicitInterposition();

	try
	{
	    inter.registerTransaction(control);
	}
	catch (Exception e)
	{
	    System.err.println("WARNING HammerObject.incr - could not do interposition");
	    return false;
	}

	CurrentImple current = OTSImpleManager.current();

	try
	{
	    current.begin();

	    if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
	    {
		value.value = _value;

		current.commit(true);
		res = true;
	    }
	    else
		current.rollback();
	}
	catch (Exception e)
	{
	    System.err.println("HammerObject.get: "+e);
	    
	    res = false;
	}

	inter.unregisterTransaction();
	
	return res;
    }

    public boolean save_state (OutputObjectState os, int ot)
    {
	if (!super.save_state(os, ot))
	    return false;
	
	try
	{
	    os.packInt(_value);

	    return true;
	}
	catch (IOException e)
	{
	    return false;
	}
    }

    public boolean restore_state (InputObjectState os, int ot)
    {
	if (!super.restore_state(os, ot))
	    return false;
	
	try
	{
	    _value = os.unpackInt();

	    return true;
	}
	catch (IOException e)
	{
	    return false;
	}
    }

    public String type ()
    {
	return "/StateManager/LockManager/HammerObject";
    }

    private int _value;
 
}