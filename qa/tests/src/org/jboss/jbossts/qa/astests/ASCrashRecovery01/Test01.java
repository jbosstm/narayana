package org.jboss.jbossts.qa.astests.ASCrashRecovery01;

import org.jboss.jbossts.qa.astests.taskdefs.ClientAction;
import org.jboss.jbossts.qa.astests.taskdefs.ASTestConfig;

import java.util.Map;

public class Test01 implements ClientAction
{
    public static void main(String args[])
	{
        System.out.println("Passed");
    }

    public boolean execute(ASTestConfig config, Map<String, String> params)
    {
        return true;
    }

    public boolean cancel() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("TODO");
    }
}

