/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.CrashRecovery03Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery03.*;
import org.omg.CosTransactions.*;

public class ResourceImpl01 implements ResourceOperations
{
	public ResourceImpl01()
	{
	}

	public Vote prepare()
			throws HeuristicMixed, HeuristicHazard
	{
		return Vote.VoteCommit;
	}

	public void rollback()
			throws HeuristicCommit, HeuristicMixed, HeuristicHazard
	{
	}

	public void commit()
			throws NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
	{
	}

	public void commit_one_phase()
			throws HeuristicHazard
	{
	}

	public void forget()
	{
	}
}