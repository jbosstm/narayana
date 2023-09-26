/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery05Clients1;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery05.CrashBehavior;
import org.jboss.jbossts.qa.Utils.OTS;
import org.omg.CosTransactions.HeuristicHazard;

public class Client01b
{
	public static void main(String[] args)
	{
        ClientBeforeCrash beforeCrash = new ClientBeforeCrash(Client01b.class.getSimpleName());

		try
		{
            beforeCrash.initOrb(args);
            beforeCrash.initCrashBehaviour(CrashBehavior.CrashBehaviorCrashInCommitOnePhase);
            beforeCrash.serviceSetup();

			try
			{
				OTS.current().commit(true);
				beforeCrash.setCorrect(false);  // should have got a heuristic
			}
			catch (HeuristicHazard heuristicHazard)
			{
			}

            beforeCrash.reportStatus();
		}
		catch (Exception exception)
		{
            beforeCrash.reportException(exception);
		}
        finally {
            beforeCrash.shutdownOrb();
        }
	}
}