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
 * This is a work in progress version of errorclient04
 * <p/>
 * It seemed a good idea to put the next pahase of the development in a new class
 * since it took a long time to get to this point.
 */
public class ErrorClient03 extends BaseTestClient
{
	public static void main(String[] args)
	{
		ErrorClient03 test = new ErrorClient03(args);
	}

	private ErrorClient03(String[] args)
	{
		super(args);
	}

	public void Test()
	{
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
			Fail("Error in ErrorClient03.test() :", e);
		}

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

				String s = "";
				for (int j = 0; j < mCrashArray.length; j++)
				{
					int value = 0;
					value = mCrashArray[j];
					s += "Resource " + (j + 1) + " : " + TwoPhaseOutcome.stringForm(value) + " ";
				}
				Debug(s + "Final value = " + ActionStatus.stringForm(mFinalValue));
			}
		}
		catch (Exception e)
		{
			Fail("Error in ErrorClient03.test() :", e);
		}

	}

	private void createArrayList()
	{

		int numberoftests = 0;
		int displaycounter = 0;
		mPermutaionsList = new ArrayList();
		for (int j = 0; j < mNumberOfResources; j++)
		{
			numberoftests = mMaxInt;// - mIgnorList.length;
			for (int perms = 0; perms < j; perms++)
			{
				numberoftests = numberoftests * mMaxInt;
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
				display(displaycounter);
				displaycounter++;
			}
		}
	}

	private int[] createArrays(int size, int[] last)
	{
		int[] crash = new int[size];

		if (last == null)
		{
			crash[0] = 0;
			return crash;
		}
		else
		{
			boolean increase = true;
			int value = 0;
			int testvalue = 0;
			//if this happend new array size
			if (last.length != crash.length)
			{
				for (int i = 0; i < crash.length; i++)
				{
					crash[i] = 0;
				}
				return crash;
			}
			for (int i = crash.length - 1; i > -1; i--)
			{
				value = last[i];

				if (increase)
				{
					value++;
				}

				testvalue = value;

				if (value > mMaxValue)
				{
					value = 0;
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
	private int[] mIgnorList = null;

}
