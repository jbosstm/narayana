/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.Common;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.utils.Utility;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

/**
 * Simple test to see if Uid generation is unique.
 */
public class UidTestWorker
{
	/**
	 * This is a sub processso it may be difficult to return our results.
	 */
	public static void main(String[] args)
	{
		int mNumberOfUids = 0;
		try
		{
			mNumberOfUids = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException mfe)
		{
			qautil.debug("error in uid worker using default value of 1000");
			mNumberOfUids = 1000;
		}

        System.err.println("start ms/pid: "+System.currentTimeMillis()+"/"+ Utility.getpid());

		for (int i = 0; i < mNumberOfUids; i++)
		{
			System.out.println(new Uid().toString());
		}

        // hold ownership of the pid socket for at least one second,
        // as that's the uniqness scope of the process init timestamp
        // in Uid. Anything less and another proc may grab the same port
        // within the same second and thus get same Uids.
        try {
            Thread.sleep(1200);
        } catch(InterruptedException e) {

        }

        System.err.println("end pid "+Utility.getpid());
	}
}