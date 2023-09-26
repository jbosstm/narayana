/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.common;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.internal.arjuna.utils.ExecProcessId;
import com.arjuna.ats.internal.arjuna.utils.FileProcessId;
import com.arjuna.ats.internal.arjuna.utils.MBeanProcessId;
import com.arjuna.ats.internal.arjuna.utils.ManualProcessId;

public class ProcessIdUnitTest
{
    @Test
    public void testFileProcessId()
    {
        FileProcessId fp = new FileProcessId();

        assertTrue(fp.getpid() != -1);
    }
    
    @Test
    public void testManualProcessId()
    {
        arjPropertyManager.getCoreEnvironmentBean().setPid(1);
        
        ManualProcessId mp = new ManualProcessId();

        assertTrue(mp.getpid() == 1);
    }
    
    @Test
    public void testExecProcessId()
    {
		ExecProcessId xp = new ExecProcessId();
		// TODO windows
		if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {

			assertTrue(xp.getpid() > 0);
		}
	}

    @Test
    public void testMBeanProcessId()
    {
        MBeanProcessId mp = new MBeanProcessId();
        
        assertTrue(mp.getpid() > 0);
    }
}