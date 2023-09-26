/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.RawSubtransactionAwareResources02Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
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