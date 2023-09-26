/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.CrashRecovery08Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery08.*;
import org.omg.CosTransactions.*;

public class ResourceImpl01 implements ResourceOperations
{
	public ResourceImpl01(int objectNumber, int resourceNumber)
	{
		_objectNumber = objectNumber;
		_resourceNumber = resourceNumber;
	}

	public Vote prepare()
			throws HeuristicMixed, HeuristicHazard
	{
		System.err.print("ResourceImpl01.prepare [O" + _objectNumber + ".R" + _resourceNumber + "]: ");

		if (_resourceTrace == ResourceTrace.ResourceTraceNone)
		{
			_resourceTrace = ResourceTrace.ResourceTracePrepare;
		}
		else
		{
			_resourceTrace = ResourceTrace.ResourceTraceUnknown;
		}

		System.err.println("ReturnVoteCommit");

		return Vote.VoteCommit;
	}

	public void rollback()
			throws HeuristicCommit, HeuristicMixed, HeuristicHazard
	{
		System.err.print("ResourceImpl01.rollback [O" + _objectNumber + ".R" + _resourceNumber + "]: ");

		if (_resourceTrace == ResourceTrace.ResourceTraceNone)
		{
			_resourceTrace = ResourceTrace.ResourceTraceRollback;
		}
		else
		{
			_resourceTrace = ResourceTrace.ResourceTraceUnknown;
		}

		System.err.println("Return");
	}

	public void commit()
			throws NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
	{
		if (commitlog && _resourceTrace == ResourceTrace.ResourceTracePrepareCommit)
		{
			System.err.println("ResourceImpl01.commit [O" + _objectNumber + ".R" + _resourceNumber + "]: ");
			System.err.println("Commit called again: This is acceptable");
			return;
		}

		System.err.print("ResourceImpl01.commit [O" + _objectNumber + ".R" + _resourceNumber + "]: ");

		if (_resourceTrace == ResourceTrace.ResourceTracePrepare)
		{
			_resourceTrace = ResourceTrace.ResourceTracePrepareCommit;
			commitlog = true;
		}
		else
		{
			_resourceTrace = ResourceTrace.ResourceTraceUnknown;
		}

		System.err.println("Return");
	}

	public void commit_one_phase()
			throws HeuristicHazard
	{
		System.err.print("ResourceImpl01.commit_one_phase [O" + _objectNumber + ".R" + _resourceNumber + "]: ");

		if (_resourceTrace == ResourceTrace.ResourceTraceNone)
		{
			_resourceTrace = ResourceTrace.ResourceTraceCommitOnePhase;
		}
		else
		{
			_resourceTrace = ResourceTrace.ResourceTraceUnknown;
		}

		System.err.println("Return");
	}

	public void forget()
	{
		System.err.println("ResourceImpl01.forget [O" + _objectNumber + ".R" + _resourceNumber + "]: Return");

		_resourceTrace = ResourceTrace.ResourceTraceUnknown;
	}

	public boolean isCorrect()
	{
		return true;
	}

	public ResourceTrace getTrace()
	{
		return _resourceTrace;
	}

	private int _objectNumber;
	private int _resourceNumber;
	private ResourceTrace _resourceTrace = ResourceTrace.ResourceTraceNone;
	private boolean commitlog = false;
}