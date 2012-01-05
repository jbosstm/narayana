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
/*
 * Created by IntelliJ IDEA.
 * User: peter craddock
 * Date: 12-Mar-02
 * Time: 14:19:06
 */
package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl.ErrorService01;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;

import java.util.ArrayList;

/**
 * This is a template to test the current outcome is
 * from the tests. we will use this to create a config file generator.
 */
public class ErrorClient04 extends BaseTestClient
{
	public static void main(String[] args)
	{
		ErrorClient04 test = new ErrorClient04(args);
	}

	private ErrorClient04(String[] args)
	{
		super(args);
	}

	public void Test()
	{
		/////////////////////////////////////////////////////////////////////////////////////////
		// set up values needed for test
		/////////////////////////////////////////////////////////////////////////////////////////
		//setup values
		mMaxInt = 12;
		mMaxValue = 11;
		try
		{
			setNumberOfResources(getNumberOfArgs());
			setCrashPoint(getNumberOfArgs() - 1);

			createArrayList();
		}
		catch (Exception e)
		{
			Fail("Error in ErrorClient04.test() :", e);
		}

		/////////////////////////////////////////////////////////////////////////////////////////
		// prepare output
		/////////////////////////////////////////////////////////////////////////////////////////

		try
		{
			// create array to hold display results
			displayarray = new ArrayList();
			String[] content = new String[mNumberOfResources + 1];
			String s = "";

			//headers
			for (int j = 0; j < mNumberOfResources; j++)
			{
				s = "Resource :" + (j + 1);
				content[j] = pad(s);
			}
			s = "Final Result";
			content[mNumberOfResources] = pad(s);
			displayarray.add(content);
		}
		catch (Exception e)
		{
			Fail("Error in ErrorClient04.test() :", e);
		}

		/////////////////////////////////////////////////////////////////////////////////////////
		//now perform tests and save results in the above array
		/////////////////////////////////////////////////////////////////////////////////////////
		try
		{
			mCrashPoint = 1;
			//crash type not used in this test
			mCrashType = 1;

			//perform tests
			int testresources = 0;
			int[] mCrashArray;
			for (int i = 0; i < mPermutaionsList.size(); i++)
			{
				mCrashArray = (int[]) mPermutaionsList.get(i);
				testresources = mCrashArray.length;

				ErrorService01 mService = new ErrorService01(testresources);
				int mFinalValue = 0;

				createTx();
				try
				{
					begin();
					mService.setupOper();
					mService.setCrash(mCrashPoint, mCrashArray);
					mFinalValue = intCommit();
				}
				catch (Exception e)
				{
					Fail("Error doing work", e);
				}

				//save the values for display later
				String[] content = new String[mNumberOfResources + 1];
				String s = "";
				for (int x = 0; x < mNumberOfResources + 1; x++)
				{
					int value = -1;
					s = " ";
					if (x < mCrashArray.length)
					{
						value = mCrashArray[x];
						s = TwoPhaseOutcome.stringForm(value);
					}
					content[x] = pad(s);

					if (x == mNumberOfResources)
					{
						s = ActionStatus.stringForm(mFinalValue);
						content[x] = pad(s);
					}
				}
				displayarray.add(content);
			}
		}
		catch (Exception e)
		{
			Fail("Error in ErrorClient04.test() :", e);
		}

		// now display saved data
		displayArray();
	}

	private void createArrayList()
	{
		int numberoftests = 0;
		int displaycounter = 0;
		mPermutaionsList = new ArrayList();
		for (int j = 0; j < mNumberOfResources; j++)
		{
			createIgnoreList(j + 1);
			numberoftests = mMaxInt - mIgnorList.length;
			for (int perms = 0; perms < j; perms++)
			{
				numberoftests = numberoftests * (mMaxInt - mIgnorList.length);
			}
			//setupCounterArray(j + 1);
			for (int i = 0; i < numberoftests; i++)
			{
				int[] last = null;
				if (i != 0 || j != 0)
				{
					last = (int[]) mPermutaionsList.get(displaycounter - 1);
				}

				mPermutaionsList.add(createArrays(j + 1, last));
				//turn this off now its working
				//display(displaycounter);
				displaycounter++;
			}
		}
	}

	private int[] createArrays(int size, int[] last)
	{
		int[] crash = new int[size];
		boolean increase = true;
		int value = -1;
		int testvalue = 0;

		if (last == null)
		{
			value = nextValidValue(value);
			crash[0] = value;
			return crash;
		}
		else
		{
			//if this happend new array size
			if (last.length != crash.length)
			{
				for (int i = 0; i < crash.length; i++)
				{
					value = nextValidValue(-1);
					crash[i] = value;
				}
				return crash;
			}
			for (int i = crash.length - 1; i > -1; i--)
			{
				value = last[i];

				if (increase)
				{
					value = nextValidValue(value);
				}

				testvalue = value;

				if (value > mMaxValue)
				{
					value = nextValidValue(-1);
				}

				if (value != mMaxValue + 1)
				{
					increase = false;
				}

				if (testvalue == mMaxValue + 1)
				{
					increase = true;
				}

				crash[i] = value;
			}
		}
		return crash;
	}

	private int nextValidValue(int value)
	{
		value++;
		for (int i = 0; i < mIgnorList.length; i++)
		{
			if (value == mIgnorList[i])
			{
				value++;
				nextValidValue(value);
			}
		}
		return value;
	}

	private void createIgnoreList(int resources)
	{
		//make sure mIgnoreList is not null
		mIgnorList = new int[0];
		if (mCrashPoint == 1)
		{
			if (resources == 1)
			{
				mIgnorList = new int[3];
				mIgnorList[0] = TwoPhaseOutcome.PREPARE_OK;
				mIgnorList[1] = TwoPhaseOutcome.PREPARE_NOTOK;
				mIgnorList[2] = TwoPhaseOutcome.PREPARE_READONLY;
			}
			else
			{
				mIgnorList = new int[4];
				mIgnorList[0] = TwoPhaseOutcome.PREPARE_OK;
				mIgnorList[1] = TwoPhaseOutcome.PREPARE_NOTOK;
				mIgnorList[2] = TwoPhaseOutcome.PREPARE_READONLY;
				mIgnorList[3] = TwoPhaseOutcome.ONE_PHASE_ERROR;
			}
		}
	}

	private String pad(String s)
	{
		int size = 40;
		String out = s;
		for (int i = s.length(); i < size; i++)
		{
			out += " ";
		}
		return out;
	}

	private void displayArray()
	{
		for (int i = 0; i < displayarray.size(); i++)
		{
			String[] displaydata = null;
			String displayValue = "";
			displaydata = (String[]) displayarray.get(i);

			for (int j = 0; j < displaydata.length; j++)
			{
				displayValue += displaydata[j];
			}

			System.err.println(displayValue);
		}
	}

	/**
	 * Helper method for debugging createarray function.
	 */
	private void display(int i)
	{
		int[] values = (int[]) mPermutaionsList.get(i);
		String s = "";
		for (int j = 0; j < values.length; j++)
		{
			s += values[j] + ", ";
		}
		Debug(s);
	}

	private int mMaxInt = 12;
	private int mMaxValue = 11;
	private ArrayList mPermutaionsList;
	private ArrayList displayarray;
	private int[] mIgnorList = null;

}
