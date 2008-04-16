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
 * Date: 11-Mar-02
 * Time: 11:40:43
 */
package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

/**
 * This service class is the container that we will use to emulate
 * how abstract records are to be used by the transaction servcie.
 */
public class Service01
{
	/**
	 * Constructor that will set up the number of abstract records that are going to ber used
	 * in each transaction.
	 */
	public Service01(int i)
	{
		mNumberOfResources = i;
	}

	/**
	 * simple method used to create the abstract records and enlist them into the current running
	 * transaction. if no transaction is running the method will start a new one.
	 */
	public void setupOper()
	{
		setupOper(false);
	}

	/**
	 * passing in true to this operation will force the records to be enlisted into a new transaction
	 * nesting the new transaction within any other running transaction.
	 */
	public void setupOper(boolean nest)
	{
		//create abstract records
		mTransaction = (AtomicAction) AtomicAction.Current();
		if (nest || mTransaction == null)
		{
			mTransaction = new AtomicAction();
			mTransaction.begin();
		}

		qautil.qadebug("createing abstract records and enlisting them");
		mAbstractRecordList = new BasicAbstractRecord[mNumberOfResources];
		//set up abstract records
		for (int i = 0; i < mNumberOfResources; i++)
		{
			mAbstractRecordList[i] = new BasicAbstractRecord();
			if (mTransaction.add(mAbstractRecordList[i]) != AddOutcome.AR_ADDED)
			{
				qautil.debug("Error when adding: " + i + " to atomic action");
				mCorrect = false;
			}
		}
		mNest = nest;
	}

	/**
	 * main body of work will be performed here and any sub transactions that are currently
	 * running will be commited on completion.
	 */
	public void doWork(int workcount)
	{
		for (int j = 0; j < mNumberOfResources; j++)
		{
			for (int i = 0; i < workcount; i++)
			{
				mAbstractRecordList[j].increase();
			}
		}
		if (mTransaction != null && mNest)
		{
			mTransaction.commit();
		}
		mMaxIteration = workcount;
	}

	/**
	 * convenience method for checking counters after test has run
	 */
	public boolean checkAbortOper()
	{
		qautil.qadebug("running check abort");
		for (int i = 0; i < mNumberOfResources; i++)
		{
			//first test to see if increases have been run
			if (mAbstractRecordList[i].getValue() != mMaxIteration)
			{
				qautil.debug("whilst checking the " + i + " resource the getvalue was: " + mAbstractRecordList[i].getValue() + " and we expected: " + mMaxIteration);
				return false;
			}
			if (mNest)
			{
				qautil.qadebug("nested check");
				if (mAbstractRecordList[i].getTLA() != 1)
				{
					qautil.debug("value check wrong on resource " + i);
					return false;
				}
				if (mAbstractRecordList[i].getNC() != 1)
				{
					qautil.debug("nested commit value is wrong in resource " + i);
					return false;
				}
			}
			else
			{
				qautil.qadebug("normal check");
				if (mAbstractRecordList[i].getTLA() != 1 && mAbstractRecordList[i].getTLC() != 1)
				{
					qautil.debug("value check wrong on resource " + i);
					return false;
				}
				if (mAbstractRecordList[i].getNC() != 0)
				{
					qautil.debug("nested commit value is wrong in resource " + i);
					return false;
				}
			}
		}
		return mCorrect;
	}

	/**
	 * convenience method for checking counters after test has run
	 */
	public boolean checkCommitOper()
	{
		qautil.qadebug("running check commit");
		for (int i = 0; i < mNumberOfResources; i++)
		{
			//first test to see if increases have been run
			if (mAbstractRecordList[i].getValue() != mMaxIteration)
			{
				qautil.debug("whilst checking the " + i + " resource the getvalue was: " + mAbstractRecordList[i].getValue() + " and we expected: " + mMaxIteration);
				return false;
			}
			if (mNumberOfResources > 1 && mAbstractRecordList[i].getStateCounter() != 1)
			{
				qautil.debug("save state has not been called on resource " + i);
				return false;
			}
			if (mNest)
			{
				qautil.qadebug("nested check");
				if (mAbstractRecordList[i].getTLA() != 1 && mAbstractRecordList[i].getTLC() != 1)
				{
					qautil.debug("value check wrong on resource " + i);
					return false;
				}
				if (mAbstractRecordList[i].getNC() != 1)
				{
					qautil.debug("nested commit value is wrong in resource " + i + " " + mAbstractRecordList[i].getNC());
					return false;
				}
			}
			else
			{
				qautil.qadebug("normal check");
				if (mAbstractRecordList[i].getTLA() != 1 && mAbstractRecordList[i].getTLC() != 1)
				{
					qautil.debug("value check wrong on resource " + i);
					return false;
				}
				if (mAbstractRecordList[i].getNC() != 0)
				{
					qautil.debug("nested commit value is wrong in resource " + i);
					return false;
				}
			}
		}
		return mCorrect;
	}

	public void storeUIDs(String uniquePrefix)
	{
		for (int j = 0; j < mNumberOfResources; j++)
		{
			String key = uniquePrefix + "resource_" + j;
			try
			{
				qautil.storeUid(key, mAbstractRecordList[j].get_uid());
			}
			catch (Exception e)
			{
				qautil.debug("Error when creating ior store", e);
				mCorrect = false;
			}
		}
	}

	public void restoreUIDs(String uniquePrefix)
	{
		mAbstractRecordList = new BasicAbstractRecord[mNumberOfResources];
		for (int j = 0; j < mNumberOfResources; j++)
		{
			String key = uniquePrefix + "resource_" + j;
			try
			{
				mAbstractRecordList[j] = new BasicAbstractRecord(qautil.loadUid(key));
			}
			catch (Exception e)
			{
				qautil.debug("Error when reading ior store", e);
				mCorrect = false;
			}
		}
	}

	public void clearUIDs(String uniquePrefix)
	{
		for (int j = 0; j < mNumberOfResources; j++)
		{
			String key = uniquePrefix + "resource_" + j;
			try
			{
				qautil.clearUid(key);
			}
			catch (Exception e)
			{
				qautil.debug("Error when reading ior store", e);
				mCorrect = false;
			}
		}
	}

	public boolean checkRestore()
	{
		for (int j = 0; j < mNumberOfResources; j++)
		{
			//we dont expect the value to be saved with abstract records
			if (mAbstractRecordList[j].getValue() != 0)
			{
				qautil.debug("the value has not been retored: " + mAbstractRecordList[j].getValue());
				return false;
			}
		}
		return mCorrect;
	}

	private int mNumberOfResources = 0;
	private int mMaxIteration = 0;
	private boolean mCorrect = true;
	private boolean mNest = false;
	private BasicAbstractRecord[] mAbstractRecordList;
	private AtomicAction mTransaction = null;
}
