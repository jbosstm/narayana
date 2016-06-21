/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jbossts.qa.Utils;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.txoj.LockManager;

public class BaseTestCase
{
	public BaseTestCase()
	{
		super();
	}

	public BaseTestCase(String[] args)
	{
		super();
		mArgs = args;
		mArgsSize = args.length;
	}

	public void Fail()
	{
		Fail(null);
	}

	public void Fail(String s)
	{
		Fail(s, null);
	}

	public void Fail(String s, Exception e)
	{
		mComplete = false;
		if (e == null)
		{
			Debug(s);
		}
		else
		{
			Debug(s + ": " + e);
			e.printStackTrace();
		}
		Complete();
		System.exit(0);
	}

	public void Debug(String s)
	{
		if (s != null)
		{
			System.err.println(s);
		}
	}

	public void Debug(String s, Exception e)
	{
		if (s != null && e != null)
		{
			System.err.println(s + " :" + e);
			e.printStackTrace();
		}
		else
		{
			Debug(s);
		}
	}

	public void Pass()
	{
		mComplete = true;
		Complete();
	}

	public void qaAssert(boolean test)
	{
		if (test)
		{
			Pass();
		}
		else
		{
			Fail();
		}
	}

	private void Complete()
	{
		if (mComplete)
		{
			System.out.println("Passed");
		}
		else
		{
			System.out.println("Failed");
		}
	}

	public String[] getArgs()
	{
		return mArgs;
	}

	public int getNumberOfArgs()
	{
		return mArgsSize;
	}

	public String getArg(int position)
	{
		return mArgs[position];
	}

	public String getRelativeArg(int position)
	{
		return mArgs[mArgs.length - position];
	}

	public int getIntRelArg(int position)
	{
		return Integer.parseInt(getRelativeArg(position));
	}

	public String loadIOR(int relative_position)
	{
		String s = null;
		try
		{
			s = ServerIORStore.loadIOR(getRelativeArg(relative_position));
		}
		catch (Exception e)
		{
			Fail("Error in loading IOR: ", e);
		}
		return s;
	}

	public void storeIOR(int relative_position, String ior)
	{
		try
		{
			ServerIORStore.storeIOR(getRelativeArg(relative_position), ior);
		}
		catch (Exception e)
		{
			Fail("Error in loading IOR: ", e);
		}
	}

	public void storeIOR(String name, String ior)
	{
		try
		{
			ServerIORStore.storeIOR(name, ior);
		}
		catch (Exception e)
		{
			Fail("Error in loading IOR: ", e);
		}
	}

	public void storeUid(int relative_position, Object o)
	{
		try
		{
			LockManager lm = (LockManager) o;
			ObjectUidStore.storeUid(getRelativeArg(relative_position), lm.get_uid());
		}
		catch (Exception e)
		{
			Fail("Error storing Uid:", e);
		}
	}

	public Uid loadUid(int relative_position)
	{
		Uid u = null;
		try
		{
			u = ObjectUidStore.loadUid(getRelativeArg(relative_position));
		}
		catch (Exception e)
		{
			Fail("Error loading Uid:", e);
		}
		return u;
	}

	/**
	 * This method reads in a paramater from the comand line in a set position
	 * (relative_position) and sets the global variable.
	 */
	public void setNumberOfCalls(int relative_position)
	{
		numberOfCalls = getIntRelArg(relative_position);
	}

	public int numberOfCalls = 1000; //default value for tests
	private boolean mComplete = false;
	private String[] mArgs = null;
	private int mArgsSize = 0;

	public boolean addAction(AbstractRecord ar)
	{
		return (BasicAction.Current().add(ar) == AddOutcome.AR_ADDED);
	}

	//memory test metthods and variables used by JTS and Core tests
	public void getClientThreshold(int relative_position)
	{
		try
		{
			// If no threshold value then use default.
			if (MemoryTestProfileStore.getNoThresholdValue().equals(getRelativeArg(relative_position)))
			{
				clientIncreaseThreshold = Float.parseFloat(MemoryTestProfileStore.getDefaultClientIncreaseThreshold());
			}
			else // Use passed threshold
			{
				clientIncreaseThreshold = Float.parseFloat(getRelativeArg(relative_position));
			}
		}
		catch (Exception e)
		{
			Fail("Error whilst reading client values: ", e);
		}
	}

	//baseline values
	public int clientMemory0 = 0;
	//there can only be 1 client (this)
	public int clientMemory1 = 0;
	public float clientIncreaseThreshold = Float.valueOf("0.025").floatValue();//default value
}
