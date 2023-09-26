/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.Utils;
import org.jboss.profiler.jvmti.JVMTIInterface;

public class JVMStats
{
	public static long getMemory()
	{
		Runtime runtime = Runtime.getRuntime();

		long presentMemory = (runtime.totalMemory() - runtime.freeMemory());
		long memory = Long.MAX_VALUE;

		while (presentMemory < memory)
		{
			memory = presentMemory;

            doGarbageCollection();

			presentMemory = (runtime.totalMemory() - runtime.freeMemory());
		}

		return memory;
	}

    private static void doGarbageCollection()
    {
/*
        // no clean way to force this from within the jvm, so we'll sleep and hope the gc runs.
        System.gc();
        try {
            Thread.sleep(100);
        } catch(InterruptedException e) {

        }
        
        JBTM-555: use jboss profiler agent to invoke gc via jvmti.
        This requires native code, so tests using this will only
        execute on supported environments.
*/
        JVMTIInterface jvmtiInterface = new JVMTIInterface();

        jvmtiInterface.forceGC();
    }
}