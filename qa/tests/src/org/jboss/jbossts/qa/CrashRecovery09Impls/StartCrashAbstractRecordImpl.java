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
//
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
//
// Arjuna Technologies Ltd.,
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//
// $Id: StartCrashAbstractRecordImpl.java,v 1.2 2003/06/26 11:43:44 rbegg Exp $
//

package org.jboss.jbossts.qa.CrashRecovery09Impls;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;

public class StartCrashAbstractRecordImpl extends AbstractRecord
{
	public static final int NO_CRASH = 0;
	public static final int CRASH_IN_PREPARE = 1;
	public static final int CRASH_IN_COMMIT = 2;
	public static final int CRASH_IN_ABORT = 3;

	public StartCrashAbstractRecordImpl(int crashBehavior)
	{
		//
		// to get the appropriate ordering it is necessary to
		// fabricate a suitable objectUid
		//
		super(new Uid("-7FFFFFFF:-7FFFFFFF:0:0:0"), "StartCrashAbstractRecord", ObjectType.NEITHER);

		_crashBehavior = crashBehavior;
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
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int nestedCommit()
	{
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int nestedPrepare()
	{
		return TwoPhaseOutcome.PREPARE_OK;
	}

	public int topLevelAbort()
	{
		if (_crashBehavior == CRASH_IN_ABORT)
		{
			System.out.println("Passed");
			System.exit(0);
		}

		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelCommit()
	{

		if (_crashBehavior == CRASH_IN_COMMIT)
		{
			System.out.println("Passed");
			System.exit(0);
		}

		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelPrepare()
	{
		if (_crashBehavior == CRASH_IN_PREPARE)
		{
			System.out.println("Passed");
			System.exit(0);
		}

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

	private int _crashBehavior = NO_CRASH;
}
