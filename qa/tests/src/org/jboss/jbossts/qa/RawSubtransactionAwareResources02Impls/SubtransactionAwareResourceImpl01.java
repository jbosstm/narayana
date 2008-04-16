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
// $Id: SubtransactionAwareResourceImpl01.java,v 1.2 2003/06/26 11:45:04 rbegg Exp $
//

package org.jboss.jbossts.qa.RawSubtransactionAwareResources02Impls;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SubtransactionAwareResourceImpl01.java,v 1.2 2003/06/26 11:45:04 rbegg Exp $
 */

/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SubtransactionAwareResourceImpl01.java,v 1.2 2003/06/26 11:45:04 rbegg Exp $
 */


import org.jboss.jbossts.qa.RawSubtransactionAwareResources02.*;
import org.omg.CosTransactions.*;

public class SubtransactionAwareResourceImpl01 implements SubtransactionAwareResourceOperations
{
	public SubtransactionAwareResourceImpl01(int objectNumber, int subtransactionAwareResourceNumber)
	{
		_donePrepare = false;
		_objectNumber = objectNumber;
		_subtransactionAwareResourceNumber = subtransactionAwareResourceNumber;
	}

	public Vote prepare()
			throws HeuristicMixed, HeuristicHazard
	{
		System.err.println("SubtransactionAwareResourceImpl01.prepare [O" + _objectNumber + ".R" + _subtransactionAwareResourceNumber + "]: Return VoteCommit");

		_subtransactionAwareResourceTrace = SubtransactionAwareResourceTrace.SubtransactionAwareResourceTraceUnknown;

		return Vote.VoteCommit;
	}

	public void rollback()
			throws HeuristicCommit, HeuristicMixed, HeuristicHazard
	{
		System.err.println("SubtransactionAwareResourceImpl01.rollback [O" + _objectNumber + ".R" + _subtransactionAwareResourceNumber + "]: Return");

		_subtransactionAwareResourceTrace = SubtransactionAwareResourceTrace.SubtransactionAwareResourceTraceUnknown;
	}

	public void commit()
			throws NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
	{
		System.err.println("SubtransactionAwareResourceImpl01.commit [O" + _objectNumber + ".R" + _subtransactionAwareResourceNumber + "]: Return");

		_subtransactionAwareResourceTrace = SubtransactionAwareResourceTrace.SubtransactionAwareResourceTraceUnknown;
	}

	public void commit_one_phase()
			throws HeuristicHazard
	{
		System.err.println("SubtransactionAwareResourceImpl01.commit_one_phase [O" + _objectNumber + ".R" + _subtransactionAwareResourceNumber + "]: Return");

		_subtransactionAwareResourceTrace = SubtransactionAwareResourceTrace.SubtransactionAwareResourceTraceUnknown;
	}

	public void forget()
	{
		System.err.println("SubtransactionAwareResourceImpl01.forget [O" + _objectNumber + ".R" + _subtransactionAwareResourceNumber + "]: Return");

		_subtransactionAwareResourceTrace = SubtransactionAwareResourceTrace.SubtransactionAwareResourceTraceUnknown;
	}

	public void rollback_subtransaction()
	{
		System.err.println("SubtransactionAwareResourceImpl01.rollback_subtransaction [O" + _objectNumber + ".R" + _subtransactionAwareResourceNumber + "]: Return");

		if (_subtransactionAwareResourceTrace == SubtransactionAwareResourceTrace.SubtransactionAwareResourceTraceNone)
		{
			_subtransactionAwareResourceTrace = SubtransactionAwareResourceTrace.SubtransactionAwareResourceTraceRollbackSubtransaction;
		}
		else
		{
			_subtransactionAwareResourceTrace = SubtransactionAwareResourceTrace.SubtransactionAwareResourceTraceUnknown;
		}
	}

	public void commit_subtransaction(Coordinator parent)
	{
		System.err.println("SubtransactionAwareResourceImpl01.commit_subtransaction [O" + _objectNumber + ".R" + _subtransactionAwareResourceNumber + "]: Return");

		if (_subtransactionAwareResourceTrace == SubtransactionAwareResourceTrace.SubtransactionAwareResourceTraceNone)
		{
			_subtransactionAwareResourceTrace = SubtransactionAwareResourceTrace.SubtransactionAwareResourceTraceCommitSubtransaction;
		}
		else
		{
			_subtransactionAwareResourceTrace = SubtransactionAwareResourceTrace.SubtransactionAwareResourceTraceUnknown;
		}
	}

	public boolean isCorrect()
	{
		return true;
	}

	public SubtransactionAwareResourceTrace getTrace()
	{
		return _subtransactionAwareResourceTrace;
	}

	private boolean _donePrepare;
	private int _objectNumber;
	private int _subtransactionAwareResourceNumber;
	private SubtransactionAwareResourceTrace _subtransactionAwareResourceTrace = SubtransactionAwareResourceTrace.SubtransactionAwareResourceTraceNone;
}
