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
package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

/**
 * Simple record used to test AtomicAction
 */
public class BasicAbstractRecord extends AbstractRecord
{
	public BasicAbstractRecord()
	{
		super(new Uid());
	}

	/**
	 * This constructor will be used to recreate the object from an old uid.
	 */
	public BasicAbstractRecord(Uid oldId)
	{
		super(oldId);
	}

	public int typeIs()
	{
		return RecordType.USER_DEF_FIRST0;
	}

	public Object value()
	{
		return null;
	}

	public void setValue(Object object)
	{
	}

	public int nestedAbort()
	{
		qautil.qadebug("nested abort has been called : " + order());
		mNestedAbortCounter++;
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int nestedOnePhaseCommit()
	{
		qautil.qadebug("nested one phase comit has been called : " + order());
		mNestedCommitCounter++;
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int nestedCommit()
	{
		qautil.qadebug("nested comit has been called : " + order());
		mNestedCommitCounter++;
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int nestedPrepare()
	{
		mNestedPrepareCounter++;
		return TwoPhaseOutcome.PREPARE_OK;
	}

	public int topLevelAbort()
	{
		qautil.qadebug("top level abort has been called : " + order());
		mTopLevelAbortCounter++;
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelOnePhaseCommit()
	{
		qautil.qadebug("top level one phase commit has been called : " + order());
		mTopLevelCommitCounter++;
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelCommit()
	{
		qautil.qadebug("top level commit has been called : " + order());
		mTopLevelCommitCounter++;
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelPrepare()
	{
		qautil.qadebug("prep has been called : " + order());
		mTopLevelPrepareCounter++;
		return TwoPhaseOutcome.PREPARE_OK;
	}

	public void alter(AbstractRecord abstractRecord)
	{
	}

	public void merge(AbstractRecord abstractRecord)
	{
	}

	public boolean shouldAdd(AbstractRecord abstractRecord)
	{
		return false;
	}

	public boolean shouldAlter(AbstractRecord abstractRecord)
	{
		return false;
	}

	public boolean shouldMerge(AbstractRecord abstractRecord)
	{
		return false;
	}

	public boolean shouldReplace(AbstractRecord abstractRecord)
	{
		return false;
	}

	/**
	 * My methods to test abstract record is being processed correctly by the transaction
	 * manager.
	 */
	public void increase()
	{
		mValue++;
	}

	public int getValue()
	{
		return mValue;
	}

	/**
	 * Override method to indicate we want this object to be saved.
	 */
	public boolean doSave()
	{
		return true;
	}

	public boolean save_state(OutputObjectState objectState, int objectType)
	{
		qautil.qadebug("save state called when value = " + mValue);
		mStaveStateCounter++;
		super.save_state(objectState, objectType);
		try
		{
			objectState.packInt(mValue);
			return true;
		}
		catch (Exception exception)
		{
			qautil.debug("BasicAbstractRecord.save_state: ", exception);
			return false;
		}
	}

	/**
	 * As this is an abstract record restore state does not function as a ait object
	 * but will be used by the crash recovery engine.
	 */
	public boolean restore_state(InputObjectState objectState, int objectType)
	{
		qautil.qadebug("restore state called");
		super.restore_state(objectState, objectType);
		try
		{
			mValue = objectState.unpackInt();
			qautil.qadebug("value restored to " + mValue);
			return true;
		}
		catch (Exception exception)
		{
			qautil.debug("BasicAbstractRecord.restore_state: ", exception);
			return false;
		}
	}

	public String type()
	{
		return "/StateManager/BasicAbstractRecord";
	}

	public static String thisType()
	{
		return "/StateManager/BasicAbstractRecord";
	}

	public int getStateCounter()
	{
		return mStaveStateCounter;
	}

	public int getTLC()
	{
		return mTopLevelCommitCounter;
	}

	public int getTLP()
	{
		return mTopLevelPrepareCounter;
	}

	public int getTLA()
	{
		return mTopLevelAbortCounter;
	}

	public int getNP()
	{
		return mNestedPrepareCounter;
	}

	public int getNC()
	{
		return mNestedCommitCounter;
	}

	public int getNA()
	{
		return mNestedAbortCounter;
	}

	private int mStaveStateCounter = 0;
	private int mTopLevelCommitCounter = 0;
	private int mTopLevelAbortCounter = 0;
	private int mTopLevelPrepareCounter = 0;
	private int mNestedPrepareCounter = 0;
	private int mNestedCommitCounter = 0;
	private int mNestedAbortCounter = 0;
	private int mValue = 0;
}

