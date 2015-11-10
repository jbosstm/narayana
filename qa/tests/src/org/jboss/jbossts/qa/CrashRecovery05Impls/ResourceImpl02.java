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
// $Id: ResourceImpl02.java,v 1.2 2003/06/26 11:43:37 rbegg Exp $
//

package org.jboss.jbossts.qa.CrashRecovery05Impls;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ResourceImpl02.java,v 1.2 2003/06/26 11:43:37 rbegg Exp $
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
 * $Id: ResourceImpl02.java,v 1.2 2003/06/26 11:43:37 rbegg Exp $
 */


import org.jboss.jbossts.qa.CrashRecovery05.*;
import org.omg.CosTransactions.*;

public class ResourceImpl02 implements ResourceOperations
{
	public ResourceImpl02(int objectNumber, int resourceNumber)
	{
		_objectNumber = objectNumber;
		_resourceNumber = resourceNumber;
		_status = Status.StatusNoTransaction;
	}

	public Vote prepare()
			throws HeuristicMixed, HeuristicHazard
	{
		System.err.print("ResourceImpl02.prepare [O" + _objectNumber + ".R" + _resourceNumber + "]: ");

		if (_resourceTrace == ResourceTrace.ResourceTraceNone)
		{
			_resourceTrace = ResourceTrace.ResourceTracePrepare;
		}
		else
		{
			_resourceTrace = ResourceTrace.ResourceTraceUnknown;
		}

		_status = Status.StatusPrepared;

		System.err.println("ReturnVoteCommit");

		return Vote.VoteCommit;
	}

	public void rollback()
			throws HeuristicCommit, HeuristicMixed, HeuristicHazard
	{
		System.err.print("ResourceImpl02.rollback [O" + _objectNumber + ".R" + _resourceNumber + "]: ");

		if (isComplete())
			return;

		if (_resourceTrace == ResourceTrace.ResourceTraceNone)
		{
			_resourceTrace = ResourceTrace.ResourceTraceRollback;
		}
		else if (_resourceTrace == ResourceTrace.ResourceTracePrepare)
		{
			_resourceTrace = ResourceTrace.ResourceTracePrepareRollback;
		}
		else
		{
			_resourceTrace = ResourceTrace.ResourceTraceUnknown;
		}

		_status = Status.StatusRolledBack;

		System.err.println("Return");
	}

	public void commit()
			throws NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
	{
		System.err.print("ResourceImpl02.commit [O" + _objectNumber + ".R" + _resourceNumber + "]: ");

		if (isComplete())
			return;

		if (_resourceTrace == ResourceTrace.ResourceTraceNone)
		{
			_resourceTrace = ResourceTrace.ResourceTraceCommit;
		}
		else if (_resourceTrace == ResourceTrace.ResourceTracePrepare)
		{
			_resourceTrace = ResourceTrace.ResourceTracePrepareCommit;
		}
		else
		{
			_resourceTrace = ResourceTrace.ResourceTraceUnknown;
		}

		_status = Status.StatusCommitted;

		System.err.println("Return");
	}

	public void commit_one_phase()
			throws HeuristicHazard
	{
		System.err.print("ResourceImpl02.commit_one_phase [O" + _objectNumber + ".R" + _resourceNumber + "]: ");

		if (isComplete())
			return;

		if (_resourceTrace == ResourceTrace.ResourceTraceNone)
		{
			_resourceTrace = ResourceTrace.ResourceTraceCommitOnePhase;
		}
		else
		{
			_resourceTrace = ResourceTrace.ResourceTraceUnknown;
		}

		_status = Status.StatusCommitted;

		System.err.println("Return");
	}

	public void forget()
	{
		System.err.print("ResourceImpl02.forget [O" + _objectNumber + ".R" + _resourceNumber + "]: ");

		if (_resourceTrace == ResourceTrace.ResourceTracePrepare)
		{
			_resourceTrace = ResourceTrace.ResourceTracePrepareForget;
		}
		else if (_resourceTrace == ResourceTrace.ResourceTracePrepareRollback)
		{
			_resourceTrace = ResourceTrace.ResourceTracePrepareRollbackForget;
		}
		else if (_resourceTrace == ResourceTrace.ResourceTracePrepareCommit)
		{
			_resourceTrace = ResourceTrace.ResourceTracePrepareCommitForget;
		}
		else if (_resourceTrace == ResourceTrace.ResourceTraceCommitOnePhase)
		{
			_resourceTrace = ResourceTrace.ResourceTraceCommitOnePhaseForget;
		}
		else
		{
//			_resourceTrace = ResourceTrace.ResourceTraceUnknown;
		}

		System.err.println("Return");
	}

	public boolean isCorrect()
	{
		return true;
	}

	public ResourceTrace getTrace()
	{
		return _resourceTrace;
	}

	public boolean isComplete() {
		return _status == Status.StatusCommitted || _status == Status.StatusRolledBack;
	}

	public Status getStatus() {
		return _status;
	}

	public Status updateStatus(Status hint) {
		Status prev = _status;

		System.err.printf("ResourceImpl01.updateStatus [O%d.R%d]: status=%d -> %d%n",
				_objectNumber, _resourceNumber, _status.value(), hint.value());

		if (_status == Status.StatusUnknown || _status == Status.StatusNoTransaction) {
			try {
				if (hint == Status.StatusCommitted) {
					System.err.printf("ResourceImpl01.updateStatus call commit()%n");
					commit();
				} else if (hint == Status.StatusRolledBack) {
					System.err.printf("ResourceImpl01.updateStatus call rollback()%n");
					rollback();
				} else {
					_status = hint;
				}
			} catch (Exception e) {
				_status = hint;
			}
		}

		return prev;
	}

	private Status _status;
	private int _objectNumber;
	private int _resourceNumber;
	private ResourceBehavior _resourceBehavior;
	private ResourceTrace _resourceTrace = ResourceTrace.ResourceTraceNone;
}
