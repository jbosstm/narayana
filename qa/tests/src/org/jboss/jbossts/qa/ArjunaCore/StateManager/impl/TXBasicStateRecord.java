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
package org.jboss.jbossts.qa.ArjunaCore.StateManager.impl;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

/**
 * Simple record used to test AtomicAction
 */
public class TXBasicStateRecord extends StateManager
{
	public TXBasicStateRecord()
	{
		super();
		qautil.qadebug("starting construction");
		activate();
		modified();
		deactivate();
		qautil.qadebug("ending construction");
	}

	public TXBasicStateRecord(int id)
	{
		super(ObjectType.ANDPERSISTENT);
		qautil.qadebug("starting construction");
		activate();
		modified();
		deactivate();
		mId = id;
		qautil.qadebug("ending construction");
	}

	public TXBasicStateRecord(Uid oldId)
	{
		super(oldId, ObjectType.ANDPERSISTENT);
		qautil.qadebug("starting construction");
		activate();
		deactivate();
		qautil.qadebug("ending construction");
	}

	public int typeIs()
	{
		return RecordType.USER_DEF_FIRST0;
	}

	/**
	 * We will start a subtrancastion during the increase to see what effet this has.
	 */
	public void increase()
	{
		qautil.qadebug("start increase");
		AtomicAction a = new AtomicAction();
		try
		{
			a.begin();
			activate();
			modified();
			mValue++;
			a.commit();
		}
		catch (Exception e)
		{
			a.abort();
			qautil.debug("exception in increase method ", e);
		}
		qautil.qadebug("end increase");
	}

	public int getValue()
	{
		AtomicAction a = new AtomicAction();
		a.begin();
		activate();
		deactivate();
		a.commit();
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
		super.save_state(objectState, objectType);
		qautil.qadebug("save state called when value = " + mValue);
		try
		{
			objectState.packInt(mValue);
			return true;
		}
		catch (Exception exception)
		{
			qautil.debug("TXBasicAbstractRecord.save_state: ", exception);
			return false;
		}
	}

	/**
	 * As this is an abstract record restore state does not function as a ait object
	 * but will be used by the crash recovery engine.
	 */
	public boolean restore_state(InputObjectState objectState, int objectType)
	{
		super.restore_state(objectState, objectType);
		qautil.qadebug("restore state called");
		try
		{
			mValue = objectState.unpackInt();
			return true;
		}
		catch (Exception exception)
		{
			qautil.debug("TXBasicAbstractRecord.restore_state: ", exception);
			return false;
		}
	}

	public String type()
	{
		return "/StateManager/TXBasicStateRecord";
	}

	private int mValue = 0;
	private boolean mDebug = false;
	private int mId = 0;
}

