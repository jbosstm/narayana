/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * $Id: AtomicObject.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.txoj.*;

import com.hp.mwtests.ts.jts.exceptions.TestException;

import java.io.IOException;

public class AtomicObject extends LockManager
{

    public AtomicObject ()
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

		current.commit(false);
	    }
	    else
		current.rollback();
	}
	catch (Exception e)
	{
	    System.out.println("AtomicObject "+e);
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

	    if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
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
	    System.out.println(e);
	    e.printStackTrace();

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

	    if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
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
	    System.out.println(e);
	    e.printStackTrace();

	    res = false;
	}

	return res;
    }

    public synchronized int get () throws TestException
    {
	boolean res = false;
	CurrentImple current = OTSImpleManager.current();    
	int value = -1;

	try
	{
	    current.begin();

	    if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
	    {
		value = _value;

		current.commit(false);
		res = true;
	    }
	    else
		current.rollback();
	}
	catch (Exception e)
	{
	    System.out.println(e);
	    e.printStackTrace();

	    res = false;
	}

	if (!res)
	    throw new TestException();
	else
	    return value;
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

