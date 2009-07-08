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
 * $Id: HammerObject.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.*;

import com.arjuna.ats.txoj.*;

import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.internal.jts.OTSImpleManager;

import com.arjuna.ats.jts.ExplicitInterposition;

import org.omg.CORBA.IntHolder;

import org.omg.CosTransactions.*;

import org.omg.CORBA.SystemException;

import java.io.IOException;

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

